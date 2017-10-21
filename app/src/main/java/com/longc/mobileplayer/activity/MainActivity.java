package com.longc.mobileplayer.activity;

import android.os.Bundle;

import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.longc.mobileplayer.R;
import com.longc.mobileplayer.base.BasePager;
import com.longc.mobileplayer.pager.AudioPager;
import com.longc.mobileplayer.pager.NetAudioPager;
import com.longc.mobileplayer.pager.NetVideoPager;
import com.longc.mobileplayer.pager.VideoPager;

import java.util.ArrayList;

/**作用：主页面
 * Created by longc on 2016/12/8.
 */
public class MainActivity extends FragmentActivity {

    private RadioGroup rg_bottom_tag;

    /**
     * 页面的集合
     */
    private ArrayList<BasePager> basePagers;

    private int position;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rg_bottom_tag = (RadioGroup) findViewById(R.id.rg_bottom_tag);

        basePagers = new ArrayList<>();
        basePagers.add(new VideoPager(this));
        basePagers.add(new AudioPager(this));
        basePagers.add(new NetVideoPager(this));
        basePagers.add(new NetAudioPager(this));


        rg_bottom_tag.setOnCheckedChangeListener(new MyOnCheckChangeListener());
        rg_bottom_tag.check(R.id.rb_video);//默认选中首页

    }

    private class MyOnCheckChangeListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                default:
                    position = 0;
                    break;
                case R.id.rb_audio://音频
                    position = 1;
                    break;
                case R.id.rb_net_video://网络视频
                    position = 2;
                    break;
                case R.id.rb_netaudio://网络音频
                    position = 3;
                    break;
            }
            setFragment();
        }
    }
    /**
     * 把页面添加到Fragment中
     */
    private void setFragment() {
        //1.得到FragmentManger
        FragmentManager manager = getSupportFragmentManager();
        //2.开启事务
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        //3.替换
        fragmentTransaction.replace(R.id.fl_main_content, new Fragment() {
            @Nullable
            @Override
            public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
                //各个页面的视图
                BasePager basePager = getBasePager();
                if (basePager != null) {
                    return basePager.rootView;
                }
                return null;
            }
        });
        //4.提交事务
        fragmentTransaction.commit();

    }

    /**
     * 根据位置得到对应的页面
     * @return
     */
    private BasePager getBasePager() {
        BasePager basePager = basePagers.get(position);
        if (basePager != null && !basePager.isInitData) {
            basePager.isInitData = true;
            basePager.initData();//联网请求或者绑定数据
        }
        return basePager;
    }

    /**
     * 是否已经退出
     */
    private boolean isExit = false;
    /**
     * 按back两次退出
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            if(position != 0){//不是首页
                position = 0;
                rg_bottom_tag.check(R.id.rb_video);//首页
                return true;
            }else if(!isExit){
                isExit = true;
                Toast.makeText(this,"再按一次退出！",Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isExit  = false;
                    }
                },2000);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}

