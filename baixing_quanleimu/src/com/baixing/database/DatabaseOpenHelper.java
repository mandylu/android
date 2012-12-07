package com.baixing.database;

import android.util.Log;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 
 * @author liuchong
 *
 */
public class DatabaseOpenHelper extends SQLiteOpenHelper {

	public static final String DATABASE_NAME = "entitys.db";
    private static final int DATABASE_VERSION = 2;
    
	public static final String CHAT_MESSAGE_TABLE = "chatMsg";
	public static final String CHAT_MESSAGE_TABLE_CREATE = "create table if not exists " + CHAT_MESSAGE_TABLE +
			" (msgId TEXT NOT NULL," +
			" sender TEXT NOT NULL," +
			" receiver TEXT NOT NULL," +
			" adId TEXT," + 
			" sessionId TEXT," +
			" timestamp INTEGER," +
			"readstatus INTEGER," +
			" msgJson BLOB)";
	
	public DatabaseOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CHAT_MESSAGE_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (newVersion == 2)
		{
			Log.e("db", "clear database");
			db.execSQL("drop TABLE " + CHAT_MESSAGE_TABLE);
			db.execSQL(CHAT_MESSAGE_TABLE_CREATE);
		}
	}

}
