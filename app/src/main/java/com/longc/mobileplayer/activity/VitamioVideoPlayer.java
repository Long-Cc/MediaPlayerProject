package com.longc.mobileplayer.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.longc.mobileplayer.R;
import com.longc.mobileplayer.View.VitamioVideoView;
import com.longc.mobileplayer.domain.MediaItem;
import com.longc.mobileplayer.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;

/**
 * Created by longc on 2016/12/9.
 */
public class VitamioVideoPlayer extends Activity implements View.OnClickListener {


    private RelativeLayout mediacontroller;
    private VitamioVideoView videoview;
    private Uri uri;
    private LinearLayout llTop;
    private TextView tvName;
    private ImageView ivBattery;
    private TextView tvSystemTime;
    private Button btnVoice;
    private SeekBar seekbarVoice;
    private Button btnSwitchPlayer;
    private LinearLayout llBottom;
    private SeekBar seekbarVideo;
    private TextView tvDuration;
    private Button btnExit;
    private Button btnVideoPre;
    private Button btnVideoStartPause;
    private Button btnVideoNext;
    private Button btnVideoSiwchScreen;
    private TextView tvcurrenttime;
    private String sysTime;
    private TextView tv_buffer_netspeed;
    private LinearLayout ll_buffer;
    private LinearLayout ll_loading;
    private TextView tv_loading_netspeed;


    Utils utils;

    /**
     * 监听电量的广播
     */
    private MyReceiver receiver;
    /**
     * 视频进度的更新
     */
    private static final int PROGRESS = 1;
    /**
     * 控制面板隐藏
     */
    private static final int HID_EMEDIACONTROLLER = 2;
    /**
     * 显示网速
     */
    private static final int SHOW_SPEED = 3;
    /**
     * 屏幕默认大小
     */
    private static final int DEFAULT_SCREEN = 3;
    /**
     * 全屏
     */
    private static final int FULL_SCREEN = 4;
    /**
     * 传入进来的视频列表
     */
    private ArrayList<MediaItem> mediaItems;
    /**
     * 要播放的列表中的具体位置
     */
    private int position;
    /**
     * 手势识别器
     */
    private GestureDetector detector;
    /**
     * 是否显示控制面板
     */
    private boolean isshowMediaController = false;
    /**
     * 是否全屏
     */
    private boolean isFullScreen = false;
    /**
     * 设置屏幕的宽和高
     */
    private int screenWidth;
    private int screenHeight;
    /**
     * 真实视频的宽和高
     */
    private int videoWidth;
    private int videoHeight;
    /**
     * 调用声音
     */
    private AudioManager am;

    /**
     * 当前的音量
     */
    private int currentVoice;

    /**
     * 0~15
     * 最大音量
     */
    private int maxVoice;

    /**
     * 是否是静音
     */
    private boolean isMute;


    private float startY;
//    private float startX;
    /**
     * 屏幕的高
     */
    private float touchRang;
    /**
     * 当一按下的音量
     */
    private int mVol;
    /**
     * 是否是网络uri
     */
    private boolean isNetUri;
    /**
     * 是否用系统的监听卡
     */
    boolean isUseSystem = true;
    /**
     * 上一秒的播放进度
     */
    int precurrentPosition;

