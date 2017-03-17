package com.vidmt.acmn.db;

import java.util.HashMap;
import java.util.Map;

import android.content.UriMatcher;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.vidmt.acmn.abs.VLib;

public abstract class AbsSwitcherDbHelper extends SQLiteOpenHelper {
	private static AbsSimpleDbTable[] sTables;
	private static final Map<Integer, AbsSimpleDbTable> mTableMap = new HashMap<Integer, AbsSimpleDbTable>();
	private static final UriMatcher sMatcher = new UriMatcher(UriMatcher.NO_MATCH);

	public static void init(String authority, AbsSimpleDbTable... tables) {
		if (sTables != null) {
			return;
		}
		sTables = tables;
		for (int i = 0; i < sTables.length; i++) {
			AbsSimpleDbTable table = sTables[i];
			if (table.getMatchAllReg() != null) {
				sMatcher.addURI(authority, table.getMatchAllReg(), i * 2 + 1);
			}
			if (table.getMatchItemReg() != null) {
				sMatcher.addURI(authority, table.getMatchItemReg(), i * 2 + 2);
			}

			mTableMap.put(i * 2 + 1, table);
		}
	}

	private final String mDbName;

	public AbsSwitcherDbHelper(String dbName, int version) {
		super(VLib.app(), dbName + ".db", null, version);
		mDbName = dbName;
	}

	public String getDbname() {
		return mDbName;
	}

	public static UriMatcher getUriMatch() {
		return sMatcher;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		for (AbsSimpleDbTable table : sTables) {
			table.onCreate(db);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		for (AbsSimpleDbTable table : sTables) {
			table.onUpgrade(db, oldVersion, newVersion);
		}
	}

	public AbsSimpleDbTable getTable(Uri uri) {
		int key = sMatcher.match(uri);
		key = (key % 2 == 0) ? key - 1 : key;
		return mTableMap.get(key);
	}

	public boolean isItem(Uri uri) {
		int key = sMatcher.match(uri);
		return key % 2 == 0;
	}
}
