package com.vidmt.acmn.db;

import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public abstract class AbsSimpleDbTable {
	protected String mTableName;
	protected String mPrimKey;

	/**
	 * @param matcher
	 *            the matcher that managered by the helper
	 * @param tableName
	 * @param primKey
	 * @param matchKey
	 *            the matcher key that matches the whole data. matchkey+1 means
	 *            the itemone
	 */
	public AbsSimpleDbTable(UriMatcher matcher, String tableName, String primKey) {
		mTableName = tableName;
		mPrimKey = primKey;
	}

	public String getDbName() {
		return mTableName;
	}

	public String getPrimeKey() {
		return mPrimKey;
	}

	abstract public String getMatchAllReg();

	abstract public String getMatchItemReg();

	public String getType(boolean isItem) {
		return "vnd.android.cursor." + (isItem ? "item" : "dir") + "/" + mTableName;
	}

	abstract public void onCreate(SQLiteDatabase db);

	abstract public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);

	public synchronized void rawQuery(SQLiteOpenHelper helper, String sql, String[] selectionArgs) {
		helper.getReadableDatabase().rawQuery(sql, selectionArgs);
	}

	public synchronized Cursor query(SQLiteOpenHelper helper, String[] columns, String selection,
			String[] selectionArgs, String orderBy) {
		return helper.getReadableDatabase().query(mTableName, columns, selection, selectionArgs, null, null, orderBy);
	}

	public synchronized void clearTable(SQLiteOpenHelper helper) {
		helper.getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + mTableName);
	}

	public synchronized Cursor query(SQLiteOpenHelper helper, String[] columns, String selection,
			String[] selectionArgs, String orderBy, String limit) {
		return helper.getReadableDatabase().query(mTableName, columns, selection, selectionArgs, null, null, orderBy,
				limit);
	}

	public synchronized long insert(SQLiteOpenHelper helper, ContentValues values) {
		return helper.getWritableDatabase().insert(mTableName, "", values);
	}

	public synchronized int bulkInsert(SQLiteOpenHelper helper, ContentValues[] values) {
		SQLiteDatabase db = helper.getWritableDatabase();
		db.beginTransaction();
		try {
			int count = values.length;
			for (int i = 0; i < count; i++) {
				if (db.insert(mTableName, null, values[i]) < 0) {
					return 0;
				}
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		return values.length;
	}

	public synchronized long insertWithOnConflict(SQLiteOpenHelper helper, ContentValues values, int conflictAlgorithm) {
		return helper.getWritableDatabase().insertWithOnConflict(mTableName, "", values, conflictAlgorithm);
	}

	public synchronized int delete(SQLiteOpenHelper helper, String whereClause, String[] whereArgs) {
		return helper.getWritableDatabase().delete(mTableName, whereClause, whereArgs);
	}

	public synchronized int update(SQLiteOpenHelper helper, ContentValues values, String whereClause, String[] whereArgs) {
		return helper.getWritableDatabase().update(mTableName, values, whereClause, whereArgs);
	}
}
