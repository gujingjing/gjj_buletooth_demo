package gjj_unit_test.gjj_buletooth_demo;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.btn_chek_ble)
    Button btnChekBle;
    @Bind(R.id.fab)
    FloatingActionButton fab;
    @Bind(R.id.btn_set_ble)
    Button btnSetBle;
    @Bind(R.id.lv)
    ListView lv;
    @Bind(R.id.btn_set_ble_stop)
    Button btnSetBleStop;
    @Bind(R.id.btn_set_ble_new)
    Button btnSetBleNew;

    private Context context;
    private BluetoothAdapter mBluetoothAdapter;
    private final int REQUEST_ENABLE_BT = 111;//蓝牙发送请求码
    private boolean mScanning;//是否扫描
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private Handler mHandler;
    // 10秒后停止查找搜索.
    private static final long SCAN_PERIOD = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        this.context = this;
        mHandler = new Handler();
        checkBle();//检查是否支持ble
        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter(context);
        lv.setAdapter(mLeDeviceListAdapter);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @OnClick({R.id.btn_chek_ble, R.id.btn_set_ble,R.id.btn_set_ble_new,R.id.btn_set_ble_stop})
    void onclick(View view) {
        switch (view.getId()) {
            case R.id.btn_chek_ble:// 检查当前手机是否支持ble 蓝牙,如果不支持退出程序
                checkBle();
                break;
            case R.id.btn_set_ble:

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
                    startActivityForResult(mIntent, REQUEST_ENABLE_BT);
                    // 用enable()方法来开启，无需询问用户(实惠无声息的开启蓝牙设备),这时就需要用到android.permission.BLUETOOTH_ADMIN权限。
                    // mBluetoothAdapter.enable();
                    // mBluetoothAdapter.disable();//关闭蓝牙
                } else {

                    //发现BLE设备，使用startLeScan())方法
                    mLeDeviceListAdapter.clear();
                    scanLeDevice(true);
                }

                break;
            case R.id.btn_set_ble_stop://停止
                scanLeDevice(false);
                break;
            case R.id.btn_set_ble_new://扫描指定类型的外围设备
                /**
                 * 如果你只想扫描指定类型的外围设备，可以改为调用startLeScan(UUID[], BluetoothAdapter.LeScanCallback)),
                 * 需要提供你的app支持的GATT services的UUID对象数组。
                 */
                Intent intent=new Intent(MainActivity.this,BelNewActivity.class);
                startActivity(intent);
                break;
        }
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
//            mHandler.postDelayed(new Runnable() {//十秒之后停止扫描
//
//                @Override
//                public void run() {
//                    mScanning = false;
//                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
//                    invalidateOptionsMenu();
//                }
//            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }

    /**
     * 设备扫描的回调接口
     */
    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLeDeviceListAdapter.addDevice(device);
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    public boolean checkBle() {
        // 使用此检查确定BLE是否支持在设备上，然后你可以有选择性禁用BLE相关的功能
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(context, "该设备暂不支持BLE-蓝牙低功耗,应用程序讲关闭", Toast.LENGTH_SHORT).show();
            finish();
            return false;
        } else {
            Toast.makeText(context, "支持BLE-蓝牙低功耗", Toast.LENGTH_SHORT).show();
            return true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 为了确保设备上蓝牙能使用, 如果当前蓝牙设备没启用,弹出对话框向用户要求授予权限来启用
        if (mBluetoothAdapter!=null&&!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
//        scanLeDevice(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {//这里以请求码,判断
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, "蓝牙已经开启", Toast.LENGTH_SHORT).show();
                    mLeDeviceListAdapter.clear();
                    scanLeDevice(true);
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "不允许蓝牙开启,应用程序讲关闭", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }
}
