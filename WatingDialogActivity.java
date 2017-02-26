package com.example.chuntiao.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class WatingDialogActivity extends AppCompatActivity {
    public TextView sentence;
    public Button btn_ok;
//    public Button btn_cancel;
    public ProgressBar progressBar;
    private static final String TAG = "WatingDialogActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wating_dialog);
        Data.theDialog=this;
        Intent intent=getIntent();
        setTitle(intent.getStringExtra("title"));

        progressBar=(ProgressBar)findViewById(R.id.progressBar);
        sentence=(TextView)findViewById(R.id.text_sentence);
        sentence.setText(intent.getStringExtra("sentence"));

        btn_ok=(Button)findViewById(R.id.btn_ok);

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if(intent.getBooleanExtra("showButton",false))
            btn_ok.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
    }
}
