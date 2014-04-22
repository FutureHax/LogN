package com.t3hh4xx0r.tapn;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.PushService;
import com.t3hh4xx0r.tapn.activities.MainActivity;
import com.t3hh4xx0r.tapn.models.ParseInstallObject;

public class UberApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		Parse.initialize(this, "FwJZFLtycJHlcbybHzXDPWysNkEk23fDvjlBBteK",
				"WzQKaeztbx6IxzO6vtCedWJ8FqbOH5DWPbV2pIvb");
		PushService.setDefaultPushCallback(this, MainActivity.class);
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				ParseInstallation installation = ParseInstallation
						.getCurrentInstallation();
				if (installation != null) {
					try {
						installation.saveInBackground();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		t.start();
	}
}
