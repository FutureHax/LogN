package com.t3hh4xx0r.tapn.models;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.t3hh4xx0r.tapn.Encryption;

public class Identity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 9090603562567941360L;
	String user, pass, name;

	public Identity(String name, String user, String pass) {
		this.user = user;
		this.name = name;
		this.pass = pass;
	}
	
	public Identity(JSONObject jsonInput) throws JSONException {
		this.user = jsonInput.getString("user");
		this.pass = jsonInput.getString("pass");
		this.name = jsonInput.getString("name");
	}
	
	public Identity(Context c, JSONObject jsonInput, String tagId) throws JSONException {
		this.user = Encryption.decryptString(c, jsonInput.getString("user"), tagId);
		this.pass = Encryption.decryptString(c, jsonInput.getString("pass"), tagId);
		this.name = jsonInput.getString("name");
	}

	@Override
	public String toString() {
		return "Identity [user=" + user + ", pass=" + pass + ", name=" + name + "]";
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public static Identity decryptIdentity(String tagId, Context c, Identity encrypted) {
		String dUser, dPass;

		dUser = Encryption.decryptString(c, encrypted.user, tagId);
		dPass = Encryption.decryptString(c, encrypted.pass, tagId);

		Identity res = new Identity(encrypted.name, dUser, dPass);
		return res;
	}

	public String toPayload() throws JSONException {
        JSONObject payload = new JSONObject();
        payload.put("user", user);
        payload.put("pass", pass);
        payload.put("name", name);
		return payload.toString();
	}

}
