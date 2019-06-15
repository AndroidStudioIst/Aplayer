package com.test.player;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.io.IOException;

public class PlayerDataBaseHelper {
    private static SQLiteDatabase db;
    private static final String CREAT_POSITION_TABLE = "CREATE TABLE IF NOT EXISTS Player ( id integer PRIMARY KEY,Url text,Position integer)";

    public static void openDataBase(Context context) {
        if (db != null) {
            return;
        }
        creatDataBase(context);
        File file = new File(getDataBasePath(context) + "/player.db");
        if (file.exists()) {
            db = context.openOrCreateDatabase(getDataBasePath(context) + "/player.db", 0, null);
        }
        craetTable();
    }

    private static void creatDataBase(Context context) {
        File f = new File(getDataBasePath(context) + "/player.db");
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException ioex) {
                ioex.printStackTrace();
            }
        }
    }

    private static void craetTable() {
        if (db != null) {
            db.execSQL(CREAT_POSITION_TABLE);
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


    public static void addPlayerInfo(String url, int position) {
        url = Integer.toString(url.hashCode());
        if (db != null) {
            if (isExistsInfo(url)) {
                db.execSQL("UPDATE Player SET Position = " + position + " WHERE Url = '" + url + "'");
            } else {
                db.execSQL("INSERT INTO Player VALUES (" + "null,'" + url + "'," + position + ")");
            }
        }
    }

    public static boolean isExistsInfo(String url) {
        url = Integer.toString(url.hashCode());
        int count = 0;
        if (db != null) {
            Cursor cursor = db.rawQuery("SELECT count(*) FROM Player WHERE Url = '" + url + "'", null);
            if (cursor.moveToNext()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count != 0;
    }


    public static int getPosition(String url) {
        url = Integer.toString(url.hashCode());
        int count = 0;
        try {
            if (db != null) {
                Cursor cursor = db.rawQuery("SELECT Position FROM Player WHERE Url = '" + url + "'", null);
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

    public static void deleteInfo(String url) {
        url = Integer.toString(url.hashCode());
        if (db != null) {
            db.execSQL("DELETE FROM Player WHERE Url = '" + url + "'");
        }
    }

    public static void clearTable() {
        if (db != null) {
            db.execSQL("delete from Player");
            // db.execSQL("delete from TaskIndex");
        }
    }

    public static void closeDataBase() {
        if (db != null) {
            db.close();
        }
    }
}
