package com.t3hh4xx0r.tapn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.t3hh4xx0r.tapn.activities.MainActivity;
import com.t3hh4xx0r.tapn.models.ParseInstallObject;
import com.t3hh4xx0r.tapn.models.ParseInstallObject.InstallObject;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		final SettingsProvider sp = new SettingsProvider(context);

		InstallObject install = sp.getInstallObject();
		if (install == null || install.getFirstInstall() == -1) {
			ParseInstallObject.createInstall(context,
					new ParseInstallObject.IParseInstallFinished() {
						@Override
						public void finished(ParseInstallObject obj) {
							sp.setParseInstallObject(obj);
						}
					});
		} else {
			ParseInstallObject.checkNukedStatus(context, install);
		}
	}
}