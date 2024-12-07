package com.mtai.metronome;


import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.view.View;
import android.os.Looper;
import android.media.MediaPlayer;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;

import com.mtai.metronome.databinding.ActivityMainBinding;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'music' library on application startup.
    static {
        System.loadLibrary("music");
    }

    private ActivityMainBinding binding;
    private boolean isRunning=false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int tickCount = 0;
    private double tickInterval;
    private long startTime;
    private static final int SAMPLE_RATE = 44100;
    private MediaPlayer mediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Example of a call to a native method
        TextView tv = binding.sampleText;
        tv.setText(stringFromJNI());
        SnipperInit();
        BmpTextViewInit();
        bmpBtnListener();
        prepareMedia();
        reCalInterval();
        switch_conf();

    }


    private void prepareMedia()  {
        if(mediaPlayer == null)
        {
            mediaPlayer = MediaPlayer.create(this,R.raw.ding);
        }
    }

    private void switch_conf(){
        Switch sw = binding.Switch;
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked)
                {
                    isRunning = true;
                    startMetronome();
                }
                else {
                    isRunning = false;
                    tickCount = 0;
                    handler.removeCallbacksAndMessages(null);
                }
            }
        });
    }

    private void startMetronome() {
        startTime = SystemClock.elapsedRealtime();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(isRunning){
                    mediaPlayer.start();
                    binding.sampleText.setText(String.valueOf(tickCount));
                    tickCount += 1;
                    long now = SystemClock.elapsedRealtime();
                    long nextTickTime = startTime + tickCount * (long)tickInterval;
                    handler.postDelayed(this,Math.max(0,nextTickTime - now));
                    //handler.postDelayed(this,(long)tickInterval);

                }
            }
        },1000);
    }


    private void SnipperInit(){
        Spinner spinner = binding.spinner;

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.beats, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinner.setSelection(position);
                //setSampleFromJNI();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle no selection case (optional)

            }
        });
    }
    private void setSampleFromJNI(){
        binding.sampleText.setText( callJNI(binding.bmpBar.getProgress(),String.valueOf(binding.spinner.getSelectedItem())));

    }

    private void reCalInterval(){
        tickInterval = 60.0/ (double)binding.bmpBar.getProgress() * 1000;
    }

    private void BmpTextViewInit(){
        TextView bmp_tv = binding.bmpTextView;
        SeekBar bmpBar = binding.bmpBar;
        bmp_tv.setText(String.valueOf(bmpBar.getProgress()));
        bmpBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                bmp_tv.setText(String.valueOf(bmpBar.getProgress()));
                long curTime = tickCount * (long)tickInterval + startTime;
                reCalInterval();
                startTime = curTime - tickCount * (long)tickInterval;
                //setSampleFromJNI();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void bmpBtnListener(){
        SeekBar bmpBar = binding.bmpBar;
        findViewById(R.id.Add10).setOnClickListener(v -> bmpBar.setProgress(bmpBar.getProgress() + 10));
        findViewById(R.id.Add5 ).setOnClickListener(v -> bmpBar.setProgress(bmpBar.getProgress() + 5 ));
        findViewById(R.id.Add1 ).setOnClickListener(v -> bmpBar.setProgress(bmpBar.getProgress() + 1 ));

        findViewById(R.id.Sub10).setOnClickListener(v -> bmpBar.setProgress(bmpBar.getProgress() - 10));
        findViewById(R.id.Sub5 ).setOnClickListener(v -> bmpBar.setProgress(bmpBar.getProgress() - 5 ));
        findViewById(R.id.Sub1 ).setOnClickListener(v -> bmpBar.setProgress(bmpBar.getProgress() - 1 ));
    }


    /**
     * A native method that is implemented by the 'music' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
    public native String callJNI(int bmp,String beats);
}