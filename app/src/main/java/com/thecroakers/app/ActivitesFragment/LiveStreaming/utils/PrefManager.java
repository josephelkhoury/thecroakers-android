package com.thecroakers.app.ActivitesFragment.LiveStreaming.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.thecroakers.app.ActivitesFragment.LiveStreaming.Constants;


public class PrefManager {
    public static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    }
}
