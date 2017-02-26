package com.example.chuntiao.myapplication;

import java.util.ArrayList;

/**
 * Created by chuntiao on 17-1-22.
 */

public class Course {
    String name;
    String location;
    String teacher;
    int start;
    int length;
    int week;

    int week_start;
    int week_end;
    int week_model;//0 as all; 1 as odd ;2 as even
    Course next;
    static ArrayList<Course> courseList= new ArrayList<Course>();

    Course()
    {
        this.name="";
        this.location="";
        this.teacher="";
        this.start=0;
        this.length=0;
        this.next=null;
        this.week=0;
        week_start=0;
        week_end=0;
    }


    @Override
    public boolean equals(Object obj) {//if  their time equal then they repeat and the equal
        Course another=(Course)obj;
        return (this.week==another.week)&&(this.start==another.start);
    }
}
