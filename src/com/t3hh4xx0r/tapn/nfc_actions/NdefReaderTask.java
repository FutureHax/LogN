package com.t3hh4xx0r.tapn.nfc_actions;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Background task for reading the data. Do not block the UI thread while
 * reading.
 * 
 * @author Ralf Wondratschek
 * 
 */
public class NdefReaderTask extends AsyncTask<Tag, Void, String> {
	OnPayloadParsedListener listener;
	Tag tag;
	
	public NdefReaderTask(OnPayloadParsedListener listener, Tag tag) {
		super();
		this.listener = listener;
		this.tag = tag;
	}

	public interface OnPayloadParsedListener {
		void onPayloadParsed(String payload);
	}

	@Override
	protected String doInBackground(Tag... params) {

		Ndef ndef = Ndef.get(tag);
		if (ndef == null) {
			// NDEF is not supported by this Tag.
			return null;
		}

		NdefMessage ndefMessage = ndef.getCachedNdefMessage();

		NdefRecord[] records = ndefMessage.getRecords();
		for (NdefRecord ndefRecord : records) {
			if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN
					&& Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
				try {
					return readText(ndefRecord);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}

		return null;
	}

	private String readText(NdefRecord record)
			throws UnsupportedEncodingException {
		/*
		 * See NFC forum specification for "Text Record Type Definition" at
		 * 3.2.1
		 * 
		 * http://www.nfc-forum.org/specs/
		 * 
		 * bit_7 defines encoding bit_6 reserved for future use, must be 0
		 * bit_5..0 length of IANA language code
		 */

		byte[] payload = record.getPayload();

		// Get the Text Encoding
		String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

		// Get the Language Code
		int languageCodeLength = payload[0] & 0063;

		// String languageCode = new String(payload, 1, languageCodeLength,
		// "US-ASCII");
		// e.g. "en"

		// Get the Text
		return new String(payload, languageCodeLength + 1, payload.length
				- languageCodeLength - 1, textEncoding);
	}

	@Override
	protected void onPostExecute(String result) {
		if (result != null) {
			if (listener != null) {
				listener.onPayloadParsed(result);
			}
		}
	}
}