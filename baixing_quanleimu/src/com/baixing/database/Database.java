package com.baixing.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public abstract class Database {
	/**
	 * Read-Write open.
	 */
    protected static SQLiteDatabase database;
    
    /**
     * Read only open.
     */
    protected static SQLiteDatabase databaseRO;
    
    protected Context context;

    
    Database(Context ctx) {
        if (database == null) {
        	try{
	        	DatabaseOpenHelper helper = new DatabaseOpenHelper(ctx);
	        	context = ctx;
	            database = helper.getWritableDatabase();
	            databaseRO = helper.getReadableDatabase();
        	}catch(SQLiteException e){
        	}
        }
    }
}
