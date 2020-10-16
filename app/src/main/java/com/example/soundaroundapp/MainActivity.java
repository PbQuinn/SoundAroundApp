package com.example.soundaroundapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity  extends AppCompatActivity {

    MainActivity activity = this;
    ConnectionActivity connectionActivity = new ConnectionActivity();

    public BluetoothDevice connectedDevice;
    public BluetoothGatt deviceConnection;
    public BluetoothGattCharacteristic writeCharacteristic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void connectionButtonClick(View v){
        //Package our current main activity and pass it
        final Object objSent = activity;
        final Bundle bundle = new Bundle();
        bundle.putBinder("object_value", new ObjectWrapperForBinder(objSent));
        //Launch the connection activity;
        startActivity(new Intent(this, ConnectionActivity.class).putExtras(bundle));
    }

    public void saveNewName(View v){
        EditText editText = (EditText) activity.findViewById(R.id.editTextDeviceName);
        String text = editText.getText().toString();
        editText.setText(text);
    }

    public void foldAccordion1(View v){
        View foldDown = activity.findViewById(R.id.personalLayout);
        int visible = foldDown.getVisibility();
        if(visible != View.VISIBLE){
            foldDown.setVisibility(View.VISIBLE);
        } else {
            foldDown.setVisibility(View.GONE);
        }
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

    public void updateConnectedDevice(BluetoothDevice device, BluetoothGatt deviceConnection, BluetoothGattCharacteristic writeCharacteristic){
        this.connectedDevice = device;
        this.deviceConnection = deviceConnection;
        this.writeCharacteristic = writeCharacteristic;
        String deviceName = "Unknown Device";
        if(device.getName() != null){
            deviceName = device.getName();
        } else {
            deviceName = device.getAddress();
        }
        ((TextView) activity.findViewById(R.id.textViewConnectedDevice)).setText(deviceName);
    }

}
