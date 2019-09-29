package com.walixiwa.aplayer.tools;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.io.IOException;

public class PositionManager {
    private static SQLiteDatabase database;
    private static final String CREATE_POSITION_TABLE = "CREATE TABLE IF NOT EXISTS Player ( id integer PRIMARY KEY,Url text,Position integer)";
    private static PositionManager instance;

    public static PositionManager getInstance(Context context) {
        if (instance == null) {
            instance = new PositionManager();
            init(context);
        }
        return instance;
    }

    private static void init(Context context) {
        if (database != null) {
            return;
        }
        File f = new File(getDataBasePath(context) + "/player.db");
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException ioex) {
                ioex.printStackTrace();
            }
        }
        File file = new File(getDataBasePath(context) + "/player.db");
        if (file.exists()) {
            database = context.openOrCreateDatabase(getDataBasePath(context) + "/player.db", 0, null);
        }
        database.execSQL(CREATE_POSITION_TABLE);
    }


    private static String getDataBasePath(Context context) {
        String dataBasePath;
        File dir = context.getExternalCacheDir();
        if (dir != null) {
            dataBasePath = dir.getAbsolutePath() + "/database";
        } else {
            dataBasePath = context.getCacheDir().getAbsolutePath() + "/database";
        }
        if (!new File(dataBasePath).exists()) {
            new File(dataBasePath).mkdirs();
        }
        return dataBasePath;
    }


    public  void addPosition(String url, int position) {
        url = Integer.toString(url.hashCode());
        if (database != null) {
            if (isExists(url)) {
                database.execSQL("UPDATE Player SET Position = " + position + " WHERE Url = '" + url + "'");
            } else {
                database.execSQL("INSERT INTO Player VALUES (" + "null,'" + url + "'," + position + ")");
            }
        }
    }

    public static boolean isExists(String url) {
        url = Integer.toString(url.hashCode());
        int count = 0;
        if (database != null) {
            Cursor cursor = database.rawQuery("SELECT count(*) FROM Player WHERE Url = '" + url + "'", null);
            if (cursor.moveToNext()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count != 0;
    }


    public  int getPosition(String url) {
        url = Integer.toString(url.hashCode());
        int count = 0;
        try {
            if (database != null) {
                Cursor cursor = database.rawQuery("SELECT Position FROM Player WHERE Url = '" + url + "'", null);
                if (cursor.moveToNext()) {
                    count = cursor.getInt(0);
                }
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    public  void delete(String url) {
        url = Integer.toString(url.hashCode());
        if (database != null) {
            database.execSQL("DELETE FROM Player WHERE Url = '" + url + "'");
        }
    }

    public  void clear() {
        if (database != null) {
            database.execSQL("delete from Player");
            // db.execSQL("delete from TaskIndex");
        }
    }

    public  void close() {
        if (database != null) {
            database.close();
        }
    }
}
