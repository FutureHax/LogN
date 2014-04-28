package com.t3hh4xx0r.tapn.activities;

import java.util.ArrayList;

import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.t3hh4xx0r.tapn.DBAdapter;
import com.t3hh4xx0r.tapn.IdentityListAdapter;
import com.t3hh4xx0r.tapn.MyAccessibilityService;
import com.t3hh4xx0r.tapn.R;
import com.t3hh4xx0r.tapn.SettingsProvider;
import com.t3hh4xx0r.tapn.SwipeDismissListViewTouchListener;
import com.t3hh4xx0r.tapn.SwipeDismissListViewTouchListener.DismissCallbacks;
import com.t3hh4xx0r.tapn.billing.IabHelper;
import com.t3hh4xx0r.tapn.billing.IabResult;
import com.t3hh4xx0r.tapn.billing.Inventory;
import com.t3hh4xx0r.tapn.billing.Purchase;
import com.t3hh4xx0r.tapn.models.Identity;
import com.t3hh4xx0r.tapn.models.ParseInstallObject;
import com.t3hh4xx0r.tapn.models.ParseInstallObject.InstallObject;
import com.t3hh4xx0r.tapn.models.VerificationCode;
import com.t3hh4xx0r.tapn.models.VerificationCode.CodeValidationListener;
import com.t3hh4xx0r.tapn.nfc_actions.NFCWriterTask;
import com.t3hh4xx0r.tapn.nfc_actions.NFCWriterTask.OnPayloadWrittenListener;

public class MainActivity extends Activity {
	private NfcAdapter mAdapter;
	private PendingIntent mPendingIntent;
	IdentityListAdapter listAdapter;
	ArrayList<Identity> list;
	public int WAITING_FOR_DECRYPT = 0;
	int WAITING_FOR_WRITE = 1;
	private int NFC_STATE = -1;
	private Identity identityToDecrypt;
	private int identityToDecryptPosition;
	AlertDialog readPrompt;
	AlertDialog writePrompt;
	Identity idToWrite;

	public final static String TAG = "BILLING";
	public final static String SKU_FULL = "tapn_unlocker";
	public IabHelper mHelper;

