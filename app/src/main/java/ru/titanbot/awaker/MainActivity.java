package ru.titanbot.awaker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Activity;
import android.graphics.Color;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends Activity implements View.OnClickListener{
    private ToggleButton mTb;

    public void setActivityBackgroundColor(int color) {
        View view = this.getWindow().getDecorView();
        view.setBackgroundColor(color);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = new LinearLayout(this);
        root.setGravity(Gravity.CENTER);
        mTb = new ToggleButton(this);
        mTb.setOnClickListener(this);
        mTb.setTextOff("Disabled");
        mTb.setTextOn("Enabled");
        root.addView(mTb,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        setContentView(root, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));

        setActivityBackgroundColor(android.graphics.Color.rgb(0,0,0));

    }

    @Override
    protected void onResume() {
        super.onResume();
        mTb.setChecked(AwakerService.isRunning(this));
    }

    @Override
    public void onClick(View view) {
        if (AwakerService.isRunning(this)) {
            AwakerService.Alarm.cancelAlarm(this);
            stopService(new Intent(this, AwakerService.class));
            mTb.setChecked(false);
        } else {
            AwakerService.Alarm.setAlarm(this);
            startService(new Intent(this, AwakerService.class));
            mTb.setChecked(true);
        }
    }
}