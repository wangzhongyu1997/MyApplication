package com.example.chuntiao.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LogInActivity extends AppCompatActivity {
    private static final String TAG = "LogInActivity";
    private Handler handler;
    private Button btn_login;
    private ImageView checkNum;
    private EditText edit_num;
    private EditText edit_pass;
    private EditText edit_check;
    private CheckBox che_remember;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        setResult(-2);


        handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what==1)
                {
                    Bitmap bitmap = (Bitmap)msg.obj;
                    checkNum.setImageBitmap(bitmap);
                }
                if(msg.what==2)
                    if(  ((String)msg.obj).equals("ok") )
                    {
                        setResult(RESULT_OK);

                        Data.theDialog.btn_ok.setVisibility(View.VISIBLE);
                        Data.theDialog.progressBar.setVisibility(View.GONE);
                        Data.theDialog.setTitle("棒！");
                        Data.theDialog.sentence.setText("登录成功");
                        Data.theDialog.btn_ok.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Data.theDialog.finish();
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                finish();
                            }
                        });
                    }
                    else{

                        Data.theDialog.btn_ok.setVisibility(View.VISIBLE);
                        Data.theDialog.progressBar.setVisibility(View.GONE);
                        Data.theDialog.setTitle("登陆失败");
                        Data.theDialog.sentence.setText("验证码或密码错误,请重新登陆");
                        Data.theDialog.btn_ok.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                getCheckNum();
                                Data.theDialog.finish();
                            }
                        });
                    }
            }
        };

        btn_login=(Button)findViewById(R.id.btn_login);
        btn_login.setEnabled(false);
        checkNum=(ImageView) findViewById(R.id.image_check_num);
        checkNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCheckNum();
            }
        });
        edit_check=(EditText)findViewById(R.id.edit_check_num);
        edit_num=(EditText)findViewById(R.id.edit_num) ;
        edit_pass=(EditText)findViewById(R.id.edit_pass);
        che_remember=(CheckBox)findViewById(R.id.check_rember);

        che_remember.setChecked(Data.rememberPass);
        if(Data.rememberPass)
        {
            edit_num.setText(Data.num);
            edit_pass.setText(Data.password);
        }
        getCookieJSESSIONID_and_CheckNum();//写在一起防止太快JSESSIONID来不及更新



        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor=getSharedPreferences("data",MODE_PRIVATE).edit();
                editor.putString("num",edit_num.getText().toString());
                editor.putString("password",edit_pass.getText().toString());
                editor.putBoolean("rememberPass",che_remember.isChecked());
                editor.apply();
                logIn();
            }
        });
        edit_num.addTextChangedListener(new MyEditWatcher());
        edit_check.addTextChangedListener(new MyEditWatcher());
        edit_pass.addTextChangedListener(new MyEditWatcher());
    }

    class MyEditWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if(  !(edit_num.getText().toString().equals("")||edit_pass.getText().toString().equals("")||edit_check.getText().toString().equals(""))  )
            {
                btn_login.setEnabled(true);
            }
            else btn_login.setEnabled(false);
        }
    }
    void logIn()
    {
        Intent intent=new Intent();
        intent.setClass(this,WatingDialogActivity.class);
        intent.putExtra("title","登录中");
        startActivity(intent);

        new Thread(){
            @Override
            public void run() {
                OkHttpClient client=new OkHttpClient();
                RequestBody requestBody= new FormBody.Builder()
                        .add("groupId","")
                        .add("j_username",edit_num.getText().toString())
                        .add("j_password",edit_pass.getText().toString())
                        .add("j_captcha",edit_check.getText().toString())
                        .build();

                Request request=new Request.Builder()
                        .url("http://deanonline.stdu.edu.cn/academic/j_acegi_security_check")
                        .addHeader("Host","deanonline.stdu.edu.cn")
//                        .addHeader("User-Agent"," Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:50.0) Gecko/20100101 Firefox/50.0")
                        .addHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                        .addHeader("Accept-Language","en-US,en;q=0.5")
                        .addHeader("Accept-Encoding","gzip, deflate")
                        .addHeader("Referer","http://deanonline.stdu.edu.cn/academic/common/security/login.jsp?login_error=1")
                        .addHeader("Cookie",Data.cookieJSESSIONID)
                        .addHeader("Connection","keep-alive")
                        .addHeader("Upgrade-Insecure-Requests","1")
                        .post(requestBody)
                        .build();
                try {
                    Response response= client.newCall(request).execute();

                    String locate=response.networkResponse().request().url().toString();
                    Message msg=handler.obtainMessage();
                    msg.what=2;
                    if(locate!=null&&locate.equals("http://deanonline.stdu.edu.cn/academic/index_new.jsp"))
                    {
                        msg.obj="ok";
                    }
                    else msg.obj="wrong";

                    handler.sendMessage(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    void getCookieJSESSIONID_and_CheckNum()
    {
        new Thread(){
            @Override
            public void run() {
                OkHttpClient client=new OkHttpClient();
                Request request=new Request.Builder()
                        .url("http://deanonline.stdu.edu.cn/academic/logout_security_check")
                        .addHeader("Host","deanonline.stdu.edu.cn")
//                        .addHeader("User-Agent"," Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:50.0) Gecko/20100101 Firefox/50.0")
                        .addHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                        .addHeader("Accept-Language","en-US,en;q=0.5")
                        .addHeader("Accept-Encoding","gzip, deflate")
                        .addHeader("Referer","http://deanonline.stdu.edu.cn/academic/showHeader.do")
//                        .addHeader("Cookie",Data.cookieJSESSIONID)
                        .addHeader("Connection","keep-alive")
                        .addHeader("Upgrade-Insecure-Requests","1")
                        .build();
                try {
                    Response response= client.newCall(request).execute();
                    String cookie=response.header("Set-Cookie");
                    Data.cookieJSESSIONID=cookie.substring(0,cookie.indexOf(';'));
                    SharedPreferences.Editor editor= getSharedPreferences("data",MODE_PRIVATE).edit();
                    editor.putString("JSESSIONID",Data.cookieJSESSIONID);
                    editor.apply();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                request=new Request.Builder()
                        .url("http://deanonline.stdu.edu.cn/academic/getCaptcha.do")
                        .addHeader("Host","deanonline.stdu.edu.cn")
//                        .addHeader("User-Agent"," Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:50.0) Gecko/20100101 Firefox/50.0")
                        .addHeader("Accept","*/*")
                        .addHeader("Accept-Language","en-US,en;q=0.5")
                        .addHeader("Accept-Encoding","gzip, deflate")
                        .addHeader("Referer","http://deanonline.stdu.edu.cn/academic/index.jsp")
                        .addHeader("Cookie",Data.cookieJSESSIONID)
                        .addHeader("Connection","keep-alive")
                        .build();
                try {
                    Response response=client.newCall(request).execute();
                    InputStream imageStream= response.body().byteStream();
                    Bitmap bitmap= BitmapFactory.decodeStream(imageStream);
                    Message message=handler.obtainMessage();
                    message.obj=bitmap;
                    message.what=1;
                    handler.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }.start();
    }
    void getCheckNum()
    {
        new Thread(){
            @Override
            public void run() {
                OkHttpClient client=new OkHttpClient();
                Request request=new Request.Builder()
                        .url("http://deanonline.stdu.edu.cn/academic/getCaptcha.do")
                        .addHeader("Host","deanonline.stdu.edu.cn")
//                        .addHeader("User-Agent"," Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:50.0) Gecko/20100101 Firefox/50.0")
                        .addHeader("Accept","*/*")
                        .addHeader("Accept-Language","en-US,en;q=0.5")
                        .addHeader("Accept-Encoding","gzip, deflate")
                        .addHeader("Referer","http://deanonline.stdu.edu.cn/academic/index.jsp")
                        .addHeader("Cookie",Data.cookieJSESSIONID)
                        .addHeader("Connection","keep-alive")
                        .build();
                try {
                    Response response=client.newCall(request).execute();
                    InputStream imageStream= response.body().byteStream();
                    Bitmap bitmap= BitmapFactory.decodeStream(imageStream);
                    Message message=handler.obtainMessage();
                    message.obj=bitmap;
                    message.what=1;
                    handler.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
