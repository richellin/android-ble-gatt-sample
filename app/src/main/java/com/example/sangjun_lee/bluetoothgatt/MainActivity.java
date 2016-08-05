package com.example.sangjun_lee.bluetoothgatt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    //UUID
    private static final String SERVICE_UUID = "65432461-1EFE-4ADB-BC7E-9F7F8E27FDC1";
    private static final String CHAR_UUID = "65432461-1EFE-4ADB-BC7E-9F7F8E27FDC1";

    public BluetoothGattServer gattServer;
    public BluetoothDevice mDevice;
    public BluetoothGattCharacteristic mCharacteristic;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setGattServer();
    }

    public void setGattServer() {
        BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = manager.getAdapter();
        BluetoothLeAdvertiser advertiser = adapter.getBluetoothLeAdvertiser();
        // GattServer
        gattServer = manager.openGattServer(getApplicationContext(), new BluetoothGattServerCallback() {
            @Override
            public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
                super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);

                if (value != null) {
                    Log.d("TAG", "value ~ " + new String(value));
                }
                gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null);
            }

            @Override
            public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
                gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, "ABC".getBytes());

            }

            @Override
            public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
                super.onConnectionStateChange(device, status, newState);
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    mDevice = device;
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    mDevice = null;
                }
            }
        });


        //Service
        BluetoothGattService service = new BluetoothGattService(
                UUID.fromString(SERVICE_UUID),
                BluetoothGattService.SERVICE_TYPE_PRIMARY);
        mCharacteristic = new BluetoothGattCharacteristic(
                UUID.fromString(CHAR_UUID),
                BluetoothGattCharacteristic.PROPERTY_NOTIFY |
                        BluetoothGattCharacteristic.PROPERTY_READ |
                        BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_READ |
                        BluetoothGattCharacteristic.PERMISSION_WRITE);
        service.addCharacteristic(mCharacteristic);
        gattServer.addService(service);


        AdvertiseSettings.Builder builder = new AdvertiseSettings.Builder();

        builder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
        builder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW);
        builder.setConnectable(true);
        builder.setTimeout(0);

        AdvertiseData.Builder builder2 = new AdvertiseData.Builder();
        builder2.addServiceUuid(new ParcelUuid(UUID.fromString(CHAR_UUID)));


        //アドバタイズを開始
        advertiser.startAdvertising(builder.build(), builder2.build(), new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
            }

            @Override
            public void onStartFailure(int errorCode) {
                super.onStartFailure(errorCode);
            }
        });
    }
}

