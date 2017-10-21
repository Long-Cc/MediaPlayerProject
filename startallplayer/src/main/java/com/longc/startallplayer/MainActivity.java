package com.longc.startallplayer;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void startAllPlayer1(View v){
        Intent intent = new Intent();
        intent.setDataAndType(Uri.parse("http://gslb.miaopai.com/stream/3D~8BM-7CZqjZscVBEYr5g__.mp4"), "video/*");
        startActivity(intent);
    }
    public void startAllPlayer2(View v){
        Intent intent = new Intent();
        intent.setDataAndType(Uri.parse("http://192.168.1.103:8080/shuihuzhuang.mkv"),"video/*");
        startActivity(intent);
    }
    public void startAllPlayer3(View v){
        Intent intent = new Intent();
        intent.setDataAndType(Uri.parse("http://192.168.1.103:8080/shuihuzhuang22.mkv"),"video/*");
        startActivity(intent);
    }

}
