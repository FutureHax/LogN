package com.t3hh4xx0r.tapn.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.t3hh4xx0r.tapn.MyAccessibilityService;
import com.t3hh4xx0r.tapn.R;

public class IntroActivity extends FragmentActivity {
	ViewPager mPager;
	SectionsPagerAdapter mAdapter;

	@Override
	protected void onDestroy() {
		super.onDestroy();
		startActivity(new Intent(this, MainActivity.class)); 
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_intro);

		mAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setAdapter(mAdapter);

	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			return IntroFragment.newInstance(position);
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return getString(R.string.title_1);
			case 1:
				return getString(R.string.title_2);
			case 2:
				return getString(R.string.title_3);
			}
			return null;
		}
	}

	public static class IntroFragment extends Fragment {
		View root;
		int pos;

		public IntroFragment() {
			// Required empty public constructor
		}

		public static IntroFragment newInstance(int position) {
			IntroFragment f = new IntroFragment();
			Bundle b = new Bundle();
			b.putInt("pos", position);
			f.setArguments(b);
			return f;
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

		}

		@Override
		public void onResume() {
			super.onResume();
			if (pos == 2) {
				TextView done = (TextView) root.findViewById(R.id.done);
				if (MyAccessibilityService.sSharedInstance == null) {
					done.setText("Enable");
				} else {
					done.setText("Ok, let's go.");
				}
			}
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			root = inflater.inflate(R.layout.fragment_intro_simple, container,
					false);
			pos = getArguments().getInt("pos");

			TextView title = (TextView) root.findViewById(R.id.title);
			title.setText(((IntroActivity) getActivity()).mAdapter
					.getPageTitle(pos));
			String details = "";
			if (pos == 0) {
				root.findViewById(R.id.swipe).setVisibility(View.VISIBLE);

				details = "TapN is a new way to use NFC. TapN allows you to log in to any app on your Android device with a simple NFC tap. No more passwords, no more password managers, just simply tap and go.";
			} else if (pos == 1) {
				details = "TapN encrypts your \"identity\" with a key comprised of data from both your NFC tag and your device. This means that if your tag is compromised, the data on it is useless without the original device that wrote the data. If the device is compromised, the saved \"identities\" on the device are useless without the tag that was used to create them. At no point is all of the necessary data to unencrypt your \"identity\" ever in one place.\n\n"
						+ "TapN is launched when you long press on a text field, prompting you to tap your NFC tag. If an \"identity\" is written to the tag, the app detects if the field is a password or not, and inputs either the password or username you have saved. \n\nIf no \"identity\" is found, it inputs the tag UID. This allows you to use the tag to log in without writing any data to it - useful if you already have important content on your tag, but still wish to use the service. In this case, simply set your password to the app you wish to log into to the tags UID. This is much less secure however and is not the recommended behavior.";
			} else if (pos == 2) {
				details = "To get started, enable the TapN Accessibility Service by clicking the button below. Once enabled, hit the back button to return here. Please note that is MUST be enabled for the app to function. If not enabled, the app will never launch when you long press an input field.\n\nOnce enabled, create a new \"identity\" by selecting the icon in the upper right. After creating the \"identity\", write it to your tag by clicking on the card from the list. Once written to a tag, simply long press in any text field and tap your tag.\n\nThe rest is magic!";
				TextView done = (TextView) root.findViewById(R.id.done);
				if (MyAccessibilityService.sSharedInstance == null) {
					done.setText("Enable");
				}
				root.findViewById(R.id.done_wrapper)
						.setVisibility(View.VISIBLE);
				View skip = root.findViewById(R.id.skip);
				if (MyAccessibilityService.sSharedInstance != null) {
					skip.setVisibility(View.GONE);
				}
				done.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if (MyAccessibilityService.sSharedInstance == null) {
							launchSettings();
						} else {
							getActivity().finish();
						}
					}
				});
				skip.setOnClickListener(
						new OnClickListener() {
							@Override
							public void onClick(View arg0) {
								getActivity().finish();
							}
						});
			}
			TextView content = (TextView) root.findViewById(R.id.content);
			content.setText(details);

			return root;
		}

		void launchSettings() {
			Intent intent = new Intent(
					android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
			startActivityForResult(intent, 0);
		}
	}

}