    /**
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2016-12-11 11:14:44 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    private void findViews() {
        Vitamio.isInitialized(this);//一定要加，加在”R.layout.activity_vitamio_video_player“之前
        setContentView(R.layout.activity_vitamio_video_player);
        llTop = (LinearLayout) findViewById(R.id.ll_top);
        tvName = (TextView) findViewById(R.id.tv_name);
        ivBattery = (ImageView) findViewById(R.id.iv_battery);
        tvSystemTime = (TextView) findViewById(R.id.tv_system_time);
        btnVoice = (Button) findViewById(R.id.btn_voice);
        seekbarVoice = (SeekBar) findViewById(R.id.seekbar_voice);
        btnSwitchPlayer = (Button) findViewById(R.id.btn_switch_player);
        llBottom = (LinearLayout) findViewById(R.id.ll_bottom);
        seekbarVideo = (SeekBar) findViewById(R.id.seekbar_video);
        tvDuration = (TextView) findViewById(R.id.tv_duration);
        btnExit = (Button) findViewById(R.id.btn_exit);
        btnVideoPre = (Button) findViewById(R.id.btn_video_pre);
        btnVideoStartPause = (Button) findViewById(R.id.btn_video_start_pause);
        btnVideoNext = (Button) findViewById(R.id.btn_video_next);
        btnVideoSiwchScreen = (Button) findViewById(R.id.btn_video_siwch_screen);
        tvcurrenttime = (TextView) findViewById(R.id.tvcurrenttime);
        mediacontroller = (RelativeLayout) findViewById(R.id.mediacontroller);
        tv_buffer_netspeed = (TextView) findViewById(R.id.tv_buffer_netspeed);
        ll_buffer = (LinearLayout) findViewById(R.id.ll_buffer);
        ll_loading = (LinearLayout) findViewById(R.id.ll_loading);
        tv_loading_netspeed = (TextView) findViewById(R.id.tv_loading_netspeed);

        videoview = (VitamioVideoView) findViewById(R.id.videoview);

        btnVoice.setOnClickListener(this);
        btnSwitchPlayer.setOnClickListener(this);
        btnExit.setOnClickListener(this);
        btnVideoPre.setOnClickListener(this);
        btnVideoStartPause.setOnClickListener(this);
        btnVideoNext.setOnClickListener(this);
        btnVideoSiwchScreen.setOnClickListener(this);


        //最大音量和SeekBar关联
        seekbarVoice.setMax(maxVoice);
        //设置当前进度-当前音量
        seekbarVoice.setProgress(currentVoice);
        //发更新网速的消息
        handler.sendEmptyMessage(SHOW_SPEED);


    }


    /**
     * Handle button click events<br />
     * <br />
     * Auto-created on 2016-12-11 11:14:44 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    @Override
    public void onClick(View v) {
        if (v == btnVoice) {
            isMute = !isMute;
            updataVoice(0, isMute);
        } else if (v == btnSwitchPlayer) {
            showSwitchPlayerDialog();
        } else if (v == btnExit) {
            finish();//退出
        } else if (v == btnVideoStartPause) {
            startAndPause();
        } else if (v == btnVideoNext) {
            //下一个视频按钮
            playNextVideo();
        } else if (v == btnVideoPre) {
            //上一个视频按钮
            playPreVideo();
        } else if (v == btnVideoSiwchScreen) {
            setFullScreenAndDefault();
        }
        handler.removeMessages(HID_EMEDIACONTROLLER);
        handler.sendEmptyMessageDelayed(HID_EMEDIACONTROLLER, 3000);
    }

    private void showSwitchPlayerDialog() {
        new AlertDialog.Builder(VitamioVideoPlayer.this)
                .setTitle("万能播放器提示您：")
                .setMessage("如果出现花屏等图像的时候，可以尝试切换到系统播放器！")
                .setPositiveButton("确定",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startSystemPlayer();
                    }
                })
                .setNegativeButton("取消",null)
                .show();
    }

    private void startSystemPlayer() {
        Intent intent = new Intent(this, SystemVideoPlayer.class);
        if(videoview != null){
            videoview.stopPlayback();
        }
        if (mediaItems != null && mediaItems.size() > 0) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("videolist", mediaItems);
            intent.putExtras(bundle);
            intent.putExtra("position", position);

        } else if (uri != null) {
            intent.setData(uri);
        }
        startActivity(intent);
        finish();
    }

    private void startAndPause() {
        if (videoview.isPlaying()) {
            //视频是播放状态-设置为暂停状态
            videoview.pause();
            //设置按钮为播放
            btnVideoStartPause.setBackgroundResource(R.drawable.btn_video_start_selector);
        } else {
            //视频是暂停状态-设置为播放状态
            videoview.start();
            //设置按钮为暂停
            btnVideoStartPause.setBackgroundResource(R.drawable.btn_video_pause_selector);
        }
    }

    /**
     * 播放上一个视频
     */
    private void playPreVideo() {
        if (mediaItems != null && mediaItems.size() > 0) {
            //播放上一个视频
            position--;
            if (position >= 0) {

//                ll_loading.setVisibility(View.VISIBLE);

                MediaItem mediaItem = mediaItems.get(position);
                tvName.setText(mediaItem.getName());
                isNetUri = utils.isNetUri(mediaItem.getData());
                videoview.setVideoPath(mediaItem.getData());
                //设置按钮状态
                setButtonState();
            }
        } else if (uri != null) {
            //设置按钮状态-上一个和下一个按钮设置灰色并且不可以点击
            setEnable(false);
        }
    }

