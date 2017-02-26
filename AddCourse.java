package com.example.chuntiao.myapplication;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

public class AddCourse extends AppCompatActivity {
    private final String DB="course.db";
    private final String table="my_table";
    private Spinner spinner_course_start;
    private Spinner spinner_course_end;
    private Spinner spinner_week;
    private Button button_add;
    private DataBaseHp DBHP;
    private static final String TAG = "AddCourse";
    private EditText edit_teacher;
    int a=0;
    private EditText edit_name;;
    private EditText edit_location;
    private RadioGroup radioGroup;
    private EditText edit_start_week;
    private EditText edit_end_week;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);

        final String courses[]={"第1节","第2节","第3节","第4节","第5节","第6节","第7节","第8节","第9节","第10节","第11节","第12节"};
        final String week[]={"周一","周二","周三","周四","周五","周六","周日"};

        ArrayAdapter<String> adapter_course1=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, courses);
        adapter_course1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<String> adapter_course2=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, courses);
        adapter_course2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<String> adapter_week=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, week);
        adapter_week.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner_course_end=(Spinner)findViewById(R.id.spinner_end);
        spinner_course_start=(Spinner)findViewById(R.id.spinner_start);
        spinner_week=(Spinner)findViewById(R.id.spinner_week);
        radioGroup=(RadioGroup)findViewById(R.id.radioGroup);
        edit_end_week=(EditText)findViewById(R.id.week_end);
        edit_start_week=(EditText)findViewById(R.id.week_start);

        spinner_week.setAdapter(adapter_week);
        spinner_course_start.setAdapter(adapter_course1);
        spinner_course_end.setAdapter(adapter_course2);

        int position=getIntent().getIntExtra("position",0);
        Log.d(TAG, "onCreate: position"+position);
        int theStart=position/7+1;
        int theWeek=position%7+1;
        spinner_week.setSelection(theWeek-1);//Item's position from GridView .makes adding a course easier
        spinner_course_start.setSelection(theStart-1);
        spinner_course_end.setSelection(theStart);

        button_add=(Button)findViewById(R.id.button_add);

        edit_location=(EditText)findViewById(R.id.edit_location);
        edit_name=(EditText)findViewById(R.id.edit_name);
        edit_teacher=(EditText)findViewById(R.id.edit_teacher);

        button_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues cValue;
                cValue=new ContentValues();
                DBHP=new DataBaseHp(DB,AddCourse.this);
                DBHP.check(table);

                Log.d(TAG, "onClick: 打开数据库并检查数据表成功");

                int week=spinner_week.getSelectedItemPosition()+1;
                int start=spinner_course_start.getSelectedItemPosition()+1;
                int length=spinner_course_end.getSelectedItemPosition()-spinner_course_start.getSelectedItemPosition()+1;
                cValue.put("week",week);
                cValue.put("start",start);
                cValue.put("length",length);
                String name=edit_name.getText().toString();
                String teacher=edit_teacher.getText().toString();
                String location=edit_location.getText().toString();

                cValue.put("name",name);
                cValue.put("room",location);
                cValue.put("teacher",teacher);
                int week_model=0;
                switch (radioGroup.getCheckedRadioButtonId())
                {
                    case R.id.radio_every_week:
                        week_model=0;break;
                    case R.id.radio_odd:
                        week_model=1;break;
                    case R.id.radio_even:
                        week_model=2;
                }
                cValue.put("week_model",week_model);

                int week_start=Integer.parseInt(edit_start_week.getText().toString());
                int week_end=Integer.parseInt(edit_end_week.getText().toString());

                cValue.put("week_end",week_end);
                cValue.put("week_start",week_start);

                long a= DBHP.db.insert(table,null,cValue);
                DBHP.db.close();
                DBHP=null;
                Intent intent=new Intent();
                Bundle bundle=new Bundle();

                bundle.putInt("week",week);
                bundle.putInt("start",start);
                bundle.putInt("length",length);
                bundle.putString("name",name);
                bundle.putString("room",location);
                bundle.putInt("week_start",week_start);
                bundle.putInt("week_end",week_end);
                bundle.putInt("week_model",week_model);

                intent.putExtra("data",bundle);
                setResult(RESULT_OK,intent);
                finish();
            }
        });

        radioGroup=(RadioGroup)findViewById(R.id.radioGroup);
        RadioButton everyWeek=(RadioButton)findViewById(R.id.radio_every_week);
        everyWeek.setChecked(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }



    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home)
            finish();
        return true;
    }
}