	public void deleteListItem(final int pos) {
		AlertDialog.Builder b = new Builder(new ContextThemeWrapper(
				MainActivity.this, R.style.AlertDialogCustom));
		b.setTitle("Are you sure?");
		b.setMessage("Are you sure you want to delete this identity?");
		b.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				DBAdapter db = new DBAdapter(MainActivity.this);
				db.open();
				Log.d("DID DELETE SUCCEED?", Boolean.toString(db
						.deleteIdentity(listAdapter.getItem(pos))));
				listAdapter.update(db.getIdentities());
				db.close();
			}
		});
		b.setNegativeButton("No", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				DBAdapter db = new DBAdapter(MainActivity.this);
				db.open();
				listAdapter.update(db.getIdentities());
				db.close();
			}
		});

		Dialog d = b.show();
		int dividerId = d.getContext().getResources()
				.getIdentifier("android:id/titleDivider", null, null);
		View divider = d.findViewById(dividerId);
		divider.setBackgroundColor(getResources().getColor(R.color.app_base));
	}

	public final static String KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAliewbuei96AAYXlCPyMJ7pgK+bN075EavWpbeVwsfQ0pUBJyd4a8YseykGRNaqfyqnjEjDFXbbg2+P/7u7a0uHsh66b+YvnTkrWGXcd2SBwhjnwGJWsehccjiNNnMQ7tt5wYgskhoyuzcW8jcCnP/TQVWAEW85WonWmoERMTGf4Qf1xRiVs2M89Cfz3F4eloH92y0a3czOtxTbVNr/qSKyV14HtGvlTRBx1u9bK2JXgef20MBPBUj9jFRiBfd0P4u6Q74CqtPZDGINXhAmRKOxYuYpd6238YkSr2h+309K71TzB6/cnlUMAMF44hh5kR2uGQSpNF/7nDXFOztXoo1wIDAQAB";

	private void startFullUpgrade() {
		mHelper.launchPurchaseFlow(this, SKU_FULL, 10001,
				mPurchaseFinishedListener, "");
	}

	public IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
		public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
			if (mHelper == null)
				return;
			if (result.isFailure()) {
				Log.d(TAG, "Error purchasing: " + result);
				return;
			} else if (purchase.getSku().equals(SKU_FULL)) {
				new SettingsProvider(MainActivity.this).setHasFullUpgrade(true);
			}

			AlertDialog.Builder b = new Builder(new ContextThemeWrapper(
					MainActivity.this, R.style.AlertDialogCustom));
			b.setTitle("Purchase Confirmed");
			b.setIcon(R.drawable.ic_launcher);
			b.setMessage("Please restart the app.");
			b.setPositiveButton("Restart", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent i = getBaseContext().getPackageManager()
							.getLaunchIntentForPackage(
									getBaseContext().getPackageName());
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(i);
				}
			});
			Dialog d = b.show();
			int dividerId = d.getContext().getResources()
					.getIdentifier("android:id/titleDivider", null, null);
			View divider = d.findViewById(dividerId);
			divider.setBackgroundColor(getResources()
					.getColor(R.color.app_base));
		}
	};

	IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
		public void onQueryInventoryFinished(IabResult result,
				Inventory inventory) {

			if (mHelper == null)
				return;

			if (result.isFailure()) {
				Log.d(TAG, "Failed to query inventory: " + result);
				return;
			} else {
				Log.d(TAG, "Finished query inventory: " + result);
			}

			Purchase fullPurchase = inventory.getPurchase(SKU_FULL);
			final boolean hasFull = (fullPurchase != null);

			Runnable r = new Runnable() {
				@Override
				public void run() {
					if (MainActivity.this == null) {
						new Handler().postDelayed(this, 500);
						return;
					}
					if (!new SettingsProvider(MainActivity.this)
							.getHasFullUpgrade()) {
						new SettingsProvider(MainActivity.this)
								.setHasFullUpgrade(hasFull);
					}
				}
			};

			r.run();
		}
	};

	public static int getAppVersionCode(Context c) {
		try {
			PackageInfo packageInfo = c.getPackageManager().getPackageInfo(
					c.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	public boolean isFirstLaunchForVersion() {
		boolean isInPrefs = PreferenceManager.getDefaultSharedPreferences(this)
				.getBoolean("seen_" + getAppVersionCode(this), false);
		if (!isInPrefs) {
			PreferenceManager.getDefaultSharedPreferences(this).edit()
					.putBoolean("seen_" + getAppVersionCode(this), true)
					.apply();
		}
		return !isInPrefs;
	}

	public void onNewIntent(Intent intent) {
		resolveIntent(intent);
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
				|| NFC_STATE == -1) {
			return;
		}
		Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		if (NFC_STATE == WAITING_FOR_WRITE) {
			String payload;
			try {
				payload = idToWrite.toPayload();
				NFCWriterTask t = new NFCWriterTask(payload, tag,
						new OnPayloadWrittenListener() {
							@Override
							public void onPayloadFinished(final boolean success) {
								if (writePrompt != null
										&& writePrompt.isShowing()) {
									writePrompt.dismiss();
								}
								MainActivity.this.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										Toast.makeText(
												MainActivity.this,
												(success ? "Identity written successfully"
														: "Failed to write identity"),
												Toast.LENGTH_LONG).show();
									}
								});
							}
						});
				t.execute();
			} catch (JSONException e) {
				e.printStackTrace();
			}

			NFC_STATE = -1;
		} else if (NFC_STATE == WAITING_FOR_DECRYPT
				&& identityToDecrypt != null) {
			String id = ByteArrayToHexString(tag.getId());
			Identity dId = Identity
					.decryptIdentity(id, this, identityToDecrypt);
			listAdapter.showIdentity(dId, identityToDecryptPosition);
			identityToDecrypt = null;
			NFC_STATE = -1;
			if (readPrompt != null && readPrompt.isShowing()) {
				readPrompt.dismiss();
			}
		}
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

	public void onItemClick(int position) {
		AlertDialog.Builder b = new Builder(new ContextThemeWrapper(this,
				R.style.AlertDialogCustom));
		b.setTitle("Tap a tag");
		b.setIcon(R.drawable.ic_nfc);
		b.setMessage("Tap a tag to write this identity to write to it.");
		writePrompt = b.create();
		writePrompt.show();
		int dividerId = writePrompt.getContext().getResources()
				.getIdentifier("android:id/titleDivider", null, null);
		View divider = writePrompt.findViewById(dividerId);
		divider.setBackgroundColor(getResources().getColor(R.color.app_base));
		NFC_STATE = WAITING_FOR_WRITE;
		idToWrite = listAdapter.getItem(position);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		setupInstall();
		setupBilling();
		if (isFirstLaunchForVersion()) {
			startActivity(new Intent(this, IntroActivity.class));
			finish();
		}
		getList();
		listAdapter = new IdentityListAdapter(list, this);
		((ListView) findViewById(android.R.id.list)).setAdapter(listAdapter);

		SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(
				((ListView) findViewById(android.R.id.list)),
				new DismissCallbacks() {
					public void onDismiss(ListView listView,
							int[] reverseSortedPositions) {
						for (int position : reverseSortedPositions) {
							deleteListItem(position);
						}
						listAdapter.notifyDataSetChanged();
					}

					@Override
					public boolean canDismiss(int position) {
						return true;
					}
				});
		((ListView) findViewById(android.R.id.list))
				.setOnTouchListener(touchListener);
		((ListView) findViewById(android.R.id.list))
				.setOnScrollListener(touchListener.makeScrollListener());
		mAdapter = NfcAdapter.getDefaultAdapter(this);
		mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		resolveIntent(getIntent());
	}

	private void setupInstall() {
		InstallObject install = new SettingsProvider(this).getInstallObject();
		if (install == null || install.getFirstInstall() == -1) {
			ParseInstallObject.createInstall(this,
					new ParseInstallObject.IParseInstallFinished() {
						@Override
						public void finished(ParseInstallObject obj) {
							new SettingsProvider(MainActivity.this)
									.setParseInstallObject(obj);
							invalidateOptionsMenu();
						}
					});
		} else {
			ParseInstallObject.checkNukedStatus(this, install);
			invalidateOptionsMenu();
		}
	}

	private void getList() {
		list = new DBAdapter(this).getIdentities();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mHelper != null) {
			mHelper.dispose();
			mHelper = null;
		}
	}

	private void setupBilling() {
		mHelper = new IabHelper(this, KEY);
		mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			public void onIabSetupFinished(IabResult result) {
				if (!result.isSuccess()) {
					Log.d(TAG, "Problem setting up In-app Billing: " + result);
				}
				if (mHelper == null)
					return;
				mHelper.queryInventoryAsync(mGotInventoryListener);
			}
		});
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (mHelper != null) {
			if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
				super.onActivityResult(requestCode, resultCode, data);
			} else {
				Log.d(TAG, "onActivityResult handled by IABUtil.");
			}
		}
		if (data != null && data.hasExtra("data")) {
			Identity id = (Identity) data.getSerializableExtra("data");
			String tagId = data.getStringExtra("id");
			Log.d("THE DATA", id.toString());
			DBAdapter db = new DBAdapter(this);
			db.insertIdentity(this, id, tagId);
			getList();
			listAdapter.update(list);
		}
	}

	protected void handleExpiredClick() {
		AlertDialog.Builder b = new Builder(new ContextThemeWrapper(this,
				R.style.AlertDialogCustom));
		b.setTitle("Demo Expired");
		b.setIcon(R.drawable.ic_gray_buy);
		b.setMessage(new SettingsProvider(this).getAppNukedMessage()
				+ " Please unlock the full app.");
		b.setNegativeButton("Via purchase", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				startFullUpgrade();
			}
		});
		b.setPositiveButton("Via code", new OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				launchRedeemFlow();
			}
		});
		Dialog d = b.show();
		int dividerId = d.getContext().getResources()
				.getIdentifier("android:id/titleDivider", null, null);
		View divider = d.findViewById(dividerId);
		divider.setBackgroundColor(getResources().getColor(R.color.app_base));
	}

	protected void handleUpgradeClick() {
		AlertDialog.Builder b = new Builder(new ContextThemeWrapper(this,
				R.style.AlertDialogCustom));
		b.setTitle("Upgrade From Demo");
		b.setIcon(R.drawable.ic_gray_buy);
		b.setMessage("Upgrading allows you to save multiple identities and removes the one week trial limit.");
		b.setNegativeButton("Via purchase", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				startFullUpgrade();
			}
		});
		b.setPositiveButton("Via code", new OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				launchRedeemFlow();
			}
		});
		Dialog d = b.show();
		int dividerId = d.getContext().getResources()
				.getIdentifier("android:id/titleDivider", null, null);
		View divider = d.findViewById(dividerId);
		divider.setBackgroundColor(getResources().getColor(R.color.app_base));
	}

	protected void launchRedeemFlow() {
		final ViewFlipper flippy = new ViewFlipper(this);
		final EditText input = new EditText(this);
		final ProgressBar pBar = new ProgressBar(this);
		flippy.addView(input);
		flippy.addView(pBar);

		AlertDialog.Builder b = new Builder(new ContextThemeWrapper(this,
				R.style.AlertDialogCustom));
		b.setIcon(R.drawable.ic_launcher);
		b.setTitle("Redeem Code");
		b.setView(flippy);
		b.setPositiveButton("Redeem", null);
		final AlertDialog d = b.create();
		d.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				Button b = d.getButton(AlertDialog.BUTTON_POSITIVE);
				b.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						launchRedeemCode(input.getText().toString(), d);
						flippy.setDisplayedChild(1);
					}
				});
			}
		});
		d.show();
		int dividerId = d.getContext().getResources()
				.getIdentifier("android:id/titleDivider", null, null);
		View divider = d.findViewById(dividerId);
		divider.setBackgroundColor(getResources().getColor(R.color.app_base));
	}

	protected void launchRedeemCode(String code, final AlertDialog d) {
		VerificationCode.validateCode(MainActivity.this, code,
				new CodeValidationListener() {
					@Override
					public void onSuccess(boolean isValid, VerificationCode code) {
						d.dismiss();
						if (isValid) {
							new SettingsProvider(MainActivity.this)
									.setHasFullUpgrade(true);

							AlertDialog.Builder b = new Builder(
									new ContextThemeWrapper(MainActivity.this,
											R.style.AlertDialogCustom));
							b.setTitle("Valid Code");
							b.setMessage("Thank you! You have now removed the demo restrictions. Please restart the app.");
							b.setPositiveButton("Restart",
									new OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											Intent i = getBaseContext()
													.getPackageManager()
													.getLaunchIntentForPackage(
															getBaseContext()
																	.getPackageName());
											i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
											startActivity(i);
										}
									});
							Dialog d = b.show();
							int dividerId = d
									.getContext()
									.getResources()
									.getIdentifier("android:id/titleDivider",
											null, null);
							View divider = d.findViewById(dividerId);
							divider.setBackgroundColor(getResources().getColor(
									R.color.app_base));
						} else {
							AlertDialog.Builder b = new Builder(
									new ContextThemeWrapper(MainActivity.this,
											R.style.AlertDialogCustom));
							b.setTitle("Invalid Code");
							InstallObject obj = new SettingsProvider(
									MainActivity.this).getInstallObject();

							if (code.attachedUser == null) {
								b.setMessage("Please try again.");
							} else {
								if (!code.attachedUser.equalsIgnoreCase(obj
										.getPrimaryEmail())) {
									b.setMessage("This code has already been redeemed by another account.");
								} else {
									b.setMessage("Please try again.");
								}
							}
							b.setPositiveButton("Dismiss",
									new OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											dialog.dismiss();
										}
									});
							Dialog d = b.show();
							int dividerId = d
									.getContext()
									.getResources()
									.getIdentifier("android:id/titleDivider",
											null, null);
							View divider = d.findViewById(dividerId);
							divider.setBackgroundColor(getResources().getColor(
									R.color.app_base));
						}
					}

					@Override
					public void onFailure() {
						d.dismiss();
					}
				});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		SettingsProvider sp = new SettingsProvider(this);
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		MenuItem itemNuked = menu.findItem(R.id.nuked);
		MenuItem itemHelp = menu.findItem(R.id.action_help);
		if (sp.getHasFullUpgrade()) {
			menu.removeItem(R.id.action_buy);
		}
		if (MyAccessibilityService.sSharedInstance == null) {
			final ImageView itemView = new ImageView(this, null,
					android.R.style.Widget_ActionButton);
			itemView.setImageResource(R.drawable.ic_action_help);
			final Animation in = AnimationUtils.loadAnimation(this,
					android.R.anim.fade_in);
			final Animation out = AnimationUtils.loadAnimation(this,
					android.R.anim.fade_out);
			itemView.startAnimation(out);
			out.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					itemView.startAnimation(in);
				}
			});
			in.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					itemView.startAnimation(out);
				}
			});
			itemHelp.setActionView(itemView);
			itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(new Intent(v.getContext(),
							IntroActivity.class));
					finish();
				}
			});
		} else {
			if (itemHelp.getActionView() != null) {
				itemHelp.setActionView(null);
			}
		}
		if (!sp.getAppIsNuked()) {
			menu.removeItem(R.id.nuked);
		} else {
			final ImageView itemView = new ImageView(this, null,
					android.R.style.Widget_ActionButton);
			itemView.setImageResource(R.drawable.ic_action_nuked);
			final Animation in = AnimationUtils.loadAnimation(this,
					android.R.anim.fade_in);
			final Animation out = AnimationUtils.loadAnimation(this,
					android.R.anim.fade_out);
			itemView.startAnimation(out);
			out.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					itemView.startAnimation(in);
				}
			});
			in.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					itemView.startAnimation(out);
				}
			});
			itemNuked.setActionView(itemView);
			itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					handleExpiredClick();
				}
			});
		}
		return true;
	}

	private void showFeedbackChooser() {
		AlertDialog.Builder b = new Builder(new ContextThemeWrapper(this,
				R.style.AlertDialogCustom));
		b.setPositiveButton("Share", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent i = new Intent(Intent.ACTION_SEND);
				i.setType("text/plain");
				i.putExtra(Intent.EXTRA_SUBJECT, R.string.app_name);
				String sAux = "Check out this amazing application! ";
				sAux = sAux
						+ "https://play.google.com/store/apps/details?id=com.t3hh4xx0r.tapn \n\n";
				i.putExtra(Intent.EXTRA_TEXT, sAux);
				startActivity(Intent.createChooser(i, "Share via..."));
			}
		});
		b.setNegativeButton("Send Feedback", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				final Intent emailIntent = new Intent(
						android.content.Intent.ACTION_SEND);
				emailIntent.setType("plain/text");
				emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
						new String[] { "r2doesinc@gmail.com.com" });
				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
						"TapN Feedback");
				startActivity(Intent.createChooser(emailIntent, "Send via..."));
			}
		});
		Dialog d = b.show();
		int dividerId = d.getContext().getResources()
				.getIdentifier("android:id/titleDivider", null, null);
		View divider = d.findViewById(dividerId);
		divider.setBackgroundColor(getResources().getColor(R.color.app_base));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_add_identity) {
			startActivityForResult(new Intent(this, AddIdentityActivity.class),
					420);
			return true;
		} else if (id == R.id.action_help) {
			startActivity(new Intent(this, IntroActivity.class));
			finish();
			return true;
		} else if (id == R.id.action_feedback) {
			showFeedbackChooser();
			return true;
		} else if (id == R.id.action_buy) {
			handleUpgradeClick();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public void setItemForDecrypt(int wAITING_FOR_DECRYPT2, Identity id,
			int arg0) {
		NFC_STATE = WAITING_FOR_DECRYPT;
		identityToDecrypt = id;
		identityToDecryptPosition = arg0;
		AlertDialog.Builder b = new Builder(new ContextThemeWrapper(this,
				R.style.AlertDialogCustom));
		b.setTitle("Tap a tag");
		b.setIcon(R.drawable.ic_nfc);
		b.setMessage("Tap the tag you setup with this identity");
		readPrompt = b.create();
		readPrompt.show();
		int dividerId = readPrompt.getContext().getResources()
				.getIdentifier("android:id/titleDivider", null, null);
		View divider = readPrompt.findViewById(dividerId);
		divider.setBackgroundColor(getResources().getColor(R.color.app_base));
	}

	public void removeItemForDecrypt() {
		NFC_STATE = -1;
		identityToDecrypt = null;
		identityToDecryptPosition = -1;
		listAdapter.showIdentity(null, -1);
	}

}
