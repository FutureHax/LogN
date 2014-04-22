package com.t3hh4xx0r.tapn;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.t3hh4xx0r.tapn.models.ParseInstallObject;

public class SettingsProvider {
	Context c;

	public SettingsProvider(Context c) {
		this.c = c;
	}

	public void setAppNuked(boolean i, String m) {
		PreferenceManager.getDefaultSharedPreferences(c).edit()
				.putBoolean("app_nuked", i).apply();
		PreferenceManager.getDefaultSharedPreferences(c).edit()
				.putString("app_nuked_message", m).apply();
	}

	public boolean getHasFullUpgrade() {
		return PreferenceManager.getDefaultSharedPreferences(c).getBoolean(
				"has_full", false);
	}
	
	public void setHasFullUpgrade(boolean b) {
		PreferenceManager.getDefaultSharedPreferences(c).edit()
				.putBoolean("has_full", b).apply();
	}
	
	public boolean getAppIsNuked() {
		if (getHasFullUpgrade()) {
			setAppNuked(false, getAppNukedMessage());
			return false;
		} else {
			return PreferenceManager.getDefaultSharedPreferences(c).getBoolean(
					"app_nuked", false);
		}
	}
	
	public String getAppNukedMessage() {
		return PreferenceManager.getDefaultSharedPreferences(c).getString(
				"app_nuked_message", "Nuked brah!");
	}
	
	public void setParseInstallObject(ParseInstallObject obj) {
		Gson g = new Gson();
		String toStringValue = g.toJson(obj.getInstallObject());
		Editor e = PreferenceManager.getDefaultSharedPreferences(c).edit();
		e.putString("parse_install", toStringValue);
		e.commit();
	}

	public ParseInstallObject.InstallObject getInstallObject() {
		Gson g = new Gson();
		return g.fromJson(PreferenceManager.getDefaultSharedPreferences(c)
				.getString("parse_install", ""),
				ParseInstallObject.InstallObject.class);
	}
}
