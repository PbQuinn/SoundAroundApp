package com.example.soundaroundapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.widget.SeekBar;
import android.widget.TextView;

public class commandingSeekBarListener implements SeekBar.OnSeekBarChangeListener {

    MainActivity parent;
    TextView contentTextView;

    String side0Prefix;
    String change1Prefix;

    String progressString;


    public commandingSeekBarListener(String sidePrefix, String change1Prefix, MainActivity parent, TextView contentTextView){
        this.side0Prefix = sidePrefix;
        this.change1Prefix = change1Prefix;
        this.parent = parent;
        this.contentTextView = contentTextView;
    }

        @SuppressLint("SetTextI18n")
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            contentTextView.setText(progress + "%");
            progressString = "100";
            if(progress < 100){
                progressString = "0"+progress;
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            parent.sendCommand(side0Prefix+change1Prefix+progressString);
        }

}
