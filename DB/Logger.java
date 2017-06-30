package com.lynkteam.tapmanager.DB;

import android.content.Context;

import com.lynkteam.tapmanager.DB.DBHelper;

/**
 * Created by robertov on 18/08/15.
 */
public class Logger {

    public static void Info(Context context, String info){
        DBHelper.getInstance(context).saveLog("I", info, "");

    }

    public static void Error(Context context, String error, String exception){
        DBHelper.getInstance(context).saveLog("E", error, exception);
    }
}
