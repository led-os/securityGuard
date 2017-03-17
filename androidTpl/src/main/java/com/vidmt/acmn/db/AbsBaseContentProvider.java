package com.vidmt.acmn.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public abstract class AbsBaseContentProvider extends ContentProvider {
	/**
	 * better to implememt to use the default function;
	 * 
	 * @param uri
	 * @return
	 */
	abstract public AbsSwitcherDbHelper getDbHelper(Uri uri);

	@Override
	public boolean onCreate() {
		return false;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		AbsSwitcherDbHelper helper = getDbHelper(uri);
		AbsSimpleDbTable table = helper.getTable(uri);
		if (helper.isItem(uri)) {
			return table.delete(helper, table.getPrimeKey() + "=" + uri.getLastPathSegment(), null);
		}
		return table.delete(helper, selection, selectionArgs);
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		AbsSwitcherDbHelper helper = getDbHelper(uri);
		AbsSimpleDbTable table = helper.getTable(uri);
		if (helper.isItem(uri)) {
			throw new IllegalArgumentException("item can be insert only on NON-ITEM uri");
		}
		long rowId = table.insert(helper, values);
		if (rowId < 0) {
			throw new IllegalStateException("failed to insert row into " + uri);
		}
		Uri newUri = ContentUris.withAppendedId(uri, rowId);
		getContext().getContentResolver().notifyChange(newUri, null);
		return newUri;
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		AbsSwitcherDbHelper helper = getDbHelper(uri);
		AbsSimpleDbTable table = helper.getTable(uri);
		if (helper.isItem(uri)) {
			throw new IllegalArgumentException("item can be insert only on NON-ITEM uri");
		}
		return (int) table.bulkInsert(helper, values);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		AbsSwitcherDbHelper helper = getDbHelper(uri);
		AbsSimpleDbTable table = helper.getTable(uri);
		if (helper.isItem(uri)) {
			return table.query(helper, projection, table.getPrimeKey() + "=" + uri.getLastPathSegment(), null, null);
		}
		return table.query(helper, projection, selection, selectionArgs, sortOrder);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		AbsSwitcherDbHelper helper = getDbHelper(uri);
		AbsSimpleDbTable table = helper.getTable(uri);
		if (helper.isItem(uri)) {
			return table.update(helper, values, table.getPrimeKey() + "=" + uri.getLastPathSegment(), null);
		}
		return table.update(helper, values, selection, selectionArgs);
	}

	@Override
	public String getType(Uri uri) {
		AbsSwitcherDbHelper helper = getDbHelper(uri);
		AbsSimpleDbTable table = helper.getTable(uri);
		return table.getType(helper.isItem(uri));
	}
}
