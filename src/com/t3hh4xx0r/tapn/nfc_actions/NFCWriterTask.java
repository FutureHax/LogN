package com.t3hh4xx0r.tapn.nfc_actions;

import java.io.UnsupportedEncodingException;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;

public class NFCWriterTask extends AsyncTask<Void, Void, Void> {
	public interface OnPayloadWrittenListener {
		void onPayloadFinished(boolean success);
	}
	
	OnPayloadWrittenListener listener;
	String message;
	Tag tag;

	public NFCWriterTask(String message, Tag tag, OnPayloadWrittenListener listener) {
		super();
		this.message = message;
		this.tag = tag;
		this.listener = listener;
	}

	@Override
	protected Void doInBackground(Void... params) {
		try {
			NdefRecord[] records = { createRecord(message) };
			NdefMessage message = new NdefMessage(records);
			Ndef ndef = Ndef.get(tag);
			ndef.connect();
			ndef.writeNdefMessage(message);
			ndef.close();
			listener.onPayloadFinished(true);
		} catch (Exception e) {
			e.printStackTrace();
			listener.onPayloadFinished(false);
		}
		return null;
	}

	private NdefRecord createRecord(String text)
			throws UnsupportedEncodingException {
		// create the message in according with the standard
		String lang = "en";
		byte[] textBytes = text.getBytes();
		byte[] langBytes = lang.getBytes("US-ASCII");
		int langLength = langBytes.length;
		int textLength = textBytes.length;

		byte[] payload = new byte[1 + langLength + textLength];
		payload[0] = (byte) langLength;

		// copy langbytes and textbytes into payload
		System.arraycopy(langBytes, 0, payload, 1, langLength);
		System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

		NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
				NdefRecord.RTD_TEXT, new byte[0], payload);
		return recordNFC;
	}

}
