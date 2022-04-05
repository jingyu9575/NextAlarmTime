package com.thucfb.qw.nextalarmtime;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends Activity {

	@SuppressLint("SimpleDateFormat")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			long currentTime = System.currentTimeMillis();
			long alarmTime = getAlarmTime(this);
			if (alarmTime != 0) {
				long diff = alarmTime - currentTime;
				long diffMinutes = (diff + 30000) / 60000;
				long diffDays = diffMinutes / 60 / 24;
				long diffHours = (diffMinutes / 60) % 24;
				diffMinutes = diffMinutes % 60;

				Date alarmDate = new Date(alarmTime);
				String alarmStr = java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT).format(alarmDate);
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

	public static long getAlarmTime(Context context) {
		if (isMIUI(context))
			return getMIUIAlarmTime(context);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		AlarmManager.AlarmClockInfo info = alarmManager.getNextAlarmClock();
		if (info != null) return info.getTriggerTime();
		return 0;
	}

	private static boolean isIntentResolved(Context ctx, Intent intent) {
		return (intent != null && ctx.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null);
	}

	public static boolean isMIUI(Context ctx) {
		return isIntentResolved(ctx, new Intent("miui.intent.action.OP_AUTO_START").addCategory(Intent.CATEGORY_DEFAULT))
				|| isIntentResolved(ctx, new Intent().setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")))
				|| isIntentResolved(ctx, new Intent("miui.intent.action.POWER_HIDE_MODE_APP_LIST").addCategory(Intent.CATEGORY_DEFAULT))
				|| isIntentResolved(ctx, new Intent().setComponent(new ComponentName("com.miui.securitycenter", "com.miui.powercenter.PowerSettings")));
	}

	public static long getMIUIAlarmTime(Context context) {
		String formatted = Settings.System.getString(context.getContentResolver(), "next_alarm_clock_formatted");
		if (formatted == null || formatted.isEmpty())
			return 0;
		Locale locale = Locale.getDefault();
		SimpleDateFormat df = new SimpleDateFormat(DateFormat.getBestDateTimePattern(
				locale, DateFormat.is24HourFormat(context) ? "EHm" : "Ehma"), locale);
		Calendar dayAndTime;
		try {
			Date date = df.parse(formatted);
			if (date == null) return 0;
			dayAndTime = Calendar.getInstance();
			dayAndTime.setTime(date);
		} catch (ParseException e) {
			return 0;
		}
		Calendar result = Calendar.getInstance();
		result.add(Calendar.DATE, (dayAndTime.get(Calendar.DAY_OF_WEEK) - result.get(Calendar.DAY_OF_WEEK) + 7) % 7);
		result.set(Calendar.HOUR_OF_DAY, dayAndTime.get(Calendar.HOUR_OF_DAY));
		result.set(Calendar.MINUTE, dayAndTime.get(Calendar.MINUTE));
		result.set(Calendar.SECOND, 0);
		result.set(Calendar.MILLISECOND, 0);
		return result.getTimeInMillis() + (result.getTime().before(new Date()) ? 7 * 24 * 3600L * 1000L : 0);
	}

}
