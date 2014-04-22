package com.t3hh4xx0r.tapn.activities;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.t3hh4xx0r.tapn.MyAccessibilityService;
import com.t3hh4xx0r.tapn.models.Identity;
import com.t3hh4xx0r.tapn.nfc_actions.NdefReaderTask;

public class NFCTapToInputActivity extends Activity {
	private NfcAdapter mAdapter;
	private PendingIntent mPendingIntent;
	String payload = "";
	boolean doInput = true;

	public static final int ACTIVATION_REQUEST = 1;

	static String ByteArrayToHexString(byte[] inarray) {
		int i, j, in;
		String[] hex = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A",
				"B", "C", "D", "E", "F" };
		String out = "";

		for (j = 0; j < inarray.length; ++j) {
			in = (int) inarray[j] & 0xff;
			i = (in >> 4) & 0x0f;
			out += hex[i];
			i = in & 0x0f;
			out += hex[i];
		}
		return out;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAdapter = NfcAdapter.getDefaultAdapter(this);
		mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		resolveIntent(getIntent());

		TextView tap = new TextView(this);
		tap.setText("Tap your tag to sign in, or hit the back button to do a simple paste.");
		tap.setTextColor(Color.WHITE);
		tap.setTextSize(18);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		tap.setGravity(Gravity.CENTER);
		tap.setLayoutParams(lp);
		setContentView(tap);
	}

	public void onNewIntent(Intent intent) {
		resolveIntent(intent);
	}

	@Override
	public void onBackPressed() {
		doInput = false;
		super.onBackPressed();
	}

	public void onPause() {
		super.onPause();
		mAdapter.disableForegroundDispatch(this);
	}

	public void onResume() {
		super.onResume();
		mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
		resolveIntent(getIntent());
	}

	private void resolveIntent(Intent intent) {
		if (intent == null
				|| intent.getByteArrayExtra(NfcAdapter.EXTRA_ID) == null) {
			Log.d("RETURNIG", "NULL DATA OR INTENT");
			return;
		}

		final Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		new NdefReaderTask(new NdefReaderTask.OnPayloadParsedListener() {
			@Override
			public void onPayloadParsed(String payload) {
				try {
					JSONObject json = new JSONObject(payload);
					Identity id = new Identity(NFCTapToInputActivity.this,
							json, ByteArrayToHexString(tag.getId()));
					NFCTapToInputActivity.this.payload = id.toPayload();
				} catch (JSONException e) {
					e.printStackTrace();
					NFCTapToInputActivity.this.payload = ByteArrayToHexString(tag.getId());
				}
				finish();
			}
		}, tag).execute();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (doInput) {
			try {
				MyAccessibilityService.sSharedInstance.inputData(this, payload);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			MyAccessibilityService.sSharedInstance.cancelInput(this);
		}
	}

}
