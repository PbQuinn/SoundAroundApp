package com.example.soundaroundapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
//Import Bluetooth libraries
import android.bluetooth.*;
import android.bluetooth.le.*;
//Import Android libraries
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class ConnectionActivity extends AppCompatActivity {

    ConnectionActivity activity = this;
    BluetoothDevice connectedDevice;
    BluetoothGatt deviceConnection;

    private static final int REQUEST_ENABLE_BT = 1;

    final int STATUS_CONNECTED = 0;
    final int STATUS_CONNECTING = 1;
    final int STATUS_DISCONNECTED = 2;

    boolean ledOn = false;
    String UUID_RX = "6E400003-B5A3-F393-E0A9-E50E24DCCA9E";
    String UUID_TX = "6E400002-B5A3-F393-E0A9-E50E24DCCA9E";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    }

    private BluetoothLeScanner bluetoothLeScanner =
            BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
    private boolean mScanning;
    private Handler handler = new Handler();

    // Stops scanning after 10 seconds.

    private static final long SCAN_PERIOD = 10000;
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

                }
            }, SCAN_PERIOD);
            mScanning = true;
            bluetoothLeScanner.startScan(leScanCallback);

        } else {
            Toast.makeText(activity, "Already scanning, stopped!", Toast.LENGTH_SHORT).show();
            mScanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
        }
    }


    public void sendCommand(View v){

        Button scanButton = activity.findViewById(R.id.ledButton);
        BluetoothGattCharacteristic MwriteCharacteristic =
                new BluetoothGattCharacteristic(UUID.fromString(UUID_TX),
                        BluetoothGattCharacteristic.PROPERTY_WRITE,
                        BluetoothGattCharacteristic.PERMISSION_WRITE);
        String str;

        if(ledOn){
            scanButton.setBackgroundColor(Color.LTGRAY);
            str = "A";
        } else {
            scanButton.setBackgroundColor(Color.GREEN);
            str = "B";
        }
        if((MwriteCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE ) > 0) {
            byte[] strBytes = str.getBytes();
            byte[] bytes = MwriteCharacteristic.getValue();
            MwriteCharacteristic.setValue(bytes);
            boolean sendStatus = deviceConnection.writeCharacteristic(MwriteCharacteristic);
        } else {
            scanButton.setBackgroundColor(Color.LTGRAY);
            ledOn = !ledOn;
            str = "B";
        }
        ledOn = !ledOn;
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
                    @Override
                    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                        if (newState == BluetoothProfile.STATE_CONNECTED) {
                            Log.i(BluetoothLeService.TAG, "Connected to GATT server.");
                            changeConnectedDevice(connectedDevice, STATUS_CONNECTED);

                        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                            Log.i(BluetoothLeService.TAG, "Disconnected from GATT server.");
                            changeConnectedDevice(connectedDevice, STATUS_DISCONNECTED);
                        }
                    }

                    @Override
                    // New services discovered
                    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                        if (status == BluetoothGatt.GATT_SUCCESS) {

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
                };

        deviceConnection = connectedDevice.connectGatt(this, false, gattCallback);



        // Handles various events fired by the Service.
// ACTION_GATT_CONNECTED: connected to a GATT server.
// ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
// ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
// ACTION_DATA_AVAILABLE: received data from the device. This can be a
// result of read or notification operations.
        final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {

                } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {

                } else if (BluetoothLeService.
                        ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                    // Show all the supported services and characteristics on the
                    // user interface.
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    //Do another thing
                }
            }
        };
    }


    //Upkeep of GUI with connection status
    public void changeConnectedDevice(BluetoothDevice device, int connectionStatus){

        String newStatus = "";
        int position = results.indexOf(device);
        switch(connectionStatus){
            case STATUS_CONNECTED:
                newStatus = "Connected!";
                String newConnectName = device.getAddress();
                if (device.getName() != null){
                    newConnectName = results.get(position).getName();
                }
                ((TextView) activity.findViewById(R.id.textViewConnectedDevice)).setText(newConnectName);
                ((Button) activity.findViewById(R.id.ledButton)).setClickable(true);
                break;

            case STATUS_CONNECTING:
                newStatus = "Connecting...";
                break;

            case STATUS_DISCONNECTED:
                newStatus = "";
                ((TextView) activity.findViewById(R.id.textViewConnectedDevice)).setText("None");
                ((Button) activity.findViewById(R.id.ledButton)).setClickable(false);
                ((Button) activity.findViewById(R.id.ledButton)).setBackgroundColor(Color.LTGRAY);
                break;
        }

        //Update the connection status textview to the appropriate status.
        ((bleRecyclerViewAdapter) Objects.requireNonNull(resultList.getAdapter()))
                .getRecyclerViewItem(position).textViewConnection.setText(newStatus);
    }

}

