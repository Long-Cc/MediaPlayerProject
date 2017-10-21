package com.longc.mobileplayer.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.longc.mobileplayer.IMusicPlayerService;
import com.longc.mobileplayer.R;
import com.longc.mobileplayer.View.BaseVisualizerView;
import com.longc.mobileplayer.View.ShowLyricView;
import com.longc.mobileplayer.domain.MediaItem;
import com.longc.mobileplayer.service.MusicPlayerService;
import com.longc.mobileplayer.utils.LogUtil;
import com.longc.mobileplayer.utils.LyricUtils;
import com.longc.mobileplayer.utils.Utils;

import java.io.File;

import de.greenrobot.event.EventBus;


/**
 * Created by longc on 2016/12/21.
 */
public class AudioPlayerActivity extends Activity implements View.OnClickListener {
    /**
     * 进度条的消息
     */
    private static final int PROGRESS = 1;
    /**
     * 显示歌词的消息
     */
    private static final int SHOW_LYRIC = 2;

    private int position;
    private IMusicPlayerService service;

    /**
     * true:从状态栏进入的，不需要重新播放
     * false:从播放列表进入的
     */
    private boolean notification;

    /**
     * 是否显示歌词
     */
    private boolean isShowLyric = true;

    private MyReceivre myReceivre;

    private Visualizer mVisualizer;

    private RelativeLayout rlTop;
    private ImageView ivIcon;
    private TextView tvArtist;
    private TextView tvName;
    private TextView tvTime;
    private SeekBar seekbarAudio;
    private Button btnPlaymode;
    private Button btnAudioPre;
    private Button btnAudioStartPause;
    private Button btnAudioNext;
    private Button btnLyrics;
    private ShowLyricView showLyricView;
    private BaseVisualizerView baseVisualizerView;

    private Utils utils;



