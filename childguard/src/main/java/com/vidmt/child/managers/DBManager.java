package com.vidmt.child.managers;

import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.vidmt.acmn.abs.VLib;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.child.Config;

import java.util.List;

public class DBManager {
	private static DBManager sInstance;
	private final String TAG = "DBManager";
	private DbUtils mDb;

	private DBManager() {
		mDb = DbUtils.create(VLib.app());
		mDb.configAllowTransaction(true);
		mDb.configDebug(Config.DEBUG);
	}

	public static DBManager get() {
		if (sInstance == null) {
			sInstance = new DBManager();
		}
		return sInstance;
	}

	public DbUtils getDb() {
		return mDb;
	}

	public void saveEntity(Object entity) {
		try {
			mDb.save(entity);
		} catch (DbException e) {
			VLog.e(TAG, e);
		}
	}

	public <T> T findEntityById(Class<?> entityType, int id) {
		T t = null;
		try {
			t = (T) mDb.findById(entityType, id);
		} catch (DbException e) {
			VLog.e(TAG, e);
		}
		return t;
	}

	public void deleteEntityById(Class<?> entityType, int id) {
		try {
			mDb.deleteById(entityType, id);
		} catch (DbException e) {
			VLog.e(TAG, e);
		}
	}

	public <T> List<T> getAllEntity(Class<?> entityType) {
		List<T> entities = null;
		try {
			entities = mDb.findAll(Selector.from(entityType));
		} catch (DbException e) {
			VLog.e(TAG, e);
		}
		return entities;
	}

	public <T> List<T> getRangeEntity(Class<?> entityType, int start, int len) {
		List<T> entities = null;
		try {
			entities = mDb.findAll(Selector.from(entityType).limit(len).offset(start));
		} catch (DbException e) {
			VLog.e(TAG, e);
		}
		return entities;
	}

	public int getCount(Class<?> entityType) {
		int tableItems = 0;
		try {
			long count = mDb.count(entityType);
			tableItems = Integer.valueOf(count + "");
		} catch (DbException e) {
			VLog.e(TAG, e);
		}
		return tableItems;
	}

	public int getWhereCount(Class<?> entityType, String columnName, Object value) {
		long count = 0;
		try {
			count = mDb.count(Selector.from(entityType).where(columnName, "=", value));
		} catch (DbException e) {
			VLog.e(TAG, e);
		}
		return (int) count;
	}

	public void update(Object entity) {
		try {
			mDb.update(entity);
		} catch (DbException e) {
			VLog.e(TAG, e);
		}
	}

	public <T> List<T> getWhereEntity(Class<?> entityType, String columnName, Object columnValue) {
		List<T> list = null;
		try {
			list = mDb.findAll(Selector.from(entityType).where(columnName, "=", columnValue));
		} catch (DbException e) {
			VLog.e(TAG, e);
		}
		return list;
	}

	public <T> List<T> getRangeWhereEntity(Class<?> entityType, String columnName, Object columnValue, int start,
			int len) {
		List<T> entities = null;
		try {
			entities = mDb.findAll(Selector.from(entityType).where(columnName, "=", columnValue).limit(len)
					.offset(start));
		} catch (DbException e) {
			VLog.e(TAG, e);
		}
		return entities;
	}

}
