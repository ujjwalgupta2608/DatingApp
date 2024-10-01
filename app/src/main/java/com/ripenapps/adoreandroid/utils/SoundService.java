package com.ripenapps.adoreandroid.utils;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class SoundService extends Service {
    private static final String TAG = null;
    MediaPlayer player;
    IBinder mBinder = new LocalBinder();
    int value = 0;
    boolean isRepeat = false;
    Ringtone r;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
//        player = MediaPlayer.create(this, notification);
        r = RingtoneManager.getRingtone(getApplicationContext(), notification);
    }

    @SuppressLint("WrongConstant")
    public int onStartCommand(Intent intent, int flags, int startId) {


        // CommonUtils.log("myValue", new Gson().toJson(intent.getIntExtra("data", 0)));

        /*value = intent.getIntExtra("data", 0);
        isRepeat=intent.getBooleanExtra("repeat", false);*/
//        player = MediaPlayer.create(this, CommonUtilsKt.soundValue(CONSTANTS.INCOMING_CALL));

//        player.setLooping(true); // Set looping
//        player.setVolume(100, 100);
//        player.start();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            r.setLooping(true);
        }
        r.play();

        return 1;
    }

    public void setVolumeLow() {

        if (player != null) {
            player.setVolume(0, 0);
        }

    }

    public void setVolumeFull() {
        if (player != null) {
            player.setVolume(100, 100);
        }
    }

    public void onStart(Intent intent, int startId) {
        // TO DO


    }

    public IBinder onUnBind(Intent arg0) {
        // TO DO Auto-generated method


        return null;
    }

    public void onStop(Context context) {
       /* Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        r = RingtoneManager.getRingtone(context, notification);

        if (player != null) {
            player.stop();
            player.release();
        }

        r.stop();*/
    }

    public void onPause() {

    }

    @Override
    public void onDestroy() {
//        Toast.makeText(this, "stoped", Toast.LENGTH_SHORT).show();
        if (player != null) {
            player.stop();
            player.release();
        }
        r.stop();
    }

    @Override
    public void onLowMemory() {

    }

    public class LocalBinder extends Binder {

        public SoundService getServerInstance() {
            return SoundService.this;
        }

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
//        Toast.makeText(getApplicationContext(), "close", Toast.LENGTH_SHORT).show();

        if (player != null) {
            player.stop();
            player.release();
        }
        r.stop();

        /*val socket:Socket

        val userData = Gson().fromJson(
                Preferences.getStringPreference(this, USER),
                UserSignIn::class.java
        )
        Log.e("TAG", "offlineEvent: ${userData._id}")
        val app = application as BotshApplication
        socket = app.getSocket()

        val data = JSONObject()
        data.put("userId", userData._id)
        socket.emit("show_offline", data)
        //Code here
        stopSelf()*/

    }
}
