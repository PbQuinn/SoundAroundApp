package com.example.soundaroundapp;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class bleRecyclerViewAdapter extends RecyclerView.Adapter<bleRecyclerViewAdapter.bleViewHolder>{

    private ConnectionActivity parent;
    private ArrayList<BluetoothDevice> devices;
    private ArrayList<bleViewHolder> listItems = new ArrayList<>();
    private LayoutInflater inflater;

    public bleRecyclerViewAdapter(ConnectionActivity parent, ArrayList<BluetoothDevice> devices, LayoutInflater inflater){
        this.parent = parent;
        this.devices = devices;
        this.inflater = inflater;
    }


    //Responsible for creating one instance of a device to populate the RecyclerView with.
    @NonNull
    @Override
    public bleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.ble_list_item, parent, false);
        bleViewHolder holder = new bleViewHolder(v);
        holder.textViewName = (TextView) v.findViewById(R.id.textViewName);
        holder.textViewDesc = (TextView) v.findViewById(R.id.textViewDesc);
        holder.textViewConnection = (TextView) v.findViewById(R.id.textViewConnection);
        return holder;
    }


    //Responsible for providing the contents of one initialized item in the RecyclerViewList
    @Override
    public void onBindViewHolder(@NonNull bleViewHolder holder, int position) {
        BluetoothDevice device = devices.get(position);
        String deviceName;
        if(device.getName() != null){
            deviceName = device.getName();
        } else {
            deviceName = "Unknown Device";
        }

        holder.textViewName.setText(deviceName);
        holder.textViewDesc.setText(device.getAddress());
        holder.position = position;
        //Create a custom onClickListener with custom method for this list item.
        bleOnClickListener bleListItemOnClickListener = new bleOnClickListener(position, parent);
        holder.view.setOnClickListener(bleListItemOnClickListener);
        //and add it to the list of available items on the list
        listItems.add(holder);
    }


    //Returns the amount of items on the list.
    @Override
    public int getItemCount() {
        return devices.size();
    }

    public bleViewHolder getRecyclerViewItem(int position){
        return listItems.get(position);
    }



    //Helper object that holds the contents of one list item.
     public static class bleViewHolder extends RecyclerView.ViewHolder{
        View view;
        TextView textViewName;
        TextView textViewDesc;
        TextView textViewConnection;
        int position;

        public bleViewHolder(@NonNull View itemView) {
            super(itemView);
            this.view = itemView;
        }
    }


    //Custom OnClickListener that implements passing back
    // the necessary information to the connect activity;
    public static class bleOnClickListener implements View.OnClickListener{
        //We store the position of this object to retrieve the corresponding BluetoothDevice and
        //the activity to later connect to said device.
        int position;
        ConnectionActivity parent;
        //Constructor links the position (passed on creation) of the item to this listener and
        //provides the activity.
        public bleOnClickListener(int position, ConnectionActivity parent){
            this.position = position;
            this.parent = parent;
        }
        //Clicking on the particular item on position i will connect to the i'th device in the list
        //of found devices.
        @Override
        public void onClick(View v) {
            parent.connectToDevice(position);
        }
    }


}
