package com.quanleimu.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public abstract class Database {
	/**
	 * Read-Write open.
	 */
    protected static SQLiteDatabase database;
    
    /**
     * Read only open.
     */
    protected static SQLiteDatabase databaseRO;

    
    Database(Context ctx) {
        if (database == null) {
        	DatabaseOpenHelper helper = new DatabaseOpenHelper(ctx);
            database = helper.getWritableDatabase();
            databaseRO = helper.getReadableDatabase();
        }
    }
}
