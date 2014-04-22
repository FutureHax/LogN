package com.t3hh4xx0r.tapn;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.t3hh4xx0r.tapn.models.Identity;

public class DBAdapter {
	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_SWITCHES);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + IDENTITIES);
			onCreate(db);
		}
	}

	private static final int DB_VERSION = 1;
	public static final String DB_NAME = "identities.db";
	public static final String IDENTITIES = "identities";
	public static final String USER = "user";
	public static final String PASS = "pass";
	public static final String NAME = "name";

	private static final String CREATE_SWITCHES = "create table " + IDENTITIES
			+ "(_id integer primary key autoincrement, " + USER + " text, " + NAME + " text not null, " + PASS + " text);";

	private final Context context;
	private static DatabaseHelper DBHelper;
	public SQLiteDatabase db;

	public DBAdapter(Context ctx) {
		this.context = ctx;
		DBHelper = new DatabaseHelper(context);
	}

	public void close() {
		DBHelper.close();
	}

	public void insertIdentity(Context c, Identity id, String tagID) {
		if (!new SettingsProvider(c).getHasFullUpgrade() && getIdentities().size() > 0) {
			Toast.makeText(c, "Only one identity allowed for trial users.", Toast.LENGTH_LONG).show();
			return;
		}
		open();
		ContentValues v = new ContentValues();
		v.put(PASS, Encryption.encryptString(c, id.getPass(), tagID));
		v.put(USER, Encryption.encryptString(c, id.getUser(), tagID));
		v.put(NAME, id.getName());

		db.insert(IDENTITIES, null, v);
		close();
	}

	public boolean deleteIdentity(Identity id) {
		Log.d("THE ID", id.toString());
		return db.delete(
				IDENTITIES,
				NAME + " = ? AND " + USER + " = ? AND "
						+ PASS + " = ?",
				new String[] { id.getName(), id.getUser(),
						id.getPass() }) > 0;
	}

	public DBAdapter open() throws SQLException {
		db = DBHelper.getWritableDatabase();
		return this;
	}
	

	public ArrayList<Identity> getIdentities() {
		open();
		ArrayList<Identity> res = new ArrayList<Identity>();
		Cursor mCursor = db.query(IDENTITIES, new String[] { NAME, USER, PASS}, null, null, null, null, null, null);
		while (mCursor.moveToNext()) {
			Identity id = new Identity(mCursor.getString(0),
					mCursor.getString(1), mCursor.getString(2));
			res.add(id);
		}
		close();
		return res;
	}

}
