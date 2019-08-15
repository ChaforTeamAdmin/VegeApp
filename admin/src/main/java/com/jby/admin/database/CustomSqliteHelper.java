package com.jby.admin.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by user on 3/11/2018.
 */

public class CustomSqliteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "Database";
    private static final int DATABASE_VERSION = 11;

    public static final String TB_FAVOURITE_DRIVER = "tb_favourite_driver";
    public static final String TB_FAVOURITE_FARMER = "tb_favourite_farmer";
    public static final String TB_FAVOURITE_CUSTOMER = "tb_favourite_customer";

    public static final String TB_FARMER = "tb_farmer";
    public static final String TB_CUSTOMER = "tb_customer";
    public static final String TB_DRIVER = "tb_driver";
    public static final String TB_PRODUCT = "tb_product";
    public static final String TB_LOCATION = "tb_location";
    public static final String TB_GRADE = "tb_grade";

    public static final String TB_DELIVERY_ORDER = "tb_delivery_order";
    public static final String TB_STOCK_OUT = "tb_stock_out";

    public static final String TB_PURCHASE_ORDER = "tb_purchase_order";
    public static final String TB_STOCK_IN = "tb_stock_in";

    private static final String CREATE_TABLE_FAVOURITE_DRIVER = "CREATE TABLE " + TB_FAVOURITE_DRIVER +
            "(driver_id Text, " +
            "name Text, " +
            "nickname Text, " +
            "phone Text, " +
            "created_at Text)";


    private static final String CREATE_TABLE_FAVOURITE_CUSTOMER = "CREATE TABLE " + TB_FAVOURITE_CUSTOMER +
            "(customer_id Text," +
            "default_driver Text, " +
            "name Text," +
            "phone Text, " +
            "address Text, " +
            "created_at Text)";

    private static final String CREATE_TABLE_FAVOURITE_FARMER = "CREATE TABLE " + TB_FAVOURITE_FARMER +
            "(farmer_id Text, " +
            "default_driver Text, " +
            "name Text, " +
            "phone Text, " +
            "address Text, " +
            "created_at Text)";

    private static final String CREATE_TABLE_FARMER = "CREATE TABLE " + TB_FARMER +
            "(farmer_id Text, " +
            "default_driver Text, " +
            "name Text, " +
            "phone Text, " +
            "address Text, " +
            "created_at Text)";

    private static final String CREATE_TABLE_CUSTOMER = "CREATE TABLE " + TB_CUSTOMER +
            "(customer_id Text, " +
            "name Text, " +
            "default_driver Text, " +
            "phone Text, " +
            "address Text, " +
            "created_at Text)";

    private static final String CREATE_TABLE_DRIVER = "CREATE TABLE " + TB_DRIVER +
            "(driver_id Text, " +
            "name Text, " +
            "nickname Text, " +
            "phone Text, " +
            "created_at Text)";

    private static final String CREATE_TABLE_PRODUCT = "CREATE TABLE " + TB_PRODUCT +
            "(product_id Text, " +
            "product_code Text, " +
            "name Text, " +
            "picture Text, " +
            "type Text, " +
            "created_at Text)";

    private static final String CREATE_TABLE_GRADE = "CREATE TABLE " + TB_GRADE +
            "(grade_id Text, " +
            "name Text, " +
            "created_at Text)";

    private static final String CREATE_TABLE_LOCATION = "CREATE TABLE " + TB_LOCATION +
            "(location_id Text, " +
            "name Text, " +
            "created_at Text)";

    private static final String CREATE_TABLE_DELIVERY_ORDER = "CREATE TABLE " + TB_DELIVERY_ORDER +
            "(do_id Text, " +
            "deliver_driver_id Text, " +
            "customer_id Text, " +
            "date Text)";

    private static final String CREATE_TABLE_STOCK_OUT = "CREATE TABLE " + TB_STOCK_OUT +
            "(stock_out_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "local_do_id Text, " +
            "do_id Text, " +
            "customer_id Text, " +
            "product_id Text, " +
            "stock_date Text, " +
            "grade Text, " +
            "location Text, " +
            "weight Text, " +
            "quantity Text, " +
            "price Text)";

    private static final String CREATE_TABLE_PURCHASE_ORDER = "CREATE TABLE " + TB_PURCHASE_ORDER +
            "(po_id Text, " +
            "driver_id Text, " +
            "farmer_id Text, " +
            "date Text)";

    private static final String CREATE_TABLE_STOCK_IN = "CREATE TABLE " + TB_STOCK_IN +
            "(stock_in_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "local_po_id Text, " +
            "po_id Text, " +
            "farmer_id Text, " +
            "product_id Text, " +
            "grade Text, " +
            "location Text, " +
            "weight Text, " +
            "quantity Text, " +
            "price Text, " +
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

        sqLiteDatabase.execSQL(CREATE_TABLE_PRODUCT);

        sqLiteDatabase.execSQL(CREATE_TABLE_GRADE);
        sqLiteDatabase.execSQL(CREATE_TABLE_LOCATION);

        sqLiteDatabase.execSQL(CREATE_TABLE_PURCHASE_ORDER);
        sqLiteDatabase.execSQL(CREATE_TABLE_STOCK_IN);

        sqLiteDatabase.execSQL(CREATE_TABLE_DELIVERY_ORDER);
        sqLiteDatabase.execSQL(CREATE_TABLE_STOCK_OUT);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TB_FAVOURITE_DRIVER);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TB_FAVOURITE_FARMER);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TB_FAVOURITE_CUSTOMER);

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TB_FARMER);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TB_CUSTOMER);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TB_DRIVER);

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TB_PRODUCT);

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TB_GRADE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TB_LOCATION);

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TB_DELIVERY_ORDER);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TB_STOCK_OUT);

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TB_PURCHASE_ORDER);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TB_STOCK_IN);
        onCreate(sqLiteDatabase);
    }
}
