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

    public static final String TB_FAVOURITE_DRIVER = "tb_favourite_driver";
    public static final String TB_FAVOURITE_FARMER = "tb_favourite_farmer";
    public static final String TB_FAVOURITE_CUSTOMER = "tb_favourite_customer";

    public static final String TB_FARMER = "tb_farmer";
    public static final String TB_CUSTOMER = "tb_customer";
    public static final String TB_DRIVER = "tb_driver";

    private static final String CREATE_TABLE_FAVOURITE_DRIVER = "CREATE TABLE " + TB_FAVOURITE_DRIVER +
            "(driver_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "name Text)";


    private static final String CREATE_TABLE_FAVOURITE_CUSTOMER = "CREATE TABLE " + TB_FAVOURITE_CUSTOMER +
            "(customer_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "default_driver Text, " +
            "name Text," +
            "address Text)";

    private static final String CREATE_TABLE_FAVOURITE_FARMER = "CREATE TABLE " + TB_FAVOURITE_FARMER +
            "(farmer_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "default_driver Text, " +
            "name Text," +
            "address Text)";

    private static final String CREATE_TABLE_FARMER = "CREATE TABLE " + TB_FARMER +
            "(id Text, " +
            "default_driver Text, " +
            "name Text, " +
            "phone Text, " +
            "address Text, " +
            "created_at Text)";

    private static final String CREATE_TABLE_CUSTOMER = "CREATE TABLE " + TB_CUSTOMER +
            "(id Text, " +
            "name Text, " +
            "default_driver Text, " +
            "phone Text, " +
            "address Text, " +
            "created_at Text)";

    private static final String CREATE_TABLE_DRIVER = "CREATE TABLE " + TB_DRIVER +
            "(id Text, " +
            "name Text, " +
            "phone Text, " +
            "address Text, " +
            "created_at Text)";


    public CustomSqliteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_FAVOURITE_DRIVER);
        sqLiteDatabase.execSQL(CREATE_TABLE_FAVOURITE_CUSTOMER);
        sqLiteDatabase.execSQL(CREATE_TABLE_FAVOURITE_FARMER);

        sqLiteDatabase.execSQL(CREATE_TABLE_FARMER);
        sqLiteDatabase.execSQL(CREATE_TABLE_CUSTOMER);
        sqLiteDatabase.execSQL(CREATE_TABLE_DRIVER);


    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TB_FAVOURITE_DRIVER);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TB_FAVOURITE_FARMER);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TB_FAVOURITE_CUSTOMER);

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TB_FARMER);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TB_CUSTOMER);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TB_DRIVER);
        onCreate(sqLiteDatabase);
    }
}
