package com.t3hh4xx0r.tapn.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.t3hh4xx0r.tapn.MyAccessibilityService;
import com.t3hh4xx0r.tapn.R;
import com.t3hh4xx0r.tapn.SettingsProvider;
import com.t3hh4xx0r.tapn.models.Identity;

public class AddIdentityActivity extends Activity {
	private NfcAdapter mAdapter;
	private PendingIntent mPendingIntent;
	boolean watchingNFC = false;

	public void onNewIntent(Intent intent) {
		resolveIntent(intent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_write) {
			actionDone();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.new_identity, menu);
		return true;
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
				|| intent.getByteArrayExtra(NfcAdapter.EXTRA_ID) == null
				|| !watchingNFC) {
			return;
		}

		Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		PlaceholderFragment f = (PlaceholderFragment) getFragmentManager()
				.findFragmentByTag("id");

		Intent result = new Intent();
		result.putExtra("data", f.getIdentity());
		result.putExtra("id", ByteArrayToHexString(tag.getId()));
		setResult(RESULT_OK, result);
		finish();
	}

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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_identity);

		mAdapter = NfcAdapter.getDefaultAdapter(this);
		mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		resolveIntent(getIntent());

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment(), "id")
					.commit();
		}
	}

	public static class PlaceholderFragment extends Fragment {
		EditText user, pass, name;

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_add_identity,
					container, false);

			name = (EditText) rootView.findViewById(R.id.name);
			user = (EditText) rootView.findViewById(R.id.user);
			pass = (EditText) rootView.findViewById(R.id.pass);
			pass.setOnEditorActionListener(new OnEditorActionListener() {
				@Override
				public boolean onEditorAction(TextView v, int actionId,
						KeyEvent event) {
					if (actionId == EditorInfo.IME_ACTION_DONE) {
						((AddIdentityActivity) getActivity()).actionDone();
					}
					return false;
				}
			});
			return rootView;
		}

		public Identity getIdentity() {
			return new Identity(name.getText().toString(), user.getText()
					.toString(), pass.getText().toString());
		}
	}

	public void actionDone() {
		watchingNFC = true;
		AlertDialog.Builder b = new Builder(this);
		b.setTitle("Tap a tag");
		b.setMessage("Tap a tag you wish to give access to this identity. No other tags will have the ability to access this data. If you wish to use multiple tags per identity, please create seperate identities for each.");
		b.show();
	}

}
