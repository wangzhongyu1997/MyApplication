package com.example.chuntiao.myapplication;


import android.content.Context;
import android.content.SharedPreferences;

import java.sql.Date;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by chuntiao on 17-1-27.
 * to Save global variable
 */

public final class Data {
    static int thisWeek;
    static Date semesterStart;
    static boolean ShowDisabledCourses;
    static boolean rememberPass;
    static String cookieJSESSIONID;
    static WatingDialogActivity theDialog;
    static Object obj;
    static String num;
    static String password;
    public static void getSetting(Context activity)
    {
        obj=null;
        SharedPreferences sharedPreferences = activity.getSharedPreferences("data", MODE_PRIVATE);

        String strStart=sharedPreferences.getString("semester_start", "2017-02-20");
        Date start=Date.valueOf(strStart);
        long now=System.currentTimeMillis();

        semesterStart=start;
        thisWeek=(int)((now-start.getTime())/(24 * 60 * 60 * 1000*7)+1);
        ShowDisabledCourses =sharedPreferences.getBoolean("show_disabled_courses",false);
        cookieJSESSIONID=sharedPreferences.getString("JSESSIONID","JSESSIONID=DB4FBC41BD3B6B55422C070C0673B3E3");
        rememberPass=sharedPreferences.getBoolean("rememberPass",false);
        num=sharedPreferences.getString("num","20150000");
        password =sharedPreferences.getString("password","0000");
    }
}
