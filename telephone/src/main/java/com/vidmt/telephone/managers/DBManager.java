package com.vidmt.telephone.managers;

import java.util.List;

import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.vidmt.acmn.abs.VLib;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.telephone.Config;

public class DBManager {
	private final String TAG = "DBManager";
	private static DBManager sInstance;
	private DbUtils mDb;

	public static DBManager get() {
		if (sInstance == null) {
			sInstance = new DBManager();
		}
		return sInstance;
	}

	private DBManager() {
		mDb = DbUtils.create(VLib.app());
		mDb.configAllowTransaction(true);
		mDb.configDebug(Config.DEBUG);
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

	public int getWhereCount(Class<?> entityType, String columnName1, Object value1, String columnName2, Object value2) {
		long count = 0;
		try {
			count = mDb.count(Selector.from(entityType).where(columnName1, "=", value1).and(columnName2, "=", value2));
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

	public void delete(Object entity) {
		try {
			mDb.delete(entity);
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

	public <T> List<T> getWhereEntity(Class<?> entityType, String columnName1, Object columnValue1, String columnName2,
			Object columnValue2) {
		List<T> list = null;
		try {
			list = mDb.findAll(Selector.from(entityType).where(columnName1, "=", columnValue1)
					.and(columnName2, "=", columnValue2));
		} catch (DbException e) {
			VLog.e(TAG, e);
		}
		return list;
	}

	public <T> List<T> getWhereEntity(Class<?> entityType, String columnName1, Object columnValue1, String columnName2,
			Object columnValue2, String columnName3, Object columnValue3) {
		List<T> list = null;
		try {
			list = mDb.findAll(Selector.from(entityType).where(columnName1, "=", columnValue1)
					.and(columnName2, "=", columnValue2).and(columnName3, "=", columnValue3));
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

	public <T> List<T> getRangeWhereEntity(Class<?> entityType, String columnName1, Object columnValue1,
			String columnName2, Object columnValue2, int start, int len) {
		List<T> entities = null;
		try {
			entities = mDb.findAll(Selector.from(entityType).where(columnName1, "=", columnValue1)
					.and(columnName2, "=", columnValue2).limit(len).offset(start));
		} catch (DbException e) {
			VLog.e(TAG, e);
		}
		return entities;
	}

	public <T> T getWhereEndEntity(Class<?> entityType, String columnName, Object columnValue, String orderColumnName,
			boolean desc) {
		T t = null;
		try {
			t = mDb.findFirst(Selector.from(entityType).where(columnName, "=", columnValue)
					.orderBy(orderColumnName, desc));
		} catch (DbException e) {
			VLog.e(TAG, e);
		}
		return t;
	}

	public <T> T getWhereEndEntity(Class<?> entityType, String columnName1, Object columnValue1, String columnName2,
			Object columnValue2, String orderColumnName, boolean desc) {
		T t = null;
		try {
			t = mDb.findFirst(Selector.from(entityType).where(columnName1, "=", columnValue1)
					.and(columnName2, "=", columnValue2).orderBy(orderColumnName, desc));
		} catch (DbException e) {
			VLog.e(TAG, e);
		}
		return t;
	}

	public int getInEntityCount(Class<?> entityType, String columnIn, Object[] objArr, String columnName,
			Object columnValue) {
		long t = 0;
		try {
			t = mDb.count(Selector.from(entityType).where(columnIn, "in", objArr).and(columnName, "=", columnValue));
		} catch (DbException e) {
			VLog.e(TAG, e);
		}
		return (int) t;
	}

	public int getInEntityCount(Class<?> entityType, String columnIn, Object[] objArr, String columnName1,
			Object columnValue1, String columnName2, Object columnValue2) {
		long t = 0;
		try {
			t = mDb.count(Selector.from(entityType).where(columnIn, "in", objArr).and(columnName1, "=", columnValue1)
					.and(columnName2, "=", columnValue2));
		} catch (DbException e) {
			VLog.e(TAG, e);
		}
		return (int) t;
	}

}
