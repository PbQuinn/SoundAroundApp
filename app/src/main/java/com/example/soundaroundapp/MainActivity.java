package com.example.soundaroundapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;

public class MainActivity  extends AppCompatActivity {

    private static final String PREFS_NAME = "prefs";
    private static final String PREF_DARK_THEME = "dark_theme";

    MainActivity activity = this;

    private String left0Prefix = "L";
    private String right0Prefix = "R";
    private String both0Prefix = "B";

    private String sensitivity1Prefix = "S";
    private String intensity1Prefix = "I";
    private String enableDisable1Prefix = "O";


    public BluetoothDevice connectedDevice;
    public BluetoothGatt deviceConnection;
    public BluetoothGattCharacteristic writeCharacteristic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        setContentView(R.layout.activity_main);

        //Initialize the appropriate methods for the GUI components that command the ESP32.
        ((SeekBar) findViewById(R.id.seekBarSensitivity)).setOnSeekBarChangeListener(new commandingSeekBarListener(both0Prefix, sensitivity1Prefix, activity, (TextView) findViewById(R.id.textViewSensitivityAmount)));
        ((SeekBar) findViewById(R.id.seekBarIntensity)).setOnSeekBarChangeListener(new commandingSeekBarListener(both0Prefix, intensity1Prefix, activity, (TextView) findViewById(R.id.textViewIntensityAmount)));
        ((SeekBar) findViewById(R.id.seekBarDeviationLeft)).setOnSeekBarChangeListener(new commandingSeekBarListener(left0Prefix, sensitivity1Prefix, activity,  (TextView) findViewById(R.id.textViewDeviationLeftAmount)));
        ((SeekBar) findViewById(R.id.seekBarIntensityLeft)).setOnSeekBarChangeListener(new commandingSeekBarListener(left0Prefix, intensity1Prefix, activity,  (TextView) findViewById(R.id.textViewIntensityLeftAmount)));
        ((SeekBar) findViewById(R.id.seekBarDeviationRight)).setOnSeekBarChangeListener(new commandingSeekBarListener(right0Prefix, sensitivity1Prefix, activity, (TextView) findViewById(R.id.textViewDeviationRightAmount)));
        ((SeekBar) findViewById(R.id.seekBarIntensityRight)).setOnSeekBarChangeListener(new commandingSeekBarListener(right0Prefix, intensity1Prefix, activity,  (TextView) findViewById(R.id.textViewIntensityRightAmount)));


        ((Switch) findViewById(R.id.switchBoth)).setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sendCommand(both0Prefix+enableDisable1Prefix+"1");
                } else {
                    sendCommand(both0Prefix+enableDisable1Prefix+"0");
                }
            }
        });

        ((Switch) findViewById(R.id.switchLeft)).setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sendCommand(left0Prefix+enableDisable1Prefix+"1");
                } else {
                    sendCommand(left0Prefix+enableDisable1Prefix+"0");
                }
            }
        });
        ((Switch) findViewById(R.id.switchRight)).setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sendCommand(right0Prefix+enableDisable1Prefix+"1");
                } else {
                    sendCommand(right0Prefix+enableDisable1Prefix+"0");
                }

            }
        });

        super.onCreate(savedInstanceState);
    }


    public void connectionButtonClick(View v){
        //Package our current main activity and pass it
        final Object objSent = activity;
        final Bundle bundle = new Bundle();
        bundle.putBinder("object_value", new ObjectWrapperForBinder(objSent));
        //Launch the connection activity;
        startActivity(new Intent(this, ConnectionActivity.class).putExtras(bundle));
    }


    public void foldAccordion2(View v){
        View foldDown = activity.findViewById(R.id.settingsLayout);
        int visible = foldDown.getVisibility();
        if(visible != View.VISIBLE){
            foldDown.setVisibility(View.VISIBLE);
        } else {
            foldDown.setVisibility(View.GONE);
        }
    }

    public void foldAccordion2p1(View v){
        View foldDown = activity.findViewById(R.id.settingsAdvancedLayout);
        int visible = foldDown.getVisibility();
        if(visible != View.VISIBLE){
            foldDown.setVisibility(View.VISIBLE);
        } else {
            foldDown.setVisibility(View.GONE);
        }
    }

    public void updateConnectedDevice(BluetoothDevice device, BluetoothGatt deviceConnection, BluetoothGattCharacteristic writeCharacteristic){
        activity.connectedDevice = device;
        activity.deviceConnection = deviceConnection;
        activity.writeCharacteristic = writeCharacteristic;
        String deviceName = "Unknown Device";
        if(device.getName() != null){
            deviceName = device.getName();
        } else {
            deviceName = device.getAddress();
        }
        ((TextView) activity.findViewById(R.id.textViewConnectedDevice)).setText(deviceName);

    }

    public void sendCommand(String commandValue){
        System.out.println(commandValue);
        System.out.println(writeCharacteristic == null);
        if (deviceConnection != null && writeCharacteristic != null){
            activity.writeCharacteristic.setValue(commandValue.getBytes());
            deviceConnection.writeCharacteristic(writeCharacteristic);
        }
    }

    public void disConnectDevice(View v){
        if(deviceConnection != null){
            deviceConnection.close();
            deviceConnection = null;
            connectedDevice = null;
            writeCharacteristic = null;
            ((TextView) activity.findViewById(R.id.textViewConnectedDevice)).setText("None");
        }
    }

}
