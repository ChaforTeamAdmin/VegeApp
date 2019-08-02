package com.jby.admin.sharePreference;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by wypan on 2/24/2017.
 */

public class SharedPreferenceManager {


    private static String LanguageId = "language_id";
    private static String UserId = "user_id";
    private static String Username = "username";
    private static String DayLimit = "day_limit";
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
}
