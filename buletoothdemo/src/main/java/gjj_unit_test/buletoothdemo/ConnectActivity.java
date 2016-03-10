package gjj_unit_test.buletoothdemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：gjj on 2016/3/9 15:14
 * 邮箱：Gujj512@163.com
 */
public class ConnectActivity extends AppCompatActivity {

    BluetoothDevice device;
    @Bind(R.id.tv_device_name)
    TextView tvDeviceName;
    @Bind(R.id.tv_device_uuid)
    TextView tvDeviceUuid;

    BluetoothAdapter mBluetoothAdapter;
    @Bind(R.id.tv_state)
    TextView tvState;
    @Bind(R.id.btn_connect)
    Button btnConnect;
    @Bind(R.id.gatt_services_list)
    ExpandableListView gattServicesList;
    @Bind(R.id.tv_device_data)
    TextView tvDeviceData;

    private BluetoothLeService mBluetoothLeService;
    String TAG = "ConnectActivity=====";
    private boolean mConnected = false;//连接状态
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();//蓝牙接收的数据模型

    //用来在map中存储对应的值
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private BluetoothGattCharacteristic mNotifyCharacteristic;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        ButterKnife.bind(this);
        initBLE();
        initView();
        initServicesAndBroadCaset();//将activity和services绑定,注册广播，用来接收服务发出的状态
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    public void initServicesAndBroadCaset() {
        //注册广播，用来接收服务发出的状态,参数二为过滤器
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());


        /**
         * bindService(intent,mConnection, Context.BIND_AUTO_CREATE);
         第一个bindService()的参数是一个明确指定了要绑定的service的Intent．
         第二个参数是ServiceConnection对象．
         第三个参数是一个标志，它表明绑定中的操作．它一般应是BIND_AUTO_CREATE，
         这样就会在service不存在时创建一个．其它可选的值是BIND_DEBUG_UNBIND和BIND_NOT_FOREGROUND,不想指定时设为0即可．
         */
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        //注意:在manifests里面添加
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {//设备连接完成
                mConnected = true;
                updateConnectionState(R.string.connected);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {//设备连接失败
                mConnected = false;
                updateConnectionState(R.string.disconnected);
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {//发现有服务
                Log.e("发现有服务===","发现有服务===");
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {//数据展示的--intent里面包含了数据
                Log.e("有数据可以展示===","有数据可以展示===");
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    private void displayData(String data) {
        if (data != null) {
            tvDeviceData.setText(data);
        }
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // 循环可用的GATT services
        for (BluetoothGattService gattService : gattServices) {//遍历发现的所有服务
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            //如果存在这个uuid,返回对应的name，没有就返回默认的name
            currentServiceData.put(LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // 循环可用Characteristics
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        //测试，显示数据的

        //生成一个SimpleExpandableListAdapter对象
        //1.context
        //2.一级条目的数据
        //3.用来设置一级条目样式的布局文件
        //4.指定一级条目数据的key
        //5.指定一级条目数据显示控件的id
        //6.指定二级条目的数据
        //7.用来设置二级条目样式的布局文件
        //8.指定二级条目数据的key
        //9.指定二级条目数据显示控件的id
        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,//1
                gattServiceData,//2
                android.R.layout.simple_expandable_list_item_2,//3
                new String[]{LIST_NAME, LIST_UUID},//4
                new int[]{android.R.id.text1, android.R.id.text2},//5
                gattCharacteristicData,//6
                android.R.layout.simple_expandable_list_item_2,//7
                new String[]{LIST_NAME, LIST_UUID},//8
                new int[]{android.R.id.text1, android.R.id.text2}//9
        );
        gattServicesList.setAdapter(gattServiceAdapter);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvState.setText(getResources().getString(resourceId));
            }
        });
    }

    public void initBLE() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "该设备暂不支持BLE-蓝牙低功耗,应用程序讲关闭", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void initView() {
        device = getIntent().getParcelableExtra("bluetoothDevice");
        if (device != null) {
            tvDeviceName.setText(device.getName() == null ? "" : device.getName());
            tvDeviceUuid.setText(device.getAddress() == null ? "" : device.getAddress());
        } else {
            Toast.makeText(this, "无法获取设备信息", Toast.LENGTH_SHORT).show();
            finish();
        }
        gattServicesList.setOnChildClickListener(servicesListClickListner);
    }

    @OnClick({R.id.btn_connect})
    void onclick(View view) {
        switch (view.getId()) {
            case R.id.btn_connect:
                boolean result=mBluetoothLeService.connect(device.getAddress());//开始连接
                Log.e(TAG, "Connect request result=" + result);
                break;
        }
    }
    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    if (mGattCharacteristics != null) {
                        final BluetoothGattCharacteristic characteristic =
                                mGattCharacteristics.get(groupPosition).get(childPosition);
                        final int charaProp = characteristic.getProperties();//获取文件属性
                        Log.e("文件属性===PROPERTY_READ",(charaProp | BluetoothGattCharacteristic.PROPERTY_READ)+"");


                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {//Characteristic property: Characteristic is readable.
                            // If there is an active notification on a characteristic, clear
                            // it first so it doesn't update the data field on the user interface.
                            if (mNotifyCharacteristic != null) {
                                //第一个参数:哪个特点用来通知
                                //第二个参数:设置为true来启用通知/迹象
                                mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, false);
                                mNotifyCharacteristic = null;
                            }
                            Log.e("开始读取","开始读取");
                            mBluetoothLeService.readCharacteristic(characteristic);//开始读取
                        }
                        Log.e("文件属性===PROPERTY_NOTIFY",(charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY)+"");
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {//Characteristic property: Characteristic supports notification
                            mNotifyCharacteristic = characteristic;
                            mBluetoothLeService.setCharacteristicNotification(characteristic, true);
                        }

                        // TODO 待检测的，实验代码
                        //UUID_KEY_DATA是可以跟蓝牙模块串口通信的Characteristic
                        if(characteristic.getUuid().toString().equals(SampleGattAttributes.UUID_KEY_DATA)){

                            //接受Characteristic被写的通知,收到蓝牙模块的数据后会触发mOnDataAvailable.onCharacteristicWrite()
                            mBluetoothLeService.setCharacteristicNotification(characteristic, true);
                            //设置数据内容
                            characteristic.setValue("send data->");
                            //往蓝牙模块写入数据
                            mBluetoothLeService.writeCharacteristic(characteristic);

                            //测试读取当前Characteristic数据，会触发mOnDataAvailable.onCharacteristicRead()
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Log.e("开始读取","开始读取");
                                    mBluetoothLeService.readCharacteristic(characteristic);//开始读取
                                }
                            }, 500);

                        }
                        return true;
                    }

                    return false;
                }
            };
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            Log.e(TAG, "服务链接成功");
            //注意这里的连接跟2.0的蓝牙的连接不一样，通过设备的connectGatt方法进行连接，
            // 连接完成后会获得一个BluetoothGatt的对象，这个对象中有连接的一些重要信息，切记要保存好。
            //BluetoothGatt作为中央来使用和处理数据；BluetoothGattCallback返回中央的状态和周边提供的数据。
            //BluetoothGattServer作为周边来提供数据；BluetoothGattServerCallback返回周边的状态。
            boolean result=mBluetoothLeService.connect(device.getAddress());//开始连接
            Log.e(TAG, "Connect request result=" + result);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
            Log.e(TAG, "服务链接失败了");
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mGattUpdateReceiver);
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }
}