    /**
     * 播放下一个
     */
    private void playNextVideo() {
        if (mediaItems != null && mediaItems.size() > 0) {
            //播放下一个视频
            position++;
            if (position < mediaItems.size()) {

//                ll_loading.setVisibility(View.VISIBLE);

                MediaItem mediaItem = mediaItems.get(position);
                tvName.setText(mediaItem.getName());
                isNetUri = utils.isNetUri(mediaItem.getData());
                videoview.setVideoPath(mediaItem.getData());
                setButtonState();
            }

        } else if (uri != null) {
            //设置按钮状态-上一个和下一个按钮设置灰色并且不可以点击
            setEnable(false);
        }
    }

    private void setButtonState() {
        if (mediaItems != null && mediaItems.size() > 0) {
            if (mediaItems.size() == 1) {
                setEnable(false);//上一个和下一个按钮设置灰色并且不可以点击
            } else if (mediaItems.size() == 2) { //下可点击,上不可点---设置下不可点，上可点
                if (position == 0) {
                    btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
                    btnVideoPre.setEnabled(false);

                    btnVideoNext.setBackgroundResource(R.drawable.btn_video_next_selector);
                    btnVideoNext.setEnabled(true);

                } else if (position == mediaItems.size() - 1) {
                    btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
                    btnVideoNext.setEnabled(false);

                    btnVideoPre.setBackgroundResource(R.drawable.btn_video_pre_selector);
                    btnVideoPre.setEnabled(true);

                }

            } else {
                if (position == 0) { //设置 下可点击，上可点
                    btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
                    btnVideoPre.setEnabled(false);
                } else if (position == mediaItems.size() - 1) {//设置下不可点击
                    btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
                    btnVideoNext.setEnabled(false);
                } else {
                    setEnable(true);
                }
            }
        } else if (uri != null) {
            //两个按钮设置灰色
            setEnable(false);
        }
    }

