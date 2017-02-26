package com.example.chuntiao.myapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by wang on 16-11-5.
 */

public class DataBaseHp {
    String DBname;
    SQLiteDatabase db;
    String path;
    private static final String TAG = "DataBaseHp";

    DataBaseHp(String DbName, Context context)
    {
        this.DBname=DbName;
        path=context.getFilesDir().getPath();
        db=SQLiteDatabase.openOrCreateDatabase(path+"/"+DBname,null);
        Log.d(TAG, "DataBaseHp: Open database successfully path "+path);
    }
    
    public void check(String table_name)
    {
        String course_table="create table  if not exists  "+ table_name+"(week_model tinyint,week_end tinyint,week_start tinyint,week tinyint,name varchar,room varchar,start tinyint,length tinyint,teacher varchar )";
        db.execSQL(course_table);
        Log.d(TAG, "check: process  table successfully "+table_name);
    }
    public void clear(String table_name){



        String sql ="drop table  if exists  "+table_name+"; ";
        String course_table="create table  if not exists  "+ table_name+"(week_model tinyint,week_end tinyint,week_start tinyint,week tinyint,name varchar,room varchar,start tinyint,length tinyint,teacher varchar )";
        db.execSQL(sql);
        db.execSQL(course_table);
    }

    public  void  delete(String Table)
    {
        String SqlDel=" Drop table "+Table;
        db.execSQL(Table);
    }
    public SQLiteDatabase getDb()
    {
        return db;
    }
}

