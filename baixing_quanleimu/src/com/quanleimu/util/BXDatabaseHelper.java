package com.quanleimu.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class BXDatabaseHelper extends SQLiteOpenHelper {
	public static String TABLENAME = "networkReqAndRes";
	public BXDatabaseHelper(Context context, String name, CursorFactory cursorFactory, int version) {
		super(context, name, cursorFactory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		  db.execSQL("create table if not exists " + TABLENAME + "(url BLOB, response BLOB, timestamp INTEGER, UNIQUE (url) ON CONFLICT REPLACE)");  
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO 更改数据库版本的操作
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		// TODO 每次成功打开数据库后首先被执行
	}
}