package com.quanleimu.database;

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
    private static final int DATABASE_VERSION = 1;
    
	public static final String CHAT_MESSAGE_TABLE = "chatMsg";
	public static final String CHAT_MESSAGE_TABLE_CREATE = "create table if not exists " + CHAT_MESSAGE_TABLE +
			" (msgId TEXT NOT NULL," +
			" sender TEXT NOT NULL," +
			" receiver TEXT NOT NULL," +
			" adId TEXT," + 
			" sessionId TEXT," +
			" timestamp INTEGER," +
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
		// TODO Auto-generated method stub

	}

}
