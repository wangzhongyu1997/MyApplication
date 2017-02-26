package com.example.chuntiao.myapplication;



import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.widget.CheckBox;

import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;

import java.sql.Date;
import java.util.Calendar;

import static android.icu.util.Calendar.SUNDAY;

public class SettingActivity extends AppCompatActivity {

    private CheckBox show_disabled_courses;
    private EditText edit_thisWeek;
    private DatePicker datePicker;
    private final Calendar calendar =Calendar.getInstance();
    private static final String TAG = "SettingActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

//        Data.getSetting(this);

        show_disabled_courses=(CheckBox)findViewById(R.id.check_disabled_courses);
        edit_thisWeek=(EditText)findViewById(R.id.edit_this_week);
        datePicker=(DatePicker)findViewById(R.id.datePicker);


        calendar.setTime(Data.semesterStart);
        int year=calendar.get(Calendar.YEAR);
        int month=calendar.get(Calendar.MONTH);
        int date=calendar.get(Calendar.DAY_OF_MONTH);
        edit_thisWeek.setText(""+Data.thisWeek);

        show_disabled_courses.setChecked(Data.ShowDisabledCourses);
        show_disabled_courses.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Data.ShowDisabledCourses=isChecked;
            }
        });


        datePicker.init(year, month, date, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calendar.set(year,monthOfYear,dayOfMonth);
                Log.d(TAG, "onDateChanged: 触点"+year+' '+monthOfYear+' '+dayOfMonth);

                int dayOfWeek=calendar.get(Calendar.DAY_OF_WEEK);
//                long time=calendar.getTime().getTime();

                if(dayOfWeek==SUNDAY)
                    calendar.add(Calendar.DAY_OF_YEAR, -6);
                else
                   for (int i=0;i<dayOfWeek-2;i++)
                       calendar.add(Calendar.DAY_OF_YEAR, -1);

                int theWeek=(int)((System.currentTimeMillis()-calendar.getTime().getTime())/(24 * 60 * 60 * 1000*7)+1);

                edit_thisWeek.setText(""+theWeek);

                Data.thisWeek=theWeek;
                Data.semesterStart.setTime(calendar.getTime().getTime());
                Log.d(TAG, "onDateChanged: "+Data.semesterStart.toString());
            }
        });
        edit_thisWeek.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().equals("")||s.toString().equals("-"))
                    return;
                long time=System.currentTimeMillis();

                int week_ordinal=Integer.parseInt(edit_thisWeek.getText().toString());
//                time-=(week_ordinal-1)*7*24 * 60 * 60 * 1000;//这里加一句中文注释：回到解放前

                calendar.setTime(new Date(time));
                calendar.add(Calendar.WEEK_OF_YEAR, -(week_ordinal-1));

                int dayOfWeek=calendar.get(Calendar.DAY_OF_WEEK);

                if(dayOfWeek==SUNDAY)
//                    time-=24 * 60 * 60 * 1000*6;
                    calendar.add(Calendar.DAY_OF_YEAR, -6);
                else
                    for (int i=0;i<dayOfWeek-2;i++)
//                        time-=24 * 60 * 60 * 1000;
                        calendar.add(Calendar.DAY_OF_YEAR, -1);

                Data.semesterStart.setTime(calendar.getTime().getTime());
                Data.thisWeek=week_ordinal;
                Log.d(TAG, "afterTextChanged: "+time+' '+Data.semesterStart.toString());
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//<-

    }

    @Override
    public void finish() {

        SharedPreferences.Editor editor= getSharedPreferences("data",MODE_PRIVATE).edit();

        editor.putString("semester_start",Data.semesterStart.toString());
        editor.putBoolean("show_disabled_courses",show_disabled_courses.isChecked());
        editor.apply();

        super.finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home)
            finish();
        return true;
    }
}
