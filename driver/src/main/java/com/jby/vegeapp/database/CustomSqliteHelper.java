package com.jby.vegeapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by user on 3/11/2018.
 */

public class CustomSqliteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "Database";
    private static final int DATABASE_VERSION = 1;

    public static final String TB_PICK_UP_FAVOURITE_FARMER = "tb_pick_up_favourite_farmer";
    public static final String TB_BASKET_FAVOURITE_FARMER = "tb_basket_favourite_farmer";
    public static final String TB_BASKET_FAVOURITE_CUSTOMER = "tb_basket_favourite_customer";
    public static final String TB_STOCK = "tb_stock";
    public static final String TB_BASKET = "tb_basket";
    public static final String TB_PRODUCT = "tb_product";
    public static final String TB_PRODUCT_PRIORITY = "tb_product_priority";
    public static final String TB_FARMER = "tb_farmer";
    public static final String TB_COMPANY = "tb_company";
    public static final String TB_RECEIVED_ORDER = "tb_received_order";

    private static final String CREATE_TABLE_PICK_UP_FAVOURITE_FARMER = "CREATE TABLE " + TB_PICK_UP_FAVOURITE_FARMER +
            "(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "farmer_id Text," +
            "name Text," +
            "address Text)";

    private static final String CREATE_TABLE_TB_BASKET_FAVOURITE_FARMER = "CREATE TABLE " + TB_BASKET_FAVOURITE_FARMER +
            "(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "farmer_id Text," +
            "name Text," +
            "address Text)";

    private static final String CREATE_TABLE_TB_BASKET_FAVOURITE_CUSTOMER = "CREATE TABLE " + TB_BASKET_FAVOURITE_CUSTOMER +
            "(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "customer_id Text," +
            "name Text," +
            "address Text)";

    private static final String CREATE_TABLE_PICK_UP_VEGE = "CREATE TABLE " + TB_STOCK +
            "(id INTEGER PRIMARY KEY, " +
            "ro_id Text, " +
            "farmer_id Text, " +
            "product_id Text, " +
            "name Text, " +
            "picture Text, " +
            "price Text, " +
            "type Text, " +
            "quantity Text, " +
            "weight Text, " +
            "session Text, " +
            "grade Text, " +
            "status INTEGER DEFAULT 0, " +
            "created_at Text," +
            "updated_at Text)";

    private static final String CREATE_TABLE_BASKET = "CREATE TABLE " + TB_BASKET +
            "(id INTEGER PRIMARY KEY, " +
            "farmer_id Text DEFAULT 0, " +
            "customer_id Text DEFAULT 0, " +
            "quantity Text, " +
            "type Text, " +
            "status INTEGER DEFAULT 0, " +
            "created_at Text," +
            "updated_at Text)";

    private static final String CREATE_TABLE_PRODUCT = "CREATE TABLE " + TB_PRODUCT +
            "(id Text, " +
            "product_code Text, " +
            "name Text, " +
            "picture Text, " +
            "type Text, " +
            "price Text, " +
            "priority Text, " +
            "created_at Text)";

    private static final String CREATE_TABLE_PRODUCT_PRIORITY = "CREATE TABLE " + TB_PRODUCT_PRIORITY +
            "(product_id Text, " +
            "priority Text)";

    private static final String CREATE_TABLE_FARMER = "CREATE TABLE " + TB_FARMER +
            "(id Text, " +
            "name Text, " +
            "phone Text, " +
            "address Text, " +
            "created_at Text)";

    private static final String CREATE_TABLE_COMPANY = "CREATE TABLE " + TB_COMPANY +
            "(id Text, " +
            "name Text, " +
            "phone Text, " +
            "fax Text, " +
            "address Text, " +
            "created_at Text)";

    private static final String CREATE_TABLE_RECEIVED_ORDER = "CREATE TABLE " + TB_RECEIVED_ORDER +
            "(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "created_at Text)";

    public CustomSqliteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_PICK_UP_FAVOURITE_FARMER);
        sqLiteDatabase.execSQL(CREATE_TABLE_TB_BASKET_FAVOURITE_FARMER);
        sqLiteDatabase.execSQL(CREATE_TABLE_TB_BASKET_FAVOURITE_CUSTOMER);
        sqLiteDatabase.execSQL(CREATE_TABLE_PICK_UP_VEGE);
        sqLiteDatabase.execSQL(CREATE_TABLE_BASKET);
        sqLiteDatabase.execSQL(CREATE_TABLE_PRODUCT);
        sqLiteDatabase.execSQL(CREATE_TABLE_FARMER);
        sqLiteDatabase.execSQL(CREATE_TABLE_COMPANY);
        sqLiteDatabase.execSQL(CREATE_TABLE_RECEIVED_ORDER);
        sqLiteDatabase.execSQL(CREATE_TABLE_PRODUCT_PRIORITY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TB_PICK_UP_FAVOURITE_FARMER);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TB_BASKET_FAVOURITE_FARMER);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TB_BASKET_FAVOURITE_CUSTOMER);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TB_STOCK);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TB_BASKET);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TB_PRODUCT);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TB_FARMER);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TB_COMPANY);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TB_RECEIVED_ORDER);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TB_PRODUCT_PRIORITY);
        onCreate(sqLiteDatabase);
    }
}
