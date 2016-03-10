package gjj_unit_test.gjj_buletooth_demo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 作者：gjj on 2016/3/9 11:52
 * 邮箱：Gujj512@163.com
 * 如果你只想扫描指定类型的外围设备，可以改为调用startLeScan(UUID[], BluetoothAdapter.LeScanCallback)),
 * 需要提供你的app支持的GATT services的UUID对象数组。
 */
public class BelNewActivity extends AppCompatActivity {
    @Bind(R.id.listview)
    ListView listview;

    private BluetoothAdapter mBluetoothAdapter;
    private final int REQUEST_ENABLE_BT = 111;//蓝牙发送请求码
    private Context context;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    ArrayList<BluetoothDevice> deviceList = new ArrayList<>();
    ArrayList<String> devices = new ArrayList<>();
    private UUID[] myUUID;
    private BluetoothReceiver receiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bel_new);
        ButterKnife.bind(this);

        this.context = this;
        mLeDeviceListAdapter = new LeDeviceListAdapter(context);
        listview.setAdapter(mLeDeviceListAdapter);
        initUUID();
        initBroadCast();//注册广播
        initBel();
        mLeDeviceListAdapter = new LeDeviceListAdapter(context);
        listview.setAdapter(mLeDeviceListAdapter);
    }

    public void initBroadCast() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        receiver = new BluetoothReceiver();
        registerReceiver(receiver, filter);
    }

    public void initBel() {
        // 使用此检查确定BLE是否支持在设备上，然后你可以有选择性禁用BLE相关的功能
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(context, "该设备暂不支持BLE-蓝牙低功耗,应用程序讲关闭", Toast.LENGTH_SHORT).show();
            finish();
        }
        // 初始化 Bluetooth adapter, 通过蓝牙管理器得到一个参考蓝牙适配器(API必须在以上android4.3或以上和版本)
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        //接下来开启蓝牙，你需要确认蓝牙是否开启。调用isEnabled())去检测蓝牙当前是否开启。如果该方法返回false,蓝牙被禁用。
        // 确保蓝牙在设备上可以开启
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            // 我们通过startActivityForResult()方法发起的Intent将会在onActivityResult()回调方法中获取用户的选择，比如用户单击了Yes开启，
            // 那么将会收到RESULT_OK的结果，
            // 如果RESULT_CANCELED则代表用户不愿意开启蓝牙
            Intent mIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);//让自己的设备呗别人看见,最大不超过300秒
            startActivityForResult(mIntent, REQUEST_ENABLE_BT);
            // 用enable()方法来开启，无需询问用户(实惠无声息的开启蓝牙设备),这时就需要用到android.permission.BLUETOOTH_ADMIN权限。
            // mBluetoothAdapter.enable();
            // mBluetoothAdapter.disable();//关闭蓝牙
        } else {

            //发现BLE设备，使用startLeScan())方法
            mLeDeviceListAdapter.clear();
            //扫描设备，只需要调用startDiscovery()方法，这个扫描的过程大概持续是12秒，
            //应用程序为了ACTION_FOUND动作需要注册一个BroadcastReceiver来接受设备扫描到的信息。对于每一个设备，系统都会广播ACTION_FOUND动作。
            mBluetoothAdapter.startDiscovery();//开始
        }
    }

    //.监视Bluetooth打开状态
    BroadcastReceiver bluetoothState = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String stateExtra = BluetoothAdapter.EXTRA_STATE;
            int state = intent.getIntExtra(stateExtra, -1);
            switch (state) {
                case BluetoothAdapter.STATE_TURNING_ON://正在打开
                    break;
                case BluetoothAdapter.STATE_ON://打开了
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF://正在关闭
                    break;
                case BluetoothAdapter.STATE_OFF://关闭了
                    break;
            }
        }
    };

    public void initUUID() {
        UUID u1 = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");
        UUID u2 = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
        UUID u3 = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");

        myUUID = new UUID[]{u1, u2, u3};
    }

    public class BluetoothReceiver extends BroadcastReceiver {

        public BluetoothReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (isLock(device)) {
                    devices.add(device.getName());
                }
                deviceList.add(device);
                showDevices(device);
            }
        }

        private boolean isLock(BluetoothDevice device) {
//        boolean isLockName = (device.getName()).equals(lockName);
//        boolean isSingleDevice = devices.indexOf(device.getName()) == -1;
//        return isLockName && isSingleDevice;
            return false;
        }

        private void showDevices(BluetoothDevice device) {
            mLeDeviceListAdapter.addDevice(device);
        }
    }
    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);//注销广播
        super.onDestroy();
    }
}
