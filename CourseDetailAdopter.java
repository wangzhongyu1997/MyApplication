package com.example.chuntiao.myapplication;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by chuntiao on 17-2-25.
 */

public class CourseDetailAdopter extends ArrayAdapter<Course> {
    private int xmlId;
    public CourseDetailAdopter(Context context, int resource, List<Course> objects) {
        super(context, resource, objects);
        xmlId=resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Course course=getItem(position);

        View view= LayoutInflater.from(getContext()).inflate(xmlId,parent,false);
        TextView name=(TextView) view.findViewById(R.id.text_name);
        TextView location=(TextView)view.findViewById(R.id.text_location);
        TextView time=(TextView)view.findViewById(R.id.text_time);
        TextView teacher=(TextView)view.findViewById(R.id.text_teacher);

        name.setText(course.name);
        location.setText(course.location);
        String timeAll=""+course.week_start+"-"+course.week_end;
        if(course.week_model==2)
            timeAll=timeAll+"双周";
        else if(course.week_model==1)
            timeAll=timeAll+"单周";
        else timeAll=timeAll+"周";
        timeAll+="  "+course.start+"-"+(course.start+course.length-1)+"节";
        time.setText(timeAll);
        teacher.setText(course.teacher);
        return view;
    }
}
