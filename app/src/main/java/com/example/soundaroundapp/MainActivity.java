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
import android.widget.Toast;

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
        connectionActivity.mainActivity = activity;
    }

    public void connectionButtonClick(View v){
        Intent switchActivityIntent = new Intent(this, connectionActivity.getClass());
        startActivity(switchActivityIntent);
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
        activity.findViewById(R.id.textViewConnectedDevice);
    }

}
