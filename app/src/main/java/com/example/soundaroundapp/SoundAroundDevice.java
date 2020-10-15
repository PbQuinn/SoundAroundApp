package com.example.soundaroundapp;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

public class SoundAroundDevice {

    public BluetoothDevice connectedDevice;
    public BluetoothGatt deviceConnection;
    public BluetoothGattCharacteristic writeCharacteristic;

    public SoundAroundDevice(BluetoothDevice device, BluetoothGatt deviceConnection, BluetoothGattCharacteristic writeCharacteristic){
        this.connectedDevice = device;
        this.deviceConnection = deviceConnection;
        this.writeCharacteristic = writeCharacteristic;
    }
}
