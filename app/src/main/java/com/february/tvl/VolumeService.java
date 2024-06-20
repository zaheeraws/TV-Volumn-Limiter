package com.february.tvl;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.IBinder;
import android.util.Log;

public class VolumeService extends Service {

    private static final int MAX_VOLUME = 14;
    private static final int SET_VOLUME = 10;
    private AudioManager audioManager;
    private BroadcastReceiver volumeReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        volumeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() != null && intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")) {
                    int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    if (currentVolume > MAX_VOLUME) {
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, SET_VOLUME, 0);
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter("android.media.VOLUME_CHANGED_ACTION");
        registerReceiver(volumeReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(volumeReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}