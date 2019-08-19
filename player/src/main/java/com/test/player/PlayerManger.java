package com.test.player;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.io.IOException;

public class PlayerManger {
    private static PlayerManger instance;
    private static SQLiteDatabase database;
    private static final String CREAT_POSITION_TABLE = "CREATE TABLE IF NOT EXISTS Player ( id integer PRIMARY KEY,Url text,Position integer)";

    public static PlayerManger getInstance(Context context) {
        if (instance == null) {
            instance = new PlayerManger();
        }
        if (database == null) {
            openDataBase(context);
        }
        return instance;
    }

    private static void openDataBase(Context context) {
        if (database != null) {
            return;
        }
        creatDataBase(context);
        File file = new File(getDataBasePath(context) + "/aplayer.db");
        if (file.exists()) {
            database = context.openOrCreateDatabase(getDataBasePath(context) + "/aplayer.db", 0, null);
        }
        craetTable();
    }

    private static void creatDataBase(Context context) {
        File file = new File(getDataBasePath(context) + "/aplayer.db");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ioex) {
                ioex.printStackTrace();
            }
        }
    }

    private static void craetTable() {
        if (database != null) {
            database.execSQL(CREAT_POSITION_TABLE);
        }
    }

    private static String getDataBasePath(Context context) {
        String dataBasePath;
        File dir = context.getExternalFilesDir(null);
        if (dir != null) {
            dataBasePath = dir.getAbsolutePath() + "/database";
        } else {
            dataBasePath = context.getFilesDir().getAbsolutePath() + "/database";
        }
        if (!new File(dataBasePath).exists()) {
            new File(dataBasePath).mkdirs();
        }
        return dataBasePath;
    }


    public void addPlayerInfo(String url, int position) {
        url = Integer.toString(url.hashCode());
        if (database != null) {
            if (isExistsInfo(url)) {
                database.execSQL("UPDATE Player SET Position = " + position + " WHERE Url = '" + url + "'");
            } else {
                database.execSQL("INSERT INTO Player VALUES (" + "null,'" + url + "'," + position + ")");
            }
        }
    }

    private static boolean isExistsInfo(String url) {
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


    public int getPosition(String url) {
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

    public void deleteInfo(String url) {
        url = Integer.toString(url.hashCode());
        if (database != null) {
            database.execSQL("DELETE FROM Player WHERE Url = '" + url + "'");
        }
    }

    public static void clearTable() {
        if (database != null) {
            database.execSQL("delete from Player");
            // db.execSQL("delete from TaskIndex");
        }
    }

    public static void closeDataBase() {
        if (database != null) {
            database.close();
        }
    }
}
