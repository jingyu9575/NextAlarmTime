package com.thucfb.qw.nextalarmtime;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

public class MainActivity extends Activity {

    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            assert alarmManager != null;
            long currentTime = System.currentTimeMillis();
            AlarmManager.AlarmClockInfo info = alarmManager.getNextAlarmClock();
            if (info != null) {
                long diff = info.getTriggerTime() - currentTime;
                long diffMinutes = (diff + 30000) / 60000;
                long diffDays = diffMinutes / 60 / 24;
                long diffHours = (diffMinutes / 60) % 24;
                diffMinutes = diffMinutes % 60;

                Date alarmDate = new Date(info.getTriggerTime());
                String alarmStr = DateFormat.getTimeInstance(DateFormat.SHORT).format(alarmDate);
                if (diffDays > 0)
                    alarmStr = new SimpleDateFormat("EEE").format(alarmDate) + " " + alarmStr;

                String diffStr = getResources().getQuantityString(R.plurals.x_minutes,
                        (int) diffMinutes, (int) diffMinutes);
                if (diffHours > 0 || diffDays > 0) {
                    diffStr = getResources().getQuantityString(R.plurals.x_hours,
                            (int) diffHours, (int) diffHours) + " " + diffStr;
                    if (diffDays > 0)
                        diffStr = getResources().getQuantityString(R.plurals.x_days,
                                (int) diffDays, (int) diffDays) + " " + diffStr;
                }
                Toast.makeText(this,
                        getString(R.string.alarm_time, alarmStr, diffStr),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.no_alarm, Toast.LENGTH_SHORT).show();
            }
        } finally {
            finish();
        }
    }
}
