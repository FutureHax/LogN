package com.t3hh4xx0r.tapn.models;

import java.util.List;
import java.util.regex.Pattern;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.util.Patterns;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.t3hh4xx0r.tapn.SettingsProvider;
import com.t3hh4xx0r.tapn.models.ParseInstallObject.InstallObject;

public class VerificationCode {
	String code;
	public String attachedUser;
	public String type = "simple";

	public static final String FULL = "full";
	public static final String SIMPLE = "simple";

	public interface CodeValidationListener {
		void onSuccess(boolean isValid, VerificationCode code);

		void onFailure();
	}

	public static void validateCode(final Context c, final String code,
			final CodeValidationListener listener) {
		ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(
				"VerificationCode");
		query.whereEqualTo("code", code.trim().toLowerCase() + " ");
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> res, ParseException e) {
				if (e == null) {
					VerificationCode vCode = new VerificationCode();
					if (res.isEmpty()) {
						listener.onSuccess(false, vCode);
					} else {
						vCode.code = code;
						String email = getPrimaryEmail(c);
						if (!res.get(0).has("attachedUser")) {
							res.get(0).put("attachedUser", email);
							res.get(0).saveInBackground();
							vCode.attachedUser = email;
							if (res.get(0).has("type")) {
								vCode.type = res.get(0).getString("type");
							}
							listener.onSuccess(true, vCode);
						} else {
							vCode.attachedUser = res.get(0).getString(
									"attachedUser");
							if (res.get(0).has("type")) {
								vCode.type = res.get(0).getString("type");
							}
							InstallObject obj = new SettingsProvider(c)
									.getInstallObject();
							if (vCode.attachedUser.equalsIgnoreCase(obj
									.getPrimaryEmail())) {
								listener.onSuccess(true, vCode);
							} else {
								listener.onSuccess(false, vCode);
							}
						}
					}
				} else {
					listener.onFailure();
				}
			}
		});
	}

	public static String getPrimaryEmail(Context c) {
		Pattern emailPattern = Patterns.EMAIL_ADDRESS;
		Account[] accounts = AccountManager.get(c).getAccounts();
		if (accounts[0] != null) {
			if (emailPattern.matcher(accounts[0].name).matches()) {
				return accounts[0].name;
			}
		}
		return "";
	}

	@Override
	public String toString() {
		return "VerificationCode [code=" + code + ", attachedUser="
				+ attachedUser + "]";
	}
}
