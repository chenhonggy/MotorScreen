/*
 *  Copyright (c) 2013 The CCP project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a Beijing Speedtong Information Technology Co.,Ltd license
 *  that can be found in the LICENSE file in the root of the web site.
 *
 *   http://www.yuntongxun.com
 *
 *  An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */
package com.speedtong.example.meeting.db;

import android.content.Context;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;


import com.speedtong.example.meeting.ECApplication;
import com.speedtong.example.meeting.common.utils.LogUtil;
import com.speedtong.example.meeting.ui.manager.CCPAppManager;
import com.speedtong.sdk.im.ECMessage;


/**
 * 数据库访问接口
 * @author Jorstin Chan@容联•云通讯
 * @date 2014-12-11
 * @version 4.0
 */
public abstract class AbstractSQLManager {
	
	public static final String TAG = AbstractSQLManager.class.getName();
	
	private static DatabaseHelper databaseHelper;
	private static SQLiteDatabase sqliteDB;
	
	public AbstractSQLManager() {
		openDatabase(ECApplication.getInstance(), CCPAppManager.getVersionCode());
	}
	
	private void openDatabase(Context context, int databaseVersion) {
		if (databaseHelper == null) {
			databaseHelper = new DatabaseHelper(context,this , databaseVersion);
		}
		if (sqliteDB == null) {
			sqliteDB = databaseHelper.getWritableDatabase();
		}
	}
	
	public void destroy() {
		try {
			if (databaseHelper != null) {
				databaseHelper.close();
			}
			if (sqliteDB != null) {
				sqliteDB.close();
			}
		} catch (Exception e) {
			LogUtil.e(e.toString());
		}
	}

	private void open(boolean isReadonly) {
		if (sqliteDB == null) {
			if (isReadonly) {
				sqliteDB = databaseHelper.getReadableDatabase();
			} else {
				sqliteDB = databaseHelper.getWritableDatabase();
			}
		}
	}

	public final void reopen() {
		closeDB();
		open(false);
		LogUtil.w("[SQLiteManager] reopen this db.");
	}

	private void closeDB() {
		if (sqliteDB != null) {
			sqliteDB.close();
			sqliteDB = null;
		}
	}

	protected final SQLiteDatabase sqliteDB() {
		open(false);
		return sqliteDB;
	}
	
	/**
	 * 创建基础表结构
	 */
	static class DatabaseHelper extends SQLiteOpenHelper {
		
		/**数据库名称*/
		static final String DATABASE_NAME = "ECSDK_meeting.db";
		static final String DESC = "DESC";
		static final String ASC = "ASC";
		
		static final String TABLES_NAME_CONTACT = "contacts";
		
			
		
		private AbstractSQLManager mAbstractSQLManager;
		public DatabaseHelper(Context context, AbstractSQLManager manager ,int version) {
			this(context, manager ,CCPAppManager.getClientUser().getUserId() + "_" + DATABASE_NAME, null, version);
		}

		public DatabaseHelper(Context context, AbstractSQLManager manager , String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
			mAbstractSQLManager = manager ;
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			createTables(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			createTables(db);
		}
		
		/**
		 * @param db
		 */
		private void createTables(SQLiteDatabase db) {
			// 创建联系人表
			createTableForContacts(db);
			
		
		}

		/**
		 * 创建联系人表
		 * 
		 * @param db
		 */
		void createTableForContacts(SQLiteDatabase db) {

			String sql = "CREATE TABLE IF NOT EXISTS " + TABLES_NAME_CONTACT
					+ " (" + ContactsColumn.ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ ContactsColumn.CONTACT_ID
					+ " TEXT UNIQUE ON CONFLICT ABORT, " + ContactsColumn.type
					+ " INTEGER, " + ContactsColumn.USERNAME + " TEXT, "
					+ ContactsColumn.SUBACCOUNT + " TEXT, "
					+ ContactsColumn.TOKEN + " TEXT, "
					+ ContactsColumn.SUBTOKEN + " TEXT, "
					+ ContactsColumn.REMARK + " TEXT " + ")";
			LogUtil.v(TAG + ":" + sql);
			db.execSQL(sql);
		}
	
		
		
		
	}
	
	
	
	
	class BaseColumn {
		public static final String ID = "ID";
		public static final String UNREAD_NUM = "unreadCount";
	}
	
	
	
	
	/**
	 * 联系人表
	 */
	public class ContactsColumn extends BaseColumn {
		/**联系人账号*/
		public static final String CONTACT_ID = "contact_id";
		/**联系人昵称*/
		public static final String USERNAME = "username";
		/**联系人账号Token*/
		public static final String TOKEN = "token";
		/**联系人子账号*/
		public static final String SUBACCOUNT = "subAccount";
		/**联系人子账号Token*/
		public static final String SUBTOKEN = "subToken";
		/**联系人类型*/
		public static final String type = "type";
		/**备注*/
		public static final String REMARK = "remark";
	}
	
	
    
    protected void release() {
    	destroy();
    	closeDB();
    	databaseHelper = null;
    }
}
