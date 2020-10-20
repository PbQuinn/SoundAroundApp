package com.example.soundaroundapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
//Import Bluetooth libraries
import android.app.Activity;
import android.bluetooth.*;
import android.bluetooth.le.*;
//Import Android libraries
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class ConnectionActivity extends AppCompatActivity {

    public ConnectionActivity activity = this;
    public MainActivity mainActivity;
    public BluetoothDevice connectedDevice;
    public BluetoothGatt deviceConnection;
    public BluetoothGattCharacteristic writeCharacteristic;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_ENABLE_LOCATION = 1;


    final int STATUS_CONNECTED = 0;
    final int STATUS_CONNECTING = 1;
    final int STATUS_DISCONNECTED = 2;

    String UUID_TX = "6E400003-B5A3-F393-E0A9-E50E24DCCA9E";
    String UUID_RX = "6E400002-B5A3-F393-E0A9-E50E24DCCA9E";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_LONG).show();
            finish();
        }
        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        final Object passedActivity = ((ObjectWrapperForBinder) Objects.requireNonNull(Objects.requireNonNull(getIntent().getExtras()).getBinder("object_value"))).getData();
        mainActivity = (MainActivity) passedActivity;

    }


    private BluetoothLeScanner bluetoothLeScanner =
            BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
    private boolean mScanning;
    private Handler handler = new Handler();

    // Stops scanning after 10 seconds.

    private static final long SCAN_PERIOD = 5000;
    ArrayList<BluetoothDevice> results = new ArrayList<>();
    RecyclerView resultList;

    public void scanLeDevice(View view) {

        if (!mScanning) {
            Button scanButton = activity.findViewById(R.id.button1);
            scanButton.setClickable(false);
            scanButton.setText("Scanning...");

            Toast.makeText(activity, "Starting Scan!", Toast.LENGTH_SHORT).show();
            // Stops scanning after a pre-defined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    bluetoothLeScanner.stopScan(leScanCallback);
                    Toast.makeText(activity, "Scan finished!", Toast.LENGTH_SHORT).show();
                    Button scanButton = activity.findViewById(R.id.button1);
                    scanButton.setClickable(true);
                    scanButton.setText("Scan for Devices");
                    resultList = findViewById(R.id.resultList);
                    resultList.setLayoutManager(new LinearLayoutManager(activity));
                    bleRecyclerViewAdapter bleViewAdapter = new bleRecyclerViewAdapter(activity, results, activity.getLayoutInflater());
                    resultList.setAdapter(bleViewAdapter);
                    resultList.setVisibility(View.VISIBLE);
                    findViewById(R.id.progressBarConnection).setVisibility(View.GONE);

                }
            }, SCAN_PERIOD);
            mScanning = true;
            bluetoothLeScanner.startScan(leScanCallback);
            findViewById(R.id.progressBarConnection).setVisibility(View.VISIBLE);

        } else {
            Toast.makeText(activity, "Already scanning, stopped!", Toast.LENGTH_SHORT).show();
            mScanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
            findViewById(R.id.progressBarConnection).setVisibility(View.GONE);
        }
    }



    public void homeButtonClick(View v){
        //Once connection is established and writeCharacteristic has been found, bundle it and send
        //it to the main activity.
        if(connectedDevice != null && deviceConnection != null && writeCharacteristic != null) {
            mainActivity.updateConnectedDevice(connectedDevice, deviceConnection, writeCharacteristic);
        }
        finish();
    }


    // Device scan callback.
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            //If we encounter a new device, add to the list
            if (!results.contains(result.getDevice())){
                results.add(result.getDevice());
            }
            super.onScanResult(callbackType, result);
        }
    };


    public void connectToDevice(int position){
        final BluetoothDevice connectDevice = results.get(position);
        if(connectedDevice != null){
            changeConnectedDevice(connectedDevice, STATUS_DISCONNECTED);
        }
        changeConnectedDevice(connectDevice, STATUS_CONNECTING);


        //Finally, update current connected device to new device.
        connectedDevice = connectDevice;
        if(deviceConnection != null){
            deviceConnection.close();
        }



        // Various callback methods defined by the BLE API.
        BluetoothGattCallback gattCallback =
                new BluetoothGattCallback() {
                    private final String TAG = BluetoothLeService.TAG;

                    @Override
                    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                        if (newState == BluetoothProfile.STATE_CONNECTED) {
                            Log.i(TAG, "Connected to GATT server.");
                            changeConnectedDevice(connectedDevice, STATUS_CONNECTED);
                            gatt.discoverServices();
                        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                            Log.i(TAG, "Disconnected from GATT server.");
                            changeConnectedDevice(connectedDevice, STATUS_DISCONNECTED);
                        }
                    }


                    @Override
                    // New services discovered
                    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            for (BluetoothGattService gattService : gatt.getServices()) {
                                Log.i(TAG, "onServicesDiscovered: service=" + gattService.getUuid());

                                for (BluetoothGattCharacteristic characteristic : gattService.getCharacteristics()) {
                                    Log.i(TAG, "onServicesDiscovered: characteristic=" + characteristic.getUuid().toString());

                                    if (characteristic.getUuid().toString().equals(UUID_RX.toLowerCase())) {
                                        writeCharacteristic = characteristic;
                                    }
                                }
                            }
                        } else {
                            Log.w(BluetoothLeService.TAG, "onServicesDiscovered received: " + status);
                        }
                    }

                    @Override
                    // Result of a characteristic read operation
                    public void onCharacteristicRead(BluetoothGatt gatt,
                                                     BluetoothGattCharacteristic characteristic,
                                                     int status) {
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                        }
                    }

                    @Override
                    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                        super.onCharacteristicWrite(gatt, characteristic, status);
                        System.out.println("status: " + status);
                    }
                };

        deviceConnection = connectedDevice.connectGatt(this, false, gattCallback);


    }


    //Upkeep of GUI with connection status
    public void changeConnectedDevice(BluetoothDevice device, int connectionStatus){

        String newStatus = "";
        int position = results.indexOf(device);
        switch(connectionStatus){
            case STATUS_CONNECTED:
                newStatus = "Connected!";
                break;

            case STATUS_CONNECTING:
                newStatus = "Connecting...";
                break;

            case STATUS_DISCONNECTED:
                newStatus = "";
                break;
        }

        //Update the connection status textview to the appropriate status.
        ((bleRecyclerViewAdapter) Objects.requireNonNull(resultList.getAdapter()))
                .getRecyclerViewItem(position).textViewConnection.setText(newStatus);
    }
}


