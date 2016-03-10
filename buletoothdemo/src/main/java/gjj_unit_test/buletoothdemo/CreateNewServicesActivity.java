package gjj_unit_test.buletoothdemo;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：gjj on 2016/3/10 11:57
 * 邮箱：Gujj512@163.com
 */
public class CreateNewServicesActivity extends AppCompatActivity {

    @Bind(R.id.btn_new_services)
    Button btnNewServices;

    BluetoothGattService service;
    BluetoothGattCharacteristic character;
    BluetoothManager manager;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_services);
        ButterKnife.bind(this);
    }
    @OnClick({R.id.btn_new_services})
    void onclick(View view) {
        switch (view.getId()) {
            case R.id.btn_new_services://新建一个服务
                createServices();
                break;
        }
    }
    public void createServices(){
        //每一个周边BluetoothGattServer，包含多个服务Service，每一个Service包含多个特征Characteristic。
        //1.new一个特征：
        character = new BluetoothGattCharacteristic(
                UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT),
                BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ);
        //2.new一个服务
        service = new BluetoothGattService(UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT),BluetoothGattService.SERVICE_TYPE_PRIMARY);
        //3.把特征添加到服务：
        character.setValue("test-data");
        service.addCharacteristic(character);
        //4.获取BluetoothManager：
        manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        //5.获取/打开周边：
        BluetoothGattServer server = manager.openGattServer(this, new BluetoothGattServerCallback() {
            @Override
            public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
                super.onConnectionStateChange(device, status, newState);
            }

            @Override
            public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
                if (requestId == BluetoothGatt.GATT_SUCCESS) {
                    Log.e("onCharacteristicReadRequest", "onCharacteristicReadRequest===");
                } else {
                    Log.e("onCharacteristicReadRequest", "onCharacteristicReadRequest received: 读取服务===" + requestId);
                }
            }

            @Override
            public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
                super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
                if (requestId == BluetoothGatt.GATT_SUCCESS) {
                    Log.e("WriteRequest", "onCharacteristicWriteRequest");
                    Toast.makeText(CreateNewServicesActivity.this,"WriteRequest",Toast.LENGTH_LONG).show();
                } else {
                    Log.e("WriteRequest", "onServicesDeiscovered received: 写入服务" + requestId);
                }
            }
        });
        //6.把service添加到周边：
        server.addService(service);
        //7.开始广播service：Google还没有广播Service的API，等吧！！！！！所以目前我们还不能让一个Android手机作为周边来提供数据。
    }
}

