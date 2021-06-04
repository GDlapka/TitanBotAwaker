package ru.titanbot.awaker;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AwakerService extends Service {
    static final int connectionInterval = 300000;
    //static final int connectionInterval = 30000;
    //public static final int RTC_WAKEUP = connectionInterval;
    public int times = 0;

    public static final int NOTIFICATION_ID = 6678;

    private static final String TAG = AwakerService.class.getName();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "Service started!");
        // TODO: тут делаем всё что нам хочется...
        final Handler handler = new Handler();

        handler.post(new Runnable() {
            @Override
            public void run() {
                PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
                PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag");

                wakeLock.acquire();
                times++;

                sendGet();

                handler.postDelayed(this, connectionInterval);
            }
        });

        return super.onStartCommand(intent, flags, startId);
    }

    public static boolean isRunning(Context ctx) {
        ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (AwakerService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "Service stopped!");
    }

    public static class Alarm extends BroadcastReceiver {

        public static final String ALARM_EVENT = "net.multipi.ALARM";
        public static final int ALARM_INTERVAL_SEC = 5;

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "Alarm received: "+intent.getAction());
            if (!isRunning(context)) {
                context.startService(new Intent(context, AwakerService.class));
            }
            else {
                Log.v(TAG, "don't start service: already running...");
            }
        }

        public static void setAlarm(Context context) {
            AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent pi = PendingIntent.getBroadcast(context, 0, new Intent(ALARM_EVENT), 0);
            am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * ALARM_INTERVAL_SEC, pi);
        }

        public static void cancelAlarm(Context context) {
            PendingIntent sender = PendingIntent.getBroadcast(context, 0, new Intent(ALARM_EVENT), 0);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(sender);
        }
    }

    public void sendGet()
    {

        new Thread(new Runnable() {
            @Override
            public void run() {
                URL url;
                HttpURLConnection connection = null;

                try {
                    url = new URL("https://titanbot.ru/awaker/index.php");
                    connection = (HttpURLConnection) url.openConnection();
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                    String result = br.readLine();
                    showAlarm(result);
                    //Log.d("HTTP-GET", line);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    public void showAlarm(String message){
        //Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Uri alarmSound =Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.red_code);
        int icon;
        //if (isRedStatus) icon = R.drawable.icon3; else icon = R.drawable.icon4;
        String firstTwo = message.substring(0, 2);
        NotificationCompat.Builder builder;

        if (firstTwo.equals("ok")) {
            builder =
                    new NotificationCompat.Builder(getBaseContext())
                            .setSmallIcon(R.drawable.icon4)
                            .setContentTitle("Working...")
                            .setContentText("Everything is ok...")
                            .setAutoCancel(false)
                            .setPriority(Notification.PRIORITY_LOW)
                            .setVibrate(null);
                            //.setDefaults(Notification.DEFAULT_VIBRATE);

            builder.setSound(null);
        } else {
            //RED CODE
            builder =
                    new NotificationCompat.Builder(getBaseContext())
                            .setSmallIcon(R.drawable.icon3)
                            .setContentTitle("Red Code!")
                            .setContentText(message)
                            .setAutoCancel(false)
                            .setPriority(Notification.PRIORITY_MAX)
                            .setDefaults(Notification.DEFAULT_VIBRATE);

            builder.setSound(alarmSound);
        }

        Notification notification = builder.build();
        //notification.defaults |= Notification.DEFAULT_SOUND;

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}
