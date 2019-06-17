package com.jby.vegeapp.sharePreference;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by wypan on 2/24/2017.
 */

public class SharedPreferenceManager {


    private static String LanguageId = "language_id";
    private static String UserId = "user_id";
    private static String UserType = "user_type";
    private static String Username = "username";
    private static String Phone = "phone";
    private static String PickUpDefaultFarmer = "pickUpDefaultFarmer";
    private static String BasketDefaultFarmer = "basketDefaultFarmer";
    private static String BasketDefaultCustomer = "basketDefaultCustomer";
    /*
     * printer purpose
     * */
    private static String BlueToothName = "blueToothName";
    private static String BlueToothAddress = "blueToothAddress";

    private static SharedPreferences getSharedPreferences(Context context) {
        String SharedPreferenceFileName = "VegeApp";
        return context.getSharedPreferences(SharedPreferenceFileName, Context.MODE_PRIVATE);
    }

    public static void clear(Context context) {
        getSharedPreferences(context).edit().clear().apply();
    }

    /*
     *       User Shared Preference
     * */

    public static String getLanguageId(Context context) {
        return getSharedPreferences(context).getString(LanguageId, "default");
    }

    public static void setLanguageId(Context context, String languageId) {
        getSharedPreferences(context).edit().putString(LanguageId, languageId).apply();
    }

    public static String getUserId(Context context) {
        return getSharedPreferences(context).getString(UserId, "default");
    }

    public static void setUserId(Context context, String userId) {
        getSharedPreferences(context).edit().putString(UserId, userId).apply();
    }

    public static String getUserType(Context context) {
        return getSharedPreferences(context).getString(UserType, "default");
    }

    public static void setUserType(Context context, String userType) {
        getSharedPreferences(context).edit().putString(UserType, userType).apply();
    }

    public static String getUsername(Context context) {
        return getSharedPreferences(context).getString(Username, "default");
    }

    public static void setUsername(Context context, String username) {
        getSharedPreferences(context).edit().putString(Username, username).apply();
    }

    public static String getPhone(Context context) {
        return getSharedPreferences(context).getString(Phone, "default");
    }

    public static void setPhone(Context context, String phone) {
        getSharedPreferences(context).edit().putString(Phone, phone).apply();
    }

    public static String getPickUpDefaultFarmer(Context context) {
        return getSharedPreferences(context).getString(PickUpDefaultFarmer, "default");
    }

    public static void setPickUpDefaultFarmer(Context context, String pickUpDefaultFarmer) {
        getSharedPreferences(context).edit().putString(PickUpDefaultFarmer, pickUpDefaultFarmer).apply();
    }

    public static String getBasketDefaultFarmer(Context context) {
        return getSharedPreferences(context).getString(BasketDefaultFarmer, "default");
    }

    public static void setBasketDefaultFarmer(Context context, String basketDefaultFarmer) {
        getSharedPreferences(context).edit().putString(BasketDefaultFarmer, basketDefaultFarmer).apply();
    }

    public static String getBasketDefaultCustomer(Context context) {
        return getSharedPreferences(context).getString(BasketDefaultCustomer, "default");
    }

    public static void setBasketDefaultCustomer(Context context, String basketDefaultCustomer) {
        getSharedPreferences(context).edit().putString(BasketDefaultCustomer, basketDefaultCustomer).apply();
    }

    /*
     * print purpose
     * */

    public static void setBlueToothName(Context context, String blueToothName) {
        getSharedPreferences(context).edit().putString(BlueToothName, blueToothName).apply();
    }

    public static String getBluetoothName(Context context) {
        return getSharedPreferences(context).getString(BlueToothName, "default");
    }

    public static void setBlueToothAddress(Context context, String blueToothAddress) {
        getSharedPreferences(context).edit().putString(BlueToothAddress, blueToothAddress).apply();
    }

    public static String getBluetoothAddress(Context context) {
        return getSharedPreferences(context).getString(BlueToothAddress, "default");
    }
}