    /**
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2016-12-22 11:01:31 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    private void findViews() {
        setContentView(R.layout.activity_audioplayer);

        ivIcon = (ImageView) findViewById(R.id.iv_icon);

        ivIcon.setBackgroundResource(R.drawable.animation_list);
        AnimationDrawable rocketAnimation = (AnimationDrawable) ivIcon.getBackground();
        rocketAnimation.start();

        rlTop = (RelativeLayout) findViewById(R.id.rl_top);
        tvArtist = (TextView) findViewById(R.id.tv_artist);
        tvName = (TextView) findViewById(R.id.tv_name);
        tvTime = (TextView) findViewById(R.id.tv_time);
        seekbarAudio = (SeekBar) findViewById(R.id.seekbar_audio);
        btnPlaymode = (Button) findViewById(R.id.btn_playmode);
        btnAudioPre = (Button) findViewById(R.id.btn_audio_pre);
        btnAudioStartPause = (Button) findViewById(R.id.btn_audio_start_pause);
        btnAudioNext = (Button) findViewById(R.id.btn_audio_next);
        btnLyrics = (Button) findViewById(R.id.btn_lyrics);
        showLyricView = (ShowLyricView) findViewById(R.id.showLyricView);
        baseVisualizerView = (BaseVisualizerView) findViewById(R.id.baseVisualizerView);

        btnPlaymode.setOnClickListener(this);
        btnAudioPre.setOnClickListener(this);
        btnAudioStartPause.setOnClickListener(this);
        btnAudioNext.setOnClickListener(this);
        btnLyrics.setOnClickListener(this);

        //设置视频的拖动
        seekbarAudio.setOnSeekBarChangeListener(new MyOnSeekBarChangeListener());
    }

    private class MyOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                try {
                    service.seekTo(progress);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
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
     * Handle button click events<br />
     * <br />
     * Auto-created on 2016-12-22 11:01:31 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    @Override
    public void onClick(View v) {
        if (v == btnPlaymode) {
            setPlayMode();
        } else if (v == btnAudioPre) {
            if (service != null) {
                try {
                    service.pre();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } else if (v == btnAudioStartPause) {
            try {
                if (service != null) {
                    if (service.isPlaying()) {
                        //暂停
                        service.pause();
                        //按钮-播放
                        btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_play_selector);
                    } else {
                        //播放
                        service.start();
                        //按钮-暂停
                        btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_pause_selector);
                    }
                }

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if (v == btnAudioNext) {
            if (service != null) {
                try {
                    service.next();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } else if (v == btnLyrics) {
            // Handle clicks for btnLyrics
            if(isShowLyric){//显示歌词---关闭歌词
                isShowLyric = false;
                showLyricView.setVisibility(View.GONE);
            }else {//不显示歌词----显示歌词
                showLyricView.setVisibility(View.VISIBLE);
                handler.removeMessages(SHOW_LYRIC);
                handler.sendEmptyMessage(SHOW_LYRIC);
                isShowLyric = true;
            }
        }

    }

    private void setPlayMode() {
        try {
            int playmode = service.getPlayerMode();
            if (playmode == MusicPlayerService.REPEAT_NORMAL) {
                playmode = MusicPlayerService.REPEAT_SINGLE;
            } else if (playmode == MusicPlayerService.REPEAT_SINGLE) {
                playmode = MusicPlayerService.REPEAT_ALL;
            } else if (playmode == MusicPlayerService.REPEAT_ALL) {
                playmode = MusicPlayerService.REPEAT_NORMAL;
            } else {
                playmode = MusicPlayerService.REPEAT_NORMAL;
            }
            //保存
            service.setPlayerMode(playmode);
            //设置模式图片
            showPlaymode();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void showPlaymode() {
        try {
            int playmode = service.getPlayerMode();
            if (playmode == MusicPlayerService.REPEAT_NORMAL) {
                btnPlaymode.setBackgroundResource(R.drawable.btn_now_playing_normal_order_selector);
                Toast.makeText(AudioPlayerActivity.this, "顺序播放", Toast.LENGTH_SHORT).show();
            } else if (playmode == MusicPlayerService.REPEAT_SINGLE) {
                btnPlaymode.setBackgroundResource(R.drawable.btn_now_playing_playmode_single_selector);
                Toast.makeText(AudioPlayerActivity.this, "单曲循环", Toast.LENGTH_SHORT).show();
            } else if (playmode == MusicPlayerService.REPEAT_ALL) {
                btnPlaymode.setBackgroundResource(R.drawable.btn_now_playing_play_all_repeat_selector);
                Toast.makeText(AudioPlayerActivity.this, "全部循环", Toast.LENGTH_SHORT).show();
            } else {
                btnPlaymode.setBackgroundResource(R.drawable.btn_now_playing_normal_order_selector);
                Toast.makeText(AudioPlayerActivity.this, "顺序播放", Toast.LENGTH_SHORT).show();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

   private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SHOW_LYRIC:
                    try {
                        //1.得到当前的进度
                        int currentPosition = service.getCurrentPosition();
                        //2.把进度传入ShowLyricView控件，并且计算该高亮哪一句
                        showLyricView.setShowNextLyric(currentPosition);
                        //3.实时的发消息
                        handler.removeMessages(SHOW_LYRIC);
                        handler.sendEmptyMessage(SHOW_LYRIC);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case PROGRESS:
                    try {
                        ///1.得到当前进度
                        int currentPosition = service.getCurrentPosition();
                        // 2.设置SeekBar.setProgress(进度)
                        seekbarAudio.setProgress(currentPosition);

                        //3.时间进度更新
                        tvTime.setText(utils.stringForTime(currentPosition) + "/" + utils.stringForTime(service.getDuration()));

                        //4.每秒更新一次
                        handler.removeMessages(PROGRESS);
                        handler.sendEmptyMessageDelayed(PROGRESS, 1000);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        findViews();
        getData();
        bindAndStartService();//绑定服务
    }

    private void initData() {
        utils = new Utils();
//        //注册广播
//        myReceivre = new MyReceivre();
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(MusicPlayerService.OPENAUDIO);
//        registerReceiver(myReceivre, intentFilter);
        //1.注册
        EventBus.getDefault().register(this);
    }
    private ServiceConnection con = new ServiceConnection() {
        /**
         * 当连接成功时回调这个方法
        * @param name
        * @param ibinder
        */
        @Override
        public void onServiceConnected(ComponentName name, IBinder ibinder) {
            service = IMusicPlayerService.Stub.asInterface(ibinder);
            try {
                if (service != null) {
                    if (!notification) {//从列表中
                        service.openAudio(position);
                    } else {//从状态栏中
                        showViewData();
                        showLyric();
                        setupVisualizerFxAndUi();
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        /**
         * 当连接断开时回调这个方法
         *  @param name
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            try {
                if (service != null) {
                    service.stop();
                    service = null;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };
    class MyReceivre extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            showData(null);
        }
    }

    //3.订阅方法
//    @Subscribe(threadMode = ThreadMode.MAIN, sticky = false, priority = 0)
    public void showData(MediaItem mediaItem) {//必须时public
        //发消息开始歌词同步
        showLyric();
        showViewData();
        checkPlaymode();
        setupVisualizerFxAndUi();
    }
    public void onEventMainThread(MediaItem mediaItem){
        showLyric();
        showViewData();
        checkPlaymode();
        setupVisualizerFxAndUi();
    }
    /**
     * 生成一个VisualizerView对象，使音频频谱的波段能够反映到 VisualizerView上
     */

    private void setupVisualizerFxAndUi() {
        try {
            int audioSessionid = service.getAudioSessionId();
            System.out.println("audioSessionid=="+audioSessionid);
            mVisualizer = new Visualizer(audioSessionid);
            // 参数内必须是2的位数
            mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
            // 设置允许波形表示，并且捕获它
            baseVisualizerView.setVisualizer(mVisualizer);
            mVisualizer.setEnabled(true);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    private void showLyric() {
        //解析歌词
        LyricUtils lyricUtils = new LyricUtils();
        try {
            String path = service.getAudioPath();
            //传歌词文件
            //mnt/sdcard/audio/beijingbeijing.mp3
            //mnt/sdcard/audio/beijingbeijing.lrc
            path = path.substring(0,path.lastIndexOf("."));
            LogUtil.e("path:"+path);

            File file = new File(path+".lrc");
            if(!file.exists()){
                file = new File(path + ".txt");
            }
            lyricUtils.readLyricFile(file);//解析歌词
            showLyricView.setLyrics(lyricUtils.getLyrics());

        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if(lyricUtils.isExistsLyric()){
            handler.sendEmptyMessage(SHOW_LYRIC);
        }
    }

    /**
     * 校验状态
     */
    private void checkPlaymode() {
        try {
            int playmode = service.getPlayerMode();
            if (playmode == MusicPlayerService.REPEAT_NORMAL) {
                btnPlaymode.setBackgroundResource(R.drawable.btn_now_playing_normal_order_selector);
            } else if (playmode == MusicPlayerService.REPEAT_SINGLE) {
                btnPlaymode.setBackgroundResource(R.drawable.btn_now_playing_playmode_single_selector);
            } else if (playmode == MusicPlayerService.REPEAT_ALL) {
                btnPlaymode.setBackgroundResource(R.drawable.btn_now_playing_play_all_repeat_selector);
            } else {
                btnPlaymode.setBackgroundResource(R.drawable.btn_now_playing_normal_order_selector);
            }

            //校验播放和暂停的按钮
            if (service.isPlaying()) {
                btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_play_selector);
            } else {
                btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_pause_selector);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void showViewData() {
        try {
            tvArtist.setText(service.getArtist());
            tvName.setText(service.getName());
            //设置进度条的最大值
            seekbarAudio.setMax(service.getDuration());
            //发消息
            handler.sendEmptyMessage(PROGRESS);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    private void bindAndStartService() {
        Intent intent = new Intent(this, MusicPlayerService.class);
        intent.setAction("com.longc.mobileplayer_OPENAUDIO");
        bindService(intent, con, Context.BIND_AUTO_CREATE);
        startService(intent);//不至于实例化多个服务

    }

    /**
     * 得到数据
     */
    private void getData() {
        notification = getIntent().getBooleanExtra("notification", false);
        if (!notification) {
            position = getIntent().getIntExtra("position", 0);
        }

    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
//        if (myReceivre != null) {
//            unregisterReceiver(myReceivre);
//            myReceivre = null;
//        }
        //2.取消订阅
        EventBus.getDefault().unregister(this);
        if (con != null) {
            unbindService(con);
            con = null;
        }
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mVisualizer != null){
            mVisualizer.release();
        }
    }
}
