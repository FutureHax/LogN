package com.t3hh4xx0r.tapn;

import org.json.JSONException;
import org.json.JSONObject;

import com.t3hh4xx0r.tapn.activities.NFCTapToInputActivity;

import android.accessibilityservice.AccessibilityService;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class MyAccessibilityService extends AccessibilityService {
	public static MyAccessibilityService sSharedInstance;
	AccessibilityNodeInfo source;
	String lastClip = "";
	ClipboardManager clipboard;

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		if (new SettingsProvider(this).getAppIsNuked()) {
			return;
		}
		source = event.getSource();

		if (source != null) {
			if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_LONG_CLICKED
					&& source.getClassName().equals("android.widget.EditText")) {
				Intent i = new Intent(this, NFCTapToInputActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_CLEAR_TASK);
				startActivity(i);
			}
		}
	}

	void setData() {
		Log.d("SENDING DATA", Boolean.toString(source.refresh()));
		Log.d("SENDING DATA", Boolean.toString(source
				.performAction(AccessibilityNodeInfo.ACTION_PASTE)));
		ClipData clip = ClipData.newPlainText("nfc_input", lastClip);
		clipboard.setPrimaryClip(clip);
	}

	public void inputData(Context c, String data) throws JSONException {
		try {
			lastClip = clipboard.getPrimaryClip().getItemAt(0).coerceToText(c)
					.toString();
		} catch (Exception e) {
			lastClip = "";
		}
		Log.d("THE NODE INFO", source.toString());
		try {
			JSONObject identityPayload = new JSONObject(data);
			if (source.isPassword()) {
				data = identityPayload.getString("pass");
			} else {
				data = identityPayload.getString("user");
			}
		} catch (Exception e) {

		}

		ClipData clip = ClipData.newPlainText("nfc_input", data);
		clipboard.setPrimaryClip(clip);

		setData();
	}

	@Override
	public void onInterrupt() {

	}

	public static MyAccessibilityService getSharedInstance() {
		return sSharedInstance;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		sSharedInstance = null;
		return false;
	}

	@Override
	protected void onServiceConnected() {
		super.onServiceConnected();
		sSharedInstance = this;
		clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
	}

}