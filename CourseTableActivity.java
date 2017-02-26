package com.example.chuntiao.myapplication;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CourseTableActivity extends AppCompatActivity {
    private static final String TAG = "CourseTableActivity";
    private List<CourseV> courseVs=new ArrayList<>();
    private List<Integer> colors=new ArrayList<>();
    private final String DB="course.db";
    private final String table="my_table";
    private final int frontSize=11;
    private DataBaseHp DBHP;
    private MyAdepter adepter;
    private TextView  text_week;
    private Handler handler;


    boolean SHOW_DISABLED_COURSES=false;
    int ThisWeek=1;

    private int courseNum=0;//It is not the real courseNum, 'i just wanna make the color go circled. It starts from -1 and the color starts from color1
    int week_col;
    int name_col;
    int room_col;
    int start_col;
    int length_col;
    int teacher_col;
    int week_end_col;
    int week_start_col;
    int week_model_col;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_table);


        handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {

                if(msg.what==0)//return from NetThread normally
                {
                    Data.theDialog.finish();
                    DBHP.db.close();
                    getDataFromDB_To_courseVs(DB);
                    adepter.notifyDataSetChanged();
                }
                else if(msg.what==-1)
                {
                    Data.theDialog.finish();
                    Intent intent=new Intent();
                    intent.setClass(CourseTableActivity.this,LogInActivity.class);
                    startActivityForResult(intent,4);// SESSIONID out of date, then login
                }
                else if(msg.what==-2)
                {
                    Data.theDialog.btn_ok.setVisibility(View.VISIBLE);
                    Data.theDialog.setTitle("出错了！");
                    Data.theDialog.progressBar.setVisibility(View.GONE);
                    Data.theDialog.sentence.setText("请确保连上校园wifi");
                }
                else if(msg.what==-3)
                {
                    Data.theDialog.btn_ok.setVisibility(View.VISIBLE);
                    Data.theDialog.setTitle("出错了！");
                    Data.theDialog.progressBar.setVisibility(View.GONE);
                    Data.theDialog.sentence.setText("连接超时，可能你联网了，但是只有校园网才可以哦！");
                }
            }
        };
        colors.add(R.color.color1);
        colors.add(R.color.color2);
        colors.add(R.color.color3);
        colors.add(R.color.color4);
        colors.add(R.color.color9);
        colors.add(R.color.color6);
        colors.add(R.color.color7);
        colors.add(R.color.color5);


        for (int i=0;i<84;i++)
            courseVs.add(new CourseV());

        getDataFromDB_To_courseVs(DB);

        text_week=(TextView)findViewById(R.id.text_this_week);
        text_week.setText(""+ThisWeek);

        adepter=new MyAdepter(CourseTableActivity.this,R.layout.item_layout,courseVs);
        GridView gridView = (GridView) findViewById(R.id.gridView);
        gridView.setAdapter(adepter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(courseVs.get(position).state!=0)
                {
                    Intent intent=new Intent();
                    intent.setClass(CourseTableActivity.this,ListDialogActivity.class);
                    Data.obj=courseVs.get(position).relative;
                    startActivity(intent);
                }
                if(courseVs.get(position).state == 0)
                    Toast.makeText(CourseTableActivity.this,"长按空白处可以在此添加课程",Toast.LENGTH_SHORT).show();

            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                CourseV course=courseVs.get(position);
                if(course.state!=0)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(CourseTableActivity.this);
                    builder.setIcon(android.R.drawable.ic_dialog_alert);
                    builder.setTitle("警告");
                    builder.setMessage("是否删除此处课程？！");

                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int pos=position;
                            while (courseVs.get(pos).state!=1)//find the head of the course
                                pos-=7;
                            final int posSolid=pos;

                            courseVs.get(pos).state=0;//change state of head
                            while (courseVs.get(pos).state!=1)//until next head
                            {
                                courseVs.get(pos).state=0;
                                pos+=7;
                            }
                            int week=posSolid%7+1;
                            int start=posSolid/7+1;
                            adepter.notifyDataSetChanged();
                            DBHP.db.execSQL("delete from "+table+" where week ="+week+" and start="+start);
                            Log.d(TAG, "onClick: 删除此课程");

                        }
                    });

                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else
                {
                    Intent intent=new Intent();
                    intent.setClass(CourseTableActivity.this,AddCourse.class);
                    intent.putExtra("position",position);
                    startActivityForResult(intent,1);
                    Log.d(TAG, "onItemLongClick: 开启活动 AddCourse");
                }
                return true;
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.course_table_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        switch (id)
        {
            case android.R.id.home:
                finish();break;

            case R.id.menu_clear: {
                AlertDialog.Builder builder = new AlertDialog.Builder(CourseTableActivity.this);
                builder.setIcon(android.R.drawable.ic_dialog_alert);
                builder.setTitle("警告");
                builder.setMessage("是否删除所有课程？！");

                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DBHP.clear(table);
                        for (CourseV c:courseVs) {
                            if(c.state!=0)
                                c.state=0;
                        }
                        adepter.notifyDataSetChanged();
                        Log.d(TAG, "onClick: 删除所有课程");
                    }
                });

                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            }
            case R.id.menu_add:
            {
                Intent intent=new Intent();
                intent.setClass(CourseTableActivity.this,AddCourse.class);
                startActivityForResult(intent,1);
                break;
            }

            case R.id.menu_connect:
            {
                Intent intent=new Intent();
                intent.setClass(CourseTableActivity.this,WatingDialogActivity.class);
                intent.putExtra("title","请稍候");
                intent.putExtra("sentence","加载中");
                startActivity(intent);
                connectCompuseServerAndExe();
                break;
            }
            case R.id.menu_time_out:
                SharedPreferences.Editor editor=getSharedPreferences("data",MODE_PRIVATE).edit();
                editor.putString("JSESSIONID","hhhhh");
                Data.cookieJSESSIONID="hhh";
                editor.apply();
                break;

            case R.id.menu_setting:
            {
                Intent intent=new Intent();
                intent.setClass(CourseTableActivity.this,SettingActivity.class);
                startActivityForResult(intent,3);
                break;
            }

        }
        return true;
    }

    class CourseV
    {
        String text;
        int state;
        int color_index;
        boolean onlyOne;
        boolean showPoint;
        Course relative;
        @Override
        public boolean equals(Object obj) {//serves for search to keep the same course has the same color
            CourseV another=(CourseV)obj;
            return  !another.text.equals("") &&another.text.equals(this.text);
        }

        CourseV()
        {
            text="";
            state=0;//0 as no class,1 as first class,2 as second,3 as others
            color_index=-1;
            onlyOne=false;
            showPoint=false;
            relative=null;
        }
    }
    class MyAdepter extends ArrayAdapter<CourseV>
    {

        int recourceID;
        public MyAdepter(Context context, int resource, List<CourseV> objects) {
            super(context, resource, objects);
            this.recourceID=resource;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CourseV course=getItem(position);
            View view= LayoutInflater.from(getContext()).inflate(recourceID,parent,false);

            View gap=view.findViewById(R.id.gapID);
            TextView text=(TextView) view.findViewById(R.id.texts);
            LinearLayout cell_panel=(LinearLayout) view.findViewById(R.id.item_panel);


            if(course.state!=0)//some course at there
            {
                cell_panel.setBackgroundColor(getResources().getColor(colors.get(course.color_index%8)) );
//                gap.setBackgroundColor(getResources().getColor(colors.get(course.color_index%6)) );//that well make color deeper
            }

            if(course.state==1)//first class
            {
                if(course.showPoint)
                {
                    ImageView imageV=(ImageView)view.findViewById(R.id.imageView);
                    imageV.setVisibility(View.VISIBLE);
                }

                gap.setBackgroundColor(getResources().getColor(R.color.colorGap));//separate with a Gap
                text.setGravity(Gravity.BOTTOM);
                text.setText(course.text);
                text.setTextSize(TypedValue.COMPLEX_UNIT_PX,31-(int)(0.8*course.text.length()));//I have tried many times to find the probable hanshu"31-(int)(0.8*course.text.length())"
                if (course.onlyOne)//make Front less
                    text.setTextSize(TypedValue.COMPLEX_UNIT_SP,frontSize);
            }
            else if(course.state==2)
            {
                text.setText(course.text);
                text.setGravity(Gravity.TOP);
            }


            return view;
        }
    }

    @Override
    protected void onDestroy() {
        DBHP.db.close();
        super.onDestroy();
        Log.d(TAG, "onDestroy: 关闭数据库");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode)
        {
            case 1:
                if(resultCode==RESULT_OK)
                {
                    Log.d(TAG, "onActivityResult: 成功返回");

                    courseNum++;
                    Bundle bundle=data.getBundleExtra("data");

                    Course course=new Course();
                    course.week_end=bundle.getInt("week_end");
                    course.week_start=bundle.getInt("week_start");
                    course.week_model=bundle.getInt("week_modle");

                    if(!weekMatch(course))
                        break;
                    int length=bundle.getInt("length");
                    int start=bundle.getInt("start");
                    int week=bundle.getInt("week");
                    String name=bundle.getString("name");
                    String room=bundle.getString("room");

                    final CourseV temp_course=new CourseV();
                    temp_course.text=name;
                    boolean exists=courseVs.contains(temp_course);

                    int index=(start-1)*7+week-1;
                    for(int i=0;i<length;i++)
                    {
                        CourseV courseV= courseVs.get(index+i*7);

                        if(i==0){

                            if(!exists)
                                courseV.color_index= courseNum;
                            else
                                courseV.color_index=courseVs.get(courseVs.indexOf(temp_course)).color_index;

                            courseV.text=name;
                            courseV.state=1;
                            if(length==1){
                                courseV.text += '@'+room;
                                courseV.onlyOne=true;
                                break;
                            }
                        }
                        else if(i==1){
                            courseV.color_index=courseVs.get(index).color_index;
                            courseV.text='@'+room;
                            courseV.state=2;
                        }
                        else {
                            courseV.color_index=courseVs.get(index).color_index;
                            courseV.state=3;
                        }

                    }
                    adepter.notifyDataSetChanged();
                }
                else Log.d(TAG, "onActivityResult: 返回不成功");
                break;
            case 3:
                DBHP.db.close();
                getDataFromDB_To_courseVs(DB);
                adepter.notifyDataSetChanged();
                text_week.setText(""+ThisWeek);
                break;
            case 4://递归一次：设置中的从教务系统导
                if(resultCode==RESULT_OK){
                    Intent intent=new Intent();
                    intent.setClass(CourseTableActivity.this,WatingDialogActivity.class);
                    intent.putExtra("title","请稍候");
                    intent.putExtra("sentence","加载中");
                    startActivity(intent);
                    connectCompuseServerAndExe();
                }
                break;

        }

    }

    void getDataFromDB_To_courseVs(String dbName){


        //refresh settings
        Data.getSetting(CourseTableActivity.this);
        ThisWeek=Data.thisWeek;
        SHOW_DISABLED_COURSES=Data.ShowDisabledCourses;

        //read data
        DBHP=new DataBaseHp(dbName,CourseTableActivity.this);
        DBHP.check(table);

        for (CourseV c:courseVs) {
            c.state=0;
        }
        Course.courseList.clear();

        final String col[]={"week","name","room","start","length","teacher",  "week_start","week_end","week_model"};
        Cursor cursor=DBHP.db.query(table,col,null,null,null,null,"week");

        week_col=cursor.getColumnIndex("week");
        name_col=cursor.getColumnIndex("name");
        room_col=cursor.getColumnIndex("room");
        start_col=cursor.getColumnIndex("start");
        length_col=cursor.getColumnIndex("length");
        teacher_col=cursor.getColumnIndex("teacher");
        week_start_col=cursor.getColumnIndex("week_start");
        week_end_col=cursor.getColumnIndex("week_end");
        week_model_col=cursor.getColumnIndex("week_model");

        //process data into Course.courseList
        //to make color different


        for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext())
        {
            Course temp_course=new Course();
            temp_course.length=cursor.getInt(length_col);
            temp_course.start=cursor.getInt(start_col);
            temp_course.week=cursor.getInt(week_col);
            temp_course.week_start=cursor.getInt(week_start_col);
            temp_course.week_end=cursor.getInt(week_end_col);
            temp_course.week_model=cursor.getInt(week_model_col);

            temp_course.name=cursor.getString(name_col);
            temp_course.location=cursor.getString(room_col);
            temp_course.teacher=cursor.getString(teacher_col);

                                                        //attention !! I had Override equals method
            if(!Course.courseList.contains(temp_course))//its time repeats not same
                Course.courseList.add(temp_course);
            else {
                Course courseInList=Course.courseList.get(Course.courseList.indexOf(temp_course));

                if(weekMatchStrict(courseInList))
                {
                    temp_course.next=courseInList.next;
                    courseInList.next=temp_course;
                }
                else
                {
                    Course.courseList.set(Course.courseList.indexOf(temp_course),temp_course);
                    temp_course.next=courseInList;//尽可能保证weekMatchStrict的课位于链表头
                }

            }
        }
        cursor.close();



        for(int j=0;j<Course.courseList.size();j++)
        {
            Course course=Course.courseList.get(j);
            boolean showPoint=(course.next!=null);//if there is only one course on a position, don't show the point
            while (course!=null)
            {
                if(weekMatch(course))
                    break;
                course=course.next;
            }
            if (course==null)
                continue;

            final CourseV temp_courseV=new CourseV();//I only want to use its name
            courseNum++;
            int length=course.length;
            int start=course.start;
            int week=course.week;
            int index=(start-1)*7+week-1;//Attention!! Item in GridView is placed horizontal. I have to make them seems vertical
            temp_courseV.text=course.name;
            boolean exists=courseVs.contains(temp_courseV);//To make the same subject the same color


            for(int i=0;i<length;i++)//obtain every little class in a course
            {
                CourseV courseV= courseVs.get(index+i*7);
                courseV.relative=course;//establish relationship to access easily

                if(i==0){
                    if(!exists)
                        courseV.color_index= courseNum;
                    else
                        courseV.color_index=courseVs.get(courseVs.indexOf(temp_courseV)).color_index;//To make the same subject the same color

                    courseV.text=course.name;
                    courseV.showPoint=showPoint;
                    courseV.state=1;
                    if(length==1){//if there is only one little course in a big course,I'll make Location and Name in one line.,,,later, adjust the text size
                        courseV.text+='@'+course.location;
                        courseV.onlyOne=true;
                        break;
                    }
                }
                else if(i==1){
                    courseV.color_index=courseVs.get(index).color_index;
                    courseV.text='@'+course.location;
                    courseV.state=2;
                }
                else {
                    courseV.color_index=courseVs.get(index).color_index;
                    courseV.state=3;
                }

            }
        }
    }

    private boolean weekMatch(Course c) {
       /* switch (showModel) {
            case SHOW_DISABLED_COURSES:
                if(c.week_model==0)
                    return true;
                else return c.week_model%2==ThisWeek%2;
            default:
                if(c.week_start<=ThisWeek&&ThisWeek<=c.week_end)
                    if(c.week_model==0)
                        return true;
                    else return c.week_model%2==ThisWeek%2;
                else return false;
        }*/
        if(SHOW_DISABLED_COURSES)
        {
            if(c.week_model==0)
                return true;
            else return c.week_model%2==ThisWeek%2;
        }
        else
        {
            if(c.week_start<=ThisWeek&&ThisWeek<=c.week_end)
                if(c.week_model==0)
                    return true;
                else return c.week_model%2==ThisWeek%2;
            else return false;
        }
    }
    private boolean weekMatchStrict(Course c) {

        {
            if(c.week_start<=ThisWeek&&ThisWeek<=c.week_end)
                if(c.week_model==0)
                    return true;
                else return c.week_model%2==ThisWeek%2;
            else return false;
        }
    }


    void connectCompuseServerAndExe(){


        Thread NetThread=new Thread(){
            @Override
            public void run() {
                OkHttpClient client=new OkHttpClient();
                Request request=new Request.Builder()
                        .url("http://deanonline.stdu.edu.cn/academic/student/currcourse/currcourse.jsdo?groupId=&moduleId=2000")
                        .addHeader("Host","deanonline.stdu.edu.cn")
//                        .addHeader("User-Agent"," Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:50.0) Gecko/20100101 Firefox/50.0")
                        .addHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                        .addHeader("Accept-Language","en-US,en;q=0.5")
                        .addHeader("Accept-Encoding","gzip, deflate")
                        .addHeader("Referer","http://deanonline.stdu.edu.cn/academic/listLeft.do")
                        .addHeader("Cookie",Data.cookieJSESSIONID)
                        .addHeader("Connection","keep-alive")
                        .addHeader("Upgrade-Insecure-Requests","1")
                        .build();
                try {
                    Response response=client.newCall(request).execute();
                    String html=response.body().string();
                    updateFromHtml(html);
//                    System.out.print("NetThread run: "+response.body().string());
                    Log.d(TAG, "NetThread run: "+html);
                    Message msgOk=handler.obtainMessage();
                    msgOk.what=0;
                    handler.sendMessage(msgOk);

                }
                catch (java.lang.IndexOutOfBoundsException e)
                {
                    Message msgWrong=Message.obtain();
                    msgWrong.what=-1;
                    handler.sendMessage(msgWrong);

                    e.printStackTrace();
                    return;
                } catch (java.net.UnknownHostException e) {
                    Message msgWrong=Message.obtain();
                    msgWrong.what=-2;
                    try {
                        Thread.sleep(500);//escapt that handler.sendMessage(msgWrong) and startActivity(intent) functioned together;
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    Log.d(TAG, "run: java.net.UnknownHostException,可能没联网");
                    handler.sendMessage(msgWrong);

                    e.printStackTrace();
                    return;
                }catch (java.net.SocketTimeoutException e) {
                    Message msgWrong=Message.obtain();
                    msgWrong.what=-3;
                    try {
                        Thread.sleep(500);//escapt that handler.sendMessage(msgWrong) and startActivity(intent) functioned together;
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    Log.d(TAG, "run: java.net.SocketTimeoutException,连接超时");
                    handler.sendMessage(msgWrong);

                    e.printStackTrace();
                    return;
                }
                catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        };
        NetThread.start();
    }
    void updateFromHtml(String html)
    {
        Document document= Jsoup.parse(html);
        Elements courses=document.select("table.infolist_tab").get(0).select("tr.infolist_common");
//      Elements =table;
        DBHP.clear(table);//clean DataBase before Writing
        for(Element aCourse:courses)
        {
            Elements theCourse=aCourse.select("a.infolist");
            String courseName=theCourse.get(0).text();
            String teacher=theCourse.get(1).text();
            Log.d(TAG, "updateFromServer: "+courseName+teacher);

            Elements timeTable=null;
            try{
                timeTable=aCourse.getElementsByTag("tbody").get(0).getElementsByTag("td");
            }catch (java.lang.IndexOutOfBoundsException E)
            {
                continue;
            }

            for (int i = 0; i < timeTable.size(); i+=4)
            {
                String weeks=timeTable.get(i).text();
                String day=timeTable.get(i+1).text();
                String classTime=timeTable.get(i+2).text();
                String location=timeTable.get(i+3).text();

                int week_model=0;
                if(weeks.contains("单")) {
                    week_model=1;
                    weeks=weeks.substring(0,weeks.indexOf("单"));
                }
                else if(weeks.contains("双")) {
                    week_model=2;
                    weeks=weeks.substring(0,weeks.indexOf("双"));
                }
                else  weeks=weeks.substring(0,weeks.indexOf("周"));
                if(weeks.contains("第"))
                    weeks=weeks.substring(weeks.indexOf("第")+1);


                String week_temp[]= weeks.split("-");

                int week_start=Integer.parseInt(week_temp[0]);
                int week_end= week_temp.length==2?Integer.parseInt(week_temp[1]):week_start;

                String class_timeTemp[]=classTime.split("节");
                class_timeTemp=class_timeTemp[0].split("-");
                int start=Integer.parseInt(class_timeTemp[0]);
                int length=Integer.parseInt(class_timeTemp[1])-start+1;

                int week=0;//周几
                switch (day)
                {
                    case "周一":week=1;break;
                    case "周二":week=2;break;
                    case "周三":week=3;break;
                    case "周四":week=4;break;
                    case "周五":week=5;break;
                    case "周六":week=6;break;
                    case "周日":week=7;break;

                }


                ContentValues cValue=new ContentValues();

                cValue.put("week",week);
                cValue.put("start",start);
                cValue.put("length",length);
                cValue.put("name",courseName);
                cValue.put("room",location);
                cValue.put("teacher",teacher);
                cValue.put("week_model",week_model);
                cValue.put("week_end",week_end);
                cValue.put("week_start",week_start);

                DBHP.db.insert(table,null,cValue);
                Log.d(TAG, "updateFromServer: "+timeTable);

            }
        }

    }
}
