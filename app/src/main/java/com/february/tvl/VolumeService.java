package com.february.tvl;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.IBinder;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;


public class VolumeService extends Service {

    private static  int MAX_VOLUME = 14;
    private static  int SET_VOLUME = 10;
    private static  long LAST_SYNC = 0;

    private AudioManager audioManager;
    private BroadcastReceiver volumeReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        long unixTime = System.currentTimeMillis() / 1000L;
        Log.d( "unixTime", String.valueOf( unixTime ) );
        getVolume();
        volumeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() != null && intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")) {
                    int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    if (currentVolume > MAX_VOLUME) {
                        getVolume();
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, SET_VOLUME, 0);
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter("android.media.VOLUME_CHANGED_ACTION");
        registerReceiver(volumeReceiver, filter);
    }

    private long getVolume(){
        if( BuildConfig.isAPI ){
            if( (System.currentTimeMillis() / 1000L) > LAST_SYNC ) {
                RequestQueue queue = Volley.newRequestQueue(this);
                String url = BuildConfig.endpoint;
                Log.d("network: ", url);
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d("VolumeService: response:", response);
                                Gson gson = new Gson();
                                Type type = new TypeToken<Map<String, Object>>() {
                                }.getType();
                                Map<String, Object> dataMap = gson.fromJson(response, type);

                                MAX_VOLUME = Integer.parseInt(dataMap.get("MAX_VOL").toString());
                                SET_VOLUME = Integer.parseInt(dataMap.get("SET_VOL").toString());

                                long twentyMinutesInMillis = 20 * 60 * 1000;
                                LAST_SYNC = (System.currentTimeMillis() / 1000L) + twentyMinutesInMillis;

                                Log.d("VolumeService", "MAX_VOL: " + MAX_VOLUME);
                                Log.d("VolumeService", "SET_VOL: " + SET_VOLUME);
                                Log.d("VolumeService", "CURRENT_SYNC: "+(System.currentTimeMillis() / 1000L)+" | LAST_SYNC: " + LAST_SYNC);
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("TVL: Error: ", error.toString());
                    }
                });
                queue.add(stringRequest);
            }else{
                Log.d("Why?", "It's not the time yet.");
            }
        }else{
            long twentyMinutesInMillis = 20 * 60 * 1000;
            LAST_SYNC = (System.currentTimeMillis() / 1000L) + twentyMinutesInMillis;
        }


        return 0;
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