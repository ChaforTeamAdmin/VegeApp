package com.jby.admin.sharePreference;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by wypan on 2/24/2017.
 */

public class SharedPreferenceManager {

    private static String UserId = "user_id";
    private static String Username = "username";
    private static String DayLimit = "day_limit";
    private static String Grade = "grade";
    private static String Location = "location";
    private static String Price = "price";

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

    public static String getUserId(Context context) {
        return getSharedPreferences(context).getString(UserId, "default");
    }

    public static void setUserId(Context context, String userId) {
        getSharedPreferences(context).edit().putString(UserId, userId).apply();
    }

    public static String getUsername(Context context) {
        return getSharedPreferences(context).getString(Username, "default");
    }

    public static void setUsername(Context context, String username) {
        getSharedPreferences(context).edit().putString(Username, username).apply();
    }

    public static boolean getShowNotification(Context context, String notification) {
        return getSharedPreferences(context).getBoolean(notification, true);
    }

    public static void setShowNotification(Context context, String notification, boolean showNotification) {
        getSharedPreferences(context).edit().putBoolean(notification, showNotification).apply();
    }

    public static String getDayLimit(Context context) {
        return getSharedPreferences(context).getString(DayLimit, "5");
    }

    public static void setDayLimit(Context context, String dayLimit) {
        getSharedPreferences(context).edit().putString(DayLimit, dayLimit).apply();
    }


    public static boolean getGrade(Context context) {
        return getSharedPreferences(context).getBoolean(Grade, true);
    }

    public static void setGrade(Context context, boolean grade) {
        getSharedPreferences(context).edit().putBoolean(Grade, grade).apply();
    }

    public static boolean getLocation(Context context) {
        return getSharedPreferences(context).getBoolean(Location, true);
    }

    public static void setLocation(Context context, boolean location) {
        getSharedPreferences(context).edit().putBoolean(Location, location).apply();
    }

    public static boolean getPrice(Context context) {
        return getSharedPreferences(context).getBoolean(Price, true);
    }

    public static void setPrice(Context context, boolean price) {
        getSharedPreferences(context).edit().putBoolean(Price, price).apply();
    }
}
