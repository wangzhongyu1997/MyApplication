package com.example.chuntiao.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class ListDialogActivity extends AppCompatActivity {

    private List<Course> courses=new ArrayList<>();
    private CourseDetailAdopter adopter;
    private ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_dialog);

        setTitle("详细信息");
        listView=(ListView)findViewById(R.id.list_courses);
        Course courseNow=(Course)Data.obj;
        while (courseNow!=null)
        {
            courses.add(courseNow);
            courseNow=courseNow.next;
        }
        adopter=new CourseDetailAdopter(this,R.layout.item_detail_course,courses);
        listView.setAdapter(adopter);
    }
}