    public void setEnable(boolean isEnable) {

        if (isEnable) {
            btnVideoPre.setBackgroundResource(R.drawable.btn_video_pre_selector);
            btnVideoPre.setEnabled(true);
            btnVideoNext.setBackgroundResource(R.drawable.btn_video_next_selector);
            btnVideoNext.setEnabled(true);
        } else {
            //两个按钮设置灰色
            btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
            btnVideoPre.setEnabled(false);
            btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
            btnVideoNext.setEnabled(false);
        }

    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SHOW_SPEED://显示网速
                    //1.得到网速
                    String netSpeed = utils.getNetSpeed(VitamioVideoPlayer.this);
                    //2显示网速
                    tv_loading_netspeed.setText("玩命加载中++...." + netSpeed);
                    tv_buffer_netspeed.setText("正在缓冲...." + netSpeed);

                    //3.每两秒更新一次
                    handler.removeMessages(SHOW_SPEED);
                    handler.sendEmptyMessageDelayed(SHOW_SPEED, 2000);
                    break;

                case HID_EMEDIACONTROLLER:
                    hideMediaController();
                    break;

                case PROGRESS:
                    //1.得到当前的视频播放进程
                    int currentPosition = (int) videoview.getCurrentPosition();
                    //2.SeekBar.setProgress(当前进度);
                    seekbarVideo.setProgress(currentPosition);
                    //更新文本播放进度
                    tvcurrenttime.setText(utils.stringForTime(currentPosition));

                    //设置系统时间
                    tvSystemTime.setText(getSysTime());

                    //缓存进度的更新
                    if (isNetUri) {
                        //只有网络资源才有缓存效果
                        int buffer = videoview.getBufferPercentage();//0-100
                        int totalBuffer = buffer * seekbarVideo.getMax();
                        int secondaryProgress = totalBuffer / 100;
                        seekbarVideo.setSecondaryProgress(secondaryProgress);
                    } else {
                        //本地视频没有缓冲效果
                        seekbarVideo.setSecondaryProgress(0);
                    }

                    //监听卡
                    if (!isUseSystem) {

                        if (videoview.isPlaying()) {
                            int buffer = currentPosition - precurrentPosition;
                            if (buffer < 500) {
                                //视频卡了
                                ll_buffer.setVisibility(View.VISIBLE);
                            } else {
                                //视频不卡了
                                ll_buffer.setVisibility(View.GONE);
                            }
                        } else {
                            ll_buffer.setVisibility(View.GONE);
                        }

                    }
                    precurrentPosition = currentPosition;//记录上一次播放进度
                    //3.每秒更新一次
                    handler.removeMessages(PROGRESS);
                    handler.sendEmptyMessageDelayed(PROGRESS, 1000);
                    break;
            }
        }
    };

    private void setListener() {
        //准备好的监听
        videoview.setOnPreparedListener(new MyOnPreparedListener());

        //播放出错了的监听
        videoview.setOnErrorListener(new MyOnErrorListener());

        //播放完成了的监听
        videoview.setOnCompletionListener(new MyOnCompletionListener());

        //设置SeeKbar状态变化的监听
        seekbarVideo.setOnSeekBarChangeListener(new VideoOnSeekBarChangeListener());

        //设置SeeKbar状态变化的监听
        seekbarVoice.setOnSeekBarChangeListener(new VoiceOnSeekBarChangeListener());

        if (isUseSystem) {
            //监听视频播放卡-系统的api
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                videoview.setOnInfoListener(new MyOnInfoListener());
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        初始化数据
         */
        initData();
        /*
        初始化 id
         */
        findViews();
        /**
         * 所有的监听
         */
        setListener();

        getData();

        setData();

    }

    /**
     * 显示控制面板
     */
    private void showMediaController() {
        mediacontroller.setVisibility(View.VISIBLE);
        isshowMediaController = true;
    }

    /**
     * 隐藏控制面板
     */
    private void hideMediaController() {
        mediacontroller.setVisibility(View.GONE);
        isshowMediaController = false;
    }

    /**
     * 监听音量物理按键
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        showMediaController();
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            currentVoice--;
            updataVoice(currentVoice, false);
            handler.removeMessages(HID_EMEDIACONTROLLER);
            handler.sendEmptyMessageDelayed(HID_EMEDIACONTROLLER, 4000);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            currentVoice++;
            updataVoice(currentVoice, false);
            handler.removeMessages(HID_EMEDIACONTROLLER);
            handler.sendEmptyMessageDelayed(HID_EMEDIACONTROLLER, 4000);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        detector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //1.按下记录值
                startY = event.getY();
//                startX = event.getX();
                mVol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                handler.removeMessages(HID_EMEDIACONTROLLER);
                touchRang = Math.min(screenWidth, screenHeight);//screenHeight
                break;
            case MotionEvent.ACTION_MOVE:
                //2.移动的记录相关值
                float endY = event.getY();
                float distanceY = startY - endY;
                int delta = (int) ((distanceY / touchRang) * maxVoice);
                int voice = (int) Math.min(Math.max(mVol + delta, 0), maxVoice);
                isMute = false;
                updataVoice(voice, isMute);
                break;
            case MotionEvent.ACTION_UP:
                //重新发送消息
                handler.sendEmptyMessageDelayed(HID_EMEDIACONTROLLER, 4000);
                break;
        }


        return super.onTouchEvent(event);
    }

    private void setData() {

        if (mediaItems != null && mediaItems.size() > 0) {
            MediaItem mediaItem = mediaItems.get(position);

            tvName.setText(mediaItem.getName());//设置视频的名称
            isNetUri = utils.isNetUri(mediaItem.getData());
            videoview.setVideoPath(mediaItem.getData());

        } else if (uri != null) {
            tvName.setText(uri.toString());//设置视频的名称
            isNetUri = utils.isNetUri(uri.toString());
            videoview.setVideoURI(uri);
        } else {
            Toast.makeText(VitamioVideoPlayer.this, "没有传递数据", Toast.LENGTH_SHORT).show();
        }
        setButtonState();
    }

    private void getData() {
        //得到播放地址
        uri = getIntent().getData();
        mediaItems = (ArrayList<MediaItem>) getIntent().getSerializableExtra("videolist");
        position = getIntent().getIntExtra("position", 0);

    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    private void initData() {
        utils = new Utils();
        //注册电量广播
        receiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        //当电量发生变化时发出这个广播
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(receiver, intentFilter);

        detector = new GestureDetector(this, new MySimpleOnGestureListener());

        //1.得到屏幕的宽高
        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        //2.设置视频画面宽高
        screenWidth = outMetrics.widthPixels;
        screenHeight = outMetrics.heightPixels;
        //得到音量
        am = (AudioManager) getSystemService(AUDIO_SERVICE);
        currentVoice = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVoice = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    public void setVideoType(int defaultScreen) {
        switch (defaultScreen) {
            case FULL_SCREEN://全屏
                //1.设置视频画面的大小
                videoview.setVideoSize(screenWidth, screenHeight);
                //2.设置按钮的状态
                btnVideoSiwchScreen.setBackgroundResource(R.drawable.btn_video_siwch_screen_default_selector);
                isFullScreen = true;
//                Log.e("TGA",screenWidth+"***"+screenHeight);
//                Log.e("TGA",videoWidth+"***"+videoHeight);
                break;

            case DEFAULT_SCREEN://默认
                //1.设置视频画面的大小
                //视频真实的宽和高
                int mVideoWidth = videoWidth;
                int mVideoHeight = videoHeight;

                //屏幕的宽和高
                int width = screenWidth;
                int height = screenHeight;
                // for compatibility, we adjust size based on aspect ratio
                if (mVideoWidth * height < width * mVideoHeight) {
                    //Log.i("@@@", "image too wide, correcting");
                    width = height * mVideoWidth / mVideoHeight;
                } else if (mVideoWidth * height > width * mVideoHeight) {
                    //Log.i("@@@", "image too tall, correcting");
                    height = width * mVideoHeight / mVideoWidth;
                }

                videoview.setVideoSize(width, height);
                //2.设置按钮的状态--全屏
                btnVideoSiwchScreen.setBackgroundResource(R.drawable.btn_video_siwch_screen_full_selector);
                isFullScreen = false;
                break;
        }
    }



    class MySimpleOnGestureListener extends SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            setFullScreenAndDefault();
            return super.onDoubleTap(e);

        }

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
            startAndPause();
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (isshowMediaController) {
                //隐藏
                hideMediaController();
                handler.removeMessages(HID_EMEDIACONTROLLER);
            } else {
                //显示
                showMediaController();
                //发消息隐藏
                handler.sendEmptyMessageDelayed(HID_EMEDIACONTROLLER, 3000);
            }
            return super.onSingleTapConfirmed(e);
        }
    }

    private void setFullScreenAndDefault() {
        if (isFullScreen) {
            setVideoType(DEFAULT_SCREEN);//默认
        } else {
            setVideoType(FULL_SCREEN);
        }
    }

    public String getSysTime() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(new Date());
    }


    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra("level", 0);
            setBattery(level);
        }
    }

    private void setBattery(int level) {
        if (level <= 0) {
            ivBattery.setImageResource(R.drawable.ic_battery_0);
        } else if (level <= 10) {
            ivBattery.setImageResource(R.drawable.ic_battery_10);
        } else if (level <= 20) {
            ivBattery.setImageResource(R.drawable.ic_battery_20);
        } else if (level <= 40) {
            ivBattery.setImageResource(R.drawable.ic_battery_40);
        } else if (level <= 60) {
            ivBattery.setImageResource(R.drawable.ic_battery_60);
        } else if (level <= 80) {
            ivBattery.setImageResource(R.drawable.ic_battery_80);
        } else if (level <= 100) {
            ivBattery.setImageResource(R.drawable.ic_battery_100);
        } else {
            ivBattery.setImageResource(R.drawable.ic_battery_100);
        }
    }

    @Override
    protected void onDestroy() {
        //移除所有消息
        handler.removeCallbacksAndMessages(null);

        if (receiver != null) {//先释放子类资源，再释放父类资源
            unregisterReceiver(receiver);
            receiver = null;
        }
        super.onDestroy();
    }

    private class MyOnPreparedListener implements MediaPlayer.OnPreparedListener {
        //但底层解码准备好时
        @Override
        public void onPrepared(MediaPlayer mp) {
            videoview.start();//开始播放

            Log.e("TAG", tv_loading_netspeed.getText().toString());

            //1.视频的总时长，关联总长度
            int duration = (int) videoview.getDuration();
            seekbarVideo.setMax(duration);
            tvDuration.setText(utils.stringForTime(duration));

            //2.发消息
            handler.sendEmptyMessage(PROGRESS);

            hideMediaController();//默认是隐藏控制面板


//           videoview.setVideoSize(mp.getVideoWidth(),mp.getVideoHeight());
            /**
             * 得到真实视频的宽高
             */
            videoWidth = mp.getVideoWidth();
            videoHeight = mp.getVideoHeight();

            //屏幕的默认播放
//            setVideoType(DEFAULT_SCREEN);

            //隐藏加载页面
            ll_loading.setVisibility(View.GONE);

        }
    }

    private class MyOnErrorListener implements MediaPlayer.OnErrorListener {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
//            Toast.makeText(VitamioVideoPlayer.this, "出错了！", Toast.LENGTH_SHORT).show();
           new AlertDialog.Builder(VitamioVideoPlayer.this)
                   .setTitle("提示")
                   .setMessage("抱歉，播放出错！")
                   .setPositiveButton("确定",new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            }).show();
            return true;
        }
    }

    private class MyOnCompletionListener implements MediaPlayer.OnCompletionListener {
        @Override
        public void onCompletion(MediaPlayer mp) {
//            Toast.makeText(SystemVideoPlayer.this, "播放完成！", Toast.LENGTH_SHORT).show();
            playNextVideo();
        }
    }

    private class VideoOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        /**
         * 当手指滑动的时候，会引起SeekBar进度变化，会回调这个方法
         *
         * @param seekBar
         * @param progress
         * @param fromUser 如果是用户引起的true,不是用户引起的false
         */
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                videoview.seekTo(progress);
            }

        }

        /**
         * 当手指触碰的时候回调这个方法
         *
         * @param seekBar
         */
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            handler.removeMessages(HID_EMEDIACONTROLLER);
        }

        /**
         * 当手指离开的时候回调这个方法
         *
         * @param seekBar
         */
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            handler.sendEmptyMessageDelayed(HID_EMEDIACONTROLLER, 3000);
        }
    }

    private class VoiceOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                if (progress > 0) {
                    isMute = false;
                } else {
                    isMute = true;
                }
                updataVoice(progress, isMute);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    /**
     * 设置音量的大小
     *
     * @param progress
     */
    private void updataVoice(int progress, boolean isMute) {

        if (isMute) {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
            seekbarVoice.setProgress(0);
        } else {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            seekbarVoice.setProgress(progress);
            currentVoice = progress;
        }

    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    private class MyOnInfoListener implements MediaPlayer.OnInfoListener {
        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            switch (what) {
                case MediaPlayer.MEDIA_INFO_BUFFERING_START://视频卡了，拖动卡
                    ll_buffer.setVisibility(View.VISIBLE);
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_END://视频卡了结束，拖动卡结束了
                    ll_buffer.setVisibility(View.GONE);
                    break;
            }
            return true;
        }
    }
}
