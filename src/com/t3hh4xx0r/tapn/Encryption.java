package com.t3hh4xx0r.tapn;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.content.Context;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Base64;

public class Encryption {
	// Public for static/non-static conversion
	public Encryption() {
	}

	// String for Encryption Errors
	public static String encryptionError = "";

	// ==========================================================
	// I/O Methods
	// ==========================================================

	// Decrypt String
	public static String decryptString(Context c, String text, String tagId) {
		// Create new instance
		Encryption cryClass = new Encryption();

		// Return String place holder
		String returnString = "";

		try {
			returnString = cryClass.decrypt(text, getEncryptionKey(c, tagId));
		} catch (Exception ex) {
			// Set encryptionError = to Exception
			encryptionError = ex.toString();
		} 

		if (returnString.length() == 0) {
			return encryptionError;
		} else {
			return returnString;
		}
	}

	private static String getDeviceId(Context c) {
		String identifier = null;
		TelephonyManager tm = (TelephonyManager) c
				.getSystemService(Context.TELEPHONY_SERVICE);
		if (tm != null) {
			identifier = tm.getDeviceId();
		}
		if (identifier == null || identifier.length() == 0) {
			identifier = Secure.getString(c.getContentResolver(),
					Secure.ANDROID_ID);
		}
		
		return identifier;
	}
	
	private static String getEncryptionKey(Context c, String tagId) {
		return md5(tagId + getDeviceId(c));
	}
	
	private static String md5(String string) {
	    byte[] hash;

	    try {
	        hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
	    } catch (NoSuchAlgorithmException e) {
	        throw new RuntimeException("Huh, MD5 should be supported?", e);
	    } catch (UnsupportedEncodingException e) {
	        throw new RuntimeException("Huh, UTF-8 should be supported?", e);
	    }

	    StringBuilder hex = new StringBuilder(hash.length * 2);

	    for (byte b : hash) {
	        int i = (b & 0xFF);
	        if (i < 0x10) hex.append('0');
	        hex.append(Integer.toHexString(i));
	    }

	    return hex.toString();
	}

	// Encrypt String
	public static String encryptString(Context c, String text, String tagID) {
		// Create new instance
		Encryption cryClass = new Encryption();

		// Return String
		String returnString = "Null";

		try {
			returnString = cryClass.encrypt(text, getEncryptionKey(c, tagID));
		} catch (KeyException ex) {
			// Set encryptionError = to Exception
			encryptionError = ex.toString();
		} catch (InvalidAlgorithmParameterException ex) {
			// Set encryptionError = to Exception
			encryptionError = ex.toString();
		} catch (IllegalBlockSizeException ex) {
			// Set encryptionError = to Exception
			encryptionError = ex.toString();
		} catch (BadPaddingException ex) {
			// Set encryptionError = to Exception
			encryptionError = ex.toString();
		} catch (GeneralSecurityException ex) {
			// Set encryptionError = to Exception
			encryptionError = ex.toString();
		} catch (IOException ex) {
			// Set encryptionError = to Exception
			encryptionError = ex.toString();
		}

		return returnString;

	}

	// ==========================================================

	// ==========================================================
	// Encryption Variables
	// ==========================================================

	private final String characterEncoding = "UTF-8";
	private final String cipherTransformation = "AES/CBC/PKCS5Padding";
	private final String aesEncryptionAlgorithm = "AES";

	// ==========================================================

	// ==========================================================
	// Byte-Level Methods
	// ==========================================================

	public byte[] decrypt(byte[] cipherText, byte[] key, byte[] initialVector)
			throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, InvalidAlgorithmParameterException,
			IllegalBlockSizeException, BadPaddingException {
		Cipher cipher = Cipher.getInstance(cipherTransformation);
		SecretKeySpec secretKeySpecy = new SecretKeySpec(key,
				aesEncryptionAlgorithm);
		IvParameterSpec ivParameterSpec = new IvParameterSpec(initialVector);
		cipher.init(Cipher.DECRYPT_MODE, secretKeySpecy, ivParameterSpec);
		cipherText = cipher.doFinal(cipherText);
		return cipherText;
	}

	public byte[] encrypt(byte[] plainText, byte[] key, byte[] initialVector)
			throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, InvalidAlgorithmParameterException,
			IllegalBlockSizeException, BadPaddingException {
		Cipher cipher = Cipher.getInstance(cipherTransformation);
		SecretKeySpec secretKeySpec = new SecretKeySpec(key,
				aesEncryptionAlgorithm);
		IvParameterSpec ivParameterSpec = new IvParameterSpec(initialVector);
		cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
		plainText = cipher.doFinal(plainText);
		return plainText;
	}

	private byte[] getKeyBytes(String key) throws UnsupportedEncodingException {
		byte[] keyBytes = new byte[16];
		byte[] parameterKeyBytes = key.getBytes(characterEncoding);
		System.arraycopy(parameterKeyBytes, 0, keyBytes, 0,
				Math.min(parameterKeyBytes.length, keyBytes.length));
		return keyBytes;
	}

	// ==========================================================

	// ==========================================================
	// Encrypt/Decrypt Methods
	// ==========================================================

	private String encrypt(String plainText, String key)
			throws UnsupportedEncodingException, InvalidKeyException,
			NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException,
			BadPaddingException {
		byte[] plainTextbytes = plainText.getBytes(characterEncoding);
		byte[] keyBytes = getKeyBytes(key);
		return Base64.encodeToString(
				encrypt(plainTextbytes, keyBytes, keyBytes), Base64.DEFAULT);
	}

	private String decrypt(String encryptedText, String key)
			throws KeyException, GeneralSecurityException,
			GeneralSecurityException, InvalidAlgorithmParameterException,
			IllegalBlockSizeException, BadPaddingException, IOException {
		byte[] cipheredBytes = Base64.decode(encryptedText, Base64.DEFAULT);
		byte[] keyBytes = getKeyBytes(key);
		return new String(decrypt(cipheredBytes, keyBytes, keyBytes),
				characterEncoding);
	}

	// ==========================================================

}
