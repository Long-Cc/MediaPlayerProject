package com.longc.mobileplayer.pager;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.longc.mobileplayer.R;
import com.longc.mobileplayer.activity.AudioPlayerActivity;
import com.longc.mobileplayer.adapter.VideoPagerAdapter;
import com.longc.mobileplayer.base.BasePager;
import com.longc.mobileplayer.domain.MediaItem;

import java.util.ArrayList;

/**
 * 本地音乐界面
 *
 * @author longc
 */
public class AudioPager extends BasePager {

    private ListView listview;
    private TextView tv_nomedia;
    private ProgressBar pb_loading;


    private ArrayList<MediaItem> mediaItems;

    public AudioPager(Context context) {
        super(context);
    }

    ListAdapter audioPagerAdapter;


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
//            Toast.makeText(context,"****"+mediaItems.toString(),Toast.LENGTH_SHORT).show();
            if (mediaItems != null && mediaItems.size() > 0) {
                //有数据
                //设置Adapter
                audioPagerAdapter = new VideoPagerAdapter(context,mediaItems,false);
                listview.setAdapter(audioPagerAdapter);
                //把文本隐藏
                tv_nomedia.setVisibility(View.GONE);
            } else {
                //没有数据
                //显示文本
                tv_nomedia.setVisibility(View.VISIBLE);
                tv_nomedia.setText("没有发现音乐....");
            }
            //把progressBar隐藏
            pb_loading.setVisibility(View.GONE);
        }
    };

    /**
     * 初始化当前页面的控件，由父类调用
     *
     * @return
     */

    @Override
    public View initView() {
        View view = View.inflate(context, R.layout.video_pager, null);

        listview = (ListView) view.findViewById(R.id.listview);
        tv_nomedia = (TextView) view.findViewById(R.id.tv_nomedia);
        pb_loading = (ProgressBar) view.findViewById(R.id.pb_loading);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(context,AudioPlayerActivity.class);
                intent.putExtra("position",position);
                context.startActivity(intent);
            }

        });
        return view;
    }

    @Override
    public void initData() {
        super.initData();
        //加载本地音乐数据
        getDataFromLocal();

    }
    /**
     * 从本地sdCard中得到数据
     * 1.扫描后缀名
     * 2.从内容提供者库中获取数据
     */
    private void getDataFromLocal() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                isGrantExternalRW((Activity) context);
                mediaItems = new ArrayList<>();
                ContentResolver resolver = context.getContentResolver();
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String[] objes = {
                        MediaStore.Audio.Media.DISPLAY_NAME,//音乐的名称
                        MediaStore.Audio.Media.DURATION,//音乐的总时长
                        MediaStore.Audio.Media.SIZE,//音乐文件大小
                        MediaStore.Audio.Media.DATA,//音乐的绝对地址
                        MediaStore.Audio.Media.ARTIST//歌曲的演唱者
                };
                Cursor cursor = resolver.query(uri, objes, null, null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {

                        MediaItem mediaItem = new MediaItem();
                        mediaItems.add(mediaItem);

                        String name = cursor.getString(0);//音乐的名称
                        mediaItem.setName(name);
                        long duration = cursor.getLong(1);//音乐的总时长
                        mediaItem.setDuration(duration);
                        long size = cursor.getLong(2);//音乐文件大小
                        mediaItem.setSize(size);
                        String data = cursor.getString(3);//音乐的绝对地址
                        mediaItem.setData(data);
                        String aritst = cursor.getString(4);//歌曲的演唱者
                        mediaItem.setAritst(aritst);
                    }
                    cursor.close();
                }
//                Log.e("TGA","+++++"+mediaItems);
                //Handler发消息
                handler.sendEmptyMessage(10);
            }
        }.start();

    }



    /**
     * 解决安卓6.0以上版本不能读取外部存储权限的问题
     * @param activity
     * @return
     */
    public static boolean isGrantExternalRW(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            activity.requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);

            return false;
        }

        return true;
    }

}
