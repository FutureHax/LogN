package com.t3hh4xx0r.tapn.models;

import java.text.DecimalFormat;
import java.util.List;
import java.util.regex.Pattern;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.util.Log;
import android.util.Patterns;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.t3hh4xx0r.tapn.SettingsProvider;

public class ParseInstallObject {
	private IParseInstallFinished parseInstallListener;
	private Context c;
	InstallObject installObject;

	private static final String PRIMARY_EMAIL = "primaryEmail";
	private static final int MINUTE = 60000;
	private static final int DAY = 86400000;

	public ParseInstallObject(Context c) {
		this.c = c;
		installObject = new InstallObject();
	}

	public ParseInstallObject() {
		// TODO Auto-generated constructor stub
	}

	public static void createInstall(Context c, IParseInstallFinished listener) {
		ParseInstallObject o = new ParseInstallObject(c);

		Pattern emailPattern = Patterns.EMAIL_ADDRESS;
		Account[] accounts = AccountManager.get(c).getAccounts();
		if (accounts[0] != null) {
			if (emailPattern.matcher(accounts[0].name).matches()) {
				o.installObject.setPrimaryEmail(accounts[0].name);
			}
		}
		o.setParseInstallListener(listener);
		o.getInstallByEmails();
	}

	public IParseInstallFinished getParseInstallListener() {
		return parseInstallListener;
	}

	public void setParseInstallListener(
			IParseInstallFinished parseInstallListener) {
		this.parseInstallListener = parseInstallListener;
	}

	private void getInstallByEmails() {
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Install");
		query.whereEqualTo(PRIMARY_EMAIL, installObject.primaryEmail);
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> results, ParseException e) {
				if (e == null) {
					if (results.isEmpty()) {
						createNewInstall();
					} else {
						updateInstallFromRemote(results.get(0));
					}
				} else {
					e.printStackTrace();
				}
			}
		});
	}

	protected void createNewInstall() {
		ParseObject install = toParseObject();
		install.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException arg0) {
				parseInstallListener.finished(ParseInstallObject.this);
			}
		});
	}

	private ParseObject toParseObject() {
		ParseObject install = new ParseObject("Install");
		install.put(PRIMARY_EMAIL, installObject.primaryEmail);
		return install;
	}

	protected void updateInstallFromRemote(ParseObject install) {
		installObject.firstInstall = install.getCreatedAt().getTime();
		installObject.primaryEmail = install.getString(PRIMARY_EMAIL);
		checkNukedStatus(c, installObject);
		parseInstallListener.finished(this);
		
	}

	public static void checkNukedStatus(Context c,
			ParseInstallObject.InstallObject installObject) {
		long now = System.currentTimeMillis();
		Log.d("TIME SINCE INSTALL",
				Long.toString(now - installObject.firstInstall));
		if ((now - installObject.firstInstall) > (DAY * 7)) {
//			 if ((now - installObject.firstInstall) > MINUTE) {
			handleNukedInstall(true, c, installObject);
		} else {
			handleNukedInstall(false, c, null);
		}
	}

	private static void handleNukedInstall(boolean nuked, Context c,
			ParseInstallObject.InstallObject installObject) {
		if (!nuked) {
			new SettingsProvider(c).setAppNuked(false, "");
			return;
		}

		long now = System.currentTimeMillis();
		DecimalFormat f = new DecimalFormat("#");
		String mod = " mins ago.";
		long nukedFor = (now - installObject.firstInstall) / (60000 * 1);
		if (nukedFor > 60) {
			mod = " hours ago.";
			nukedFor = nukedFor / 60;
			if (nukedFor > 24) {
				mod = " days ago.";
				nukedFor = nukedFor / 24;
			}
		}
		new SettingsProvider(c).setAppNuked(true,
				"Your demo expired " + f.format(nukedFor) + mod);
		
	}

	public interface IParseInstallFinished {
		void finished(ParseInstallObject object);
	}

	public InstallObject getInstallObject() {
		return installObject;
	}

	public class InstallObject {
		private String primaryEmail;
		private long firstInstall = -1;

		public String getPrimaryEmail() {
			return primaryEmail;
		}

		@Override
		public String toString() {
			return "InstallObject [getPrimaryEmail()=" + getPrimaryEmail()
					+ ", getFirstInstall()=" + getFirstInstall() + "]";
		}

		public void setPrimaryEmail(String primaryEmail) {
			this.primaryEmail = primaryEmail;
		}

		public long getFirstInstall() {
			return firstInstall;
		}

		public void setFirstInstall(long firstInstall) {
			this.firstInstall = firstInstall;
		}
	}

}
