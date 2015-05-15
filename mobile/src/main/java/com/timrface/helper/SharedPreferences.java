package com.timrface.helper;

import android.app.Activity;
import android.content.Context;

public class SharedPreferences extends Activity {

	public static boolean getBoolean(final String Name,
			final boolean DefaultValue, final Context context) {
		boolean value;

		final android.content.SharedPreferences prefs = context
				.getApplicationContext().getSharedPreferences("MyPreferences",
						0);
		value = prefs.getBoolean(Name, DefaultValue);
		return value;
	}

	public static int getInteger(final String Name, final int DefaultValue,
			final Context context) {
		int value;

		final android.content.SharedPreferences prefs = context
				.getApplicationContext().getSharedPreferences("MyPreferences",
						0);
		value = prefs.getInt(Name, DefaultValue);
		return value;
	}

	public static String getString(final String Name,
			final String DefaultValue, final Context context) {
		String value;

		final android.content.SharedPreferences prefs = context
				.getApplicationContext().getSharedPreferences("MyPreferences",
						0);
		value = prefs.getString(Name, DefaultValue);
		return value;
	}

	public static void saveBoolean(final String Name, final boolean Value,
			final Context context) {
		final android.content.SharedPreferences prefs = context
				.getApplicationContext().getSharedPreferences("MyPreferences",
						0);
		final android.content.SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(Name, Value);
		editor.commit();
	}

	public static void saveInteger(final String Name, final int Value,
			final Context context) {
		final android.content.SharedPreferences prefs = context
				.getApplicationContext().getSharedPreferences("MyPreferences",
						0);
		final android.content.SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(Name, Value);
		editor.commit();
	}

	public static void saveString(final String Name, final String Value,
			final Context context) {
		final android.content.SharedPreferences prefs = context
				.getApplicationContext().getSharedPreferences("MyPreferences",
						0);
		final android.content.SharedPreferences.Editor editor = prefs.edit();
		editor.putString(Name, Value);
		editor.commit();
	}
}
