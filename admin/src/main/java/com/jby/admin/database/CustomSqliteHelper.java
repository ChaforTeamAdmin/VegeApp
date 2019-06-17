package com.jby.admin.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by user on 3/11/2018.
 */

public class CustomSqliteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "Database";
    private static final int DATABASE_VERSION = 1;

    public static final String TB_DEFAULT_DRIVER = "tb_default_driver";

    private static final String CREATE_TB_DEFAULT_DRIVER = "CREATE TABLE "+ TB_DEFAULT_DRIVER +
            "(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "driver_id Text," +
            "name Text," +
            "customer_id Text)";


   public CustomSqliteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TB_DEFAULT_DRIVER);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TB_DEFAULT_DRIVER);
        onCreate(sqLiteDatabase);
    }
}
