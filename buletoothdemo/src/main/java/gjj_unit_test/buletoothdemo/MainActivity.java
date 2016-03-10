package gjj_unit_test.buletoothdemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    BluetoothAdapter mBluetoothAdapter;
    @Bind(R.id.listView)
    ListView listView;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.btn_set_ble)
    Button btnSetBle;
    @Bind(R.id.fab)
    FloatingActionButton fab;
    private BluetoothReceiver receiver;
    private final int REQUEST_ENABLE_BT = 111;//蓝牙发送请求码
    LeDeviceListAdapter mLeDeviceListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mLeDeviceListAdapter = new LeDeviceListAdapter(this);
        listView.setAdapter(mLeDeviceListAdapter);
        //注册广播接收器
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        receiver = new BluetoothReceiver();
        registerReceiver(receiver, filter); //在onDestroy时记得注销广播接收器

        //自己的设置被别人可见
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "该设备暂不支持BLE-蓝牙低功耗,应用程序讲关闭", Toast.LENGTH_SHORT).show();
            finish();
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            mLeDeviceListAdapter.clear();
            mBluetoothAdapter.startDiscovery();
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, ConnectActivity.class);
                intent.putExtra("bluetoothDevice", (Parcelable) mLeDeviceListAdapter.getDevice(position));
                startActivity(intent);
            }
        });
    }

    @OnClick({R.id.btn_set_ble,R.id.btn_new_services})
    void onclick(View view) {
        switch (view.getId()) {
            case R.id.btn_set_ble:
                mLeDeviceListAdapter.clear();
                mBluetoothAdapter.startDiscovery();
                break;
            case R.id.btn_new_services://新建一个服务
                Intent intent=new Intent(MainActivity.this,CreateNewServicesActivity.class);
                startActivity(intent);
                break;
        }
    }

    public class BluetoothReceiver extends BroadcastReceiver {

        public BluetoothReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mLeDeviceListAdapter.addDevice(device);
                Toast.makeText(MainActivity.this, device.getAddress(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);//注销广播
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {//这里以请求码,判断
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, "蓝牙已经开启", Toast.LENGTH_SHORT).show();
                    mLeDeviceListAdapter.clear();
                    mBluetoothAdapter.startDiscovery();
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "不允许蓝牙开启,应用程序讲关闭", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }
}
