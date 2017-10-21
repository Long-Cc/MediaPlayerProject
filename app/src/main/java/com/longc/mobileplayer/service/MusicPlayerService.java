package com.longc.mobileplayer.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.longc.mobileplayer.IMusicPlayerService;
import com.longc.mobileplayer.R;
import com.longc.mobileplayer.activity.AudioPlayerActivity;
import com.longc.mobileplayer.domain.MediaItem;
import com.longc.mobileplayer.utils.CacheUtils;

import java.io.IOException;
import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * Created by longc on 2016/12/21.
 */
public class MusicPlayerService extends Service {

    public static final String OPENAUDIO = "com.longc.mobileplayer_OPENAUDIO";
    private ArrayList<MediaItem> mediaItems;
    private int position;
    /**
     * 当前播放的音频文件对象
     */
    private MediaItem mediaItem;
    /**
     * 播放音乐
     */
    private MediaPlayer mediaPlayer;
    private NotificationManager manager;

    /**
     * 播放模式
     */
    private int playmode = REPEAT_NORMAL;
    public static final int REPEAT_NORMAL = 1;//正常播放
    public static final int REPEAT_SINGLE = 2;//单曲循环
    public static final int REPEAT_ALL = 3;//全部循环


    @Override
    public void onCreate() {
        playmode = CacheUtils.getPlaymode(this, "playMode");
        super.onCreate();
        //加载音乐列表
        getDataFromLocal();
    }

    private void getDataFromLocal() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                mediaItems = new ArrayList<>();
                ContentResolver resolver = getContentResolver();
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
            }
        }.start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return stub;
    }

    private IMusicPlayerService.Stub stub = new IMusicPlayerService.Stub() {
        MusicPlayerService service = MusicPlayerService.this;

        /**
         * 打开播放器
         * @param position
         * @throws RemoteException
         */
        @Override
        public void openAudio(int position) throws RemoteException {
            service.openAudio(position);
        }

        /**
         * 开始播放
         * @throws RemoteException
         */
        @Override
        public void start() throws RemoteException {
            service.start();
        }

        /**
         * 暂停播放
         * @throws RemoteException
         */
        @Override
        public void pause() throws RemoteException {
            service.pause();
        }

        /**
         * 停止播放
         * @throws RemoteException
         */
        @Override
        public void stop() throws RemoteException {
            service.stop();
        }

        /**
         * 得到当前播放位置
         * @return
         * @throws RemoteException
         */
        @Override
        public int getCurrentPosition() throws RemoteException {
            return service.getCurrentPosition();
        }

        /**
         * 得到当前播放进度
         * @return
         * @throws RemoteException
         */
        @Override
        public int getDuration() throws RemoteException {
            return (int) service.getDuration();
        }

        /**
         * 得到艺术系
         * @return
         * @throws RemoteException
         */
        @Override
        public String getArtist() throws RemoteException {
            return service.getArtist();
        }

        /**
         * 得到歌曲名字
         * @return
         * @throws RemoteException
         */
        @Override
        public String getName() throws RemoteException {
            return service.getName();
        }

        /**
         * 播放下一首
         * @throws RemoteException
         */
        @Override
        public void next() throws RemoteException {
            service.next();
        }

        /**
         * 播放上一首
         * @throws RemoteException
         */
        @Override
        public void pre() throws RemoteException {
            service.pre();
        }

        /**
         * 设置播放模式
         * @param playerMode
         * @throws RemoteException
         */
        @Override
        public void setPlayerMode(int playerMode) throws RemoteException {
            service.setPlayerMode(playerMode);
        }

        /**
         * 得到播放模式
         * @return
         * @throws RemoteException
         */
        @Override
        public int getPlayerMode() throws RemoteException {
            return service.getPlayerMode();
        }

        /**
         * 是否在播放
         * @return
         * @throws RemoteException
         */
        @Override
        public boolean isPlaying() throws RemoteException {
            return service.isPlaying();
        }

        /**
         *
         * @param position
         * @throws RemoteException
         */
        @Override
        public void seekTo(int position) throws RemoteException {
            mediaPlayer.seekTo(position);
        }

        /**
         * 得到播放路径
         * @return
         * @throws RemoteException
         */
        @Override
        public String getAudioPath() throws RemoteException {
            return mediaItem.getData();
        }

        /**
         *
         * @return
         * @throws RemoteException
         */
        @Override
        public int getAudioSessionId() throws RemoteException {
            return mediaPlayer.getAudioSessionId();
        }

    };


    /**
     * 根据位置打开音乐
     */
    private void openAudio(int position) {
        this.position = position;
        if (mediaItems != null && mediaItems.size() > 0) {
            mediaItem = mediaItems.get(position);
            if (mediaPlayer != null) {
//                mediaPlayer.release();
                mediaPlayer.reset();
            }
            try {
                mediaPlayer = new MediaPlayer();
                //设置监听：播放出错，播放完成，准备好
                mediaPlayer.setOnPreparedListener(new MyOnPreparedListener());
                mediaPlayer.setOnCompletionListener(new MyOnCompletionListener());
                mediaPlayer.setOnErrorListener(new MyOnErrorListener());
                mediaPlayer.setDataSource(mediaItem.getData());
                mediaPlayer.prepareAsync();

                if (playmode == MusicPlayerService.REPEAT_SINGLE) {
                    //单曲循环播放-不会触发播放完成的回调
                    mediaPlayer.setLooping(true);
                } else {
                    //不循环播放
                    mediaPlayer.setLooping(false);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(MusicPlayerService.this, "没有数据....", Toast.LENGTH_SHORT).show();
        }
    }

    class MyOnErrorListener implements MediaPlayer.OnErrorListener {

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            next();
            return true;
        }
    }

    class MyOnCompletionListener implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mp) {
            next();
        }
    }

    class MyOnPreparedListener implements MediaPlayer.OnPreparedListener {
        @Override
        public void onPrepared(MediaPlayer mp) {
            //通知Activity获取信息
//            notifyChange(OPENAUDIO);
            //5.发订阅消息
            EventBus.getDefault().post(mediaItem);
            start();
        }
    }

    private void notifyChange(String action) {
        Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    /**
     * 播放音乐
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void start() {
        mediaPlayer.start();
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, AudioPlayerActivity.class);
        intent.putExtra("notification", true);//标识来自状态拦
        PendingIntent pandingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.notification_music_playing)
                .setContentTitle("酷乐音乐")
                .setContentText("正在播放：" + getName())
                .setContentIntent(pandingIntent)
                .build();
        manager.notify(1, notification);
    }

    /**
     * 暂停音乐
     */
    private void pause() {
        mediaPlayer.pause();
        manager.cancel(1);
    }

    /**
     * 停止播放音乐
     */
    private void stop() {
    }

    /**
     * 得到当前播放进度
     */
    private int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    /**
     * 得到播放的总时长
     */
    private long getDuration() {
        return mediaPlayer.getDuration();
    }

    /**
     * 得到艺术家
     */
    private String getArtist() {
        return mediaItem.getAritst();
    }

    /**
     * 得到歌曲名字
     */
    private String getName() {
        return mediaItem.getName();
    }
    /**
     * 得到歌曲播放的路径
     *
     * @return
     */
    private String getAudioPath() {
        return mediaItem.getData();
    }

    /**
     * 播放下一个
     */
    private void next() {
        //1.根据当前的播放模式，设置下一个的位置
        setNextPosition();
        //2.根据当前的播放模式和下标位置去播放音频
        openNextAudio();
    }

    private void openNextAudio() {
        int playmode = getPlayerMode();
        if (playmode == MusicPlayerService.REPEAT_NORMAL) {
            if (position < mediaItems.size()) {
                //正常范围
                openAudio(position);
            } else {
                position = mediaItems.size() - 1;
            }
        } else if (playmode == MusicPlayerService.REPEAT_SINGLE) {
            openAudio(position);
        } else if (playmode == MusicPlayerService.REPEAT_ALL) {
            openAudio(position);
        } else {
            if (position < mediaItems.size()) {
                //正常范围
                openAudio(position);
            } else {
                position = mediaItems.size() - 1;
            }
        }
    }

    private void setNextPosition() {
        int playmode = getPlayerMode();
        if (playmode == MusicPlayerService.REPEAT_NORMAL) {
            position++;
        } else if (playmode == MusicPlayerService.REPEAT_SINGLE) {
            position++;
            if (position >= mediaItems.size()) {
                position = 0;
            }
        } else if (playmode == MusicPlayerService.REPEAT_ALL) {
            position++;
            if (position >= mediaItems.size()) {
                position = 0;
            }
        } else {
            position++;
        }
    }

    /**
     * 播放上一个
     */
    private void pre() {
        //1.根据当前的播放模式，设置上一个的位置
        setPrePosition();
        //2.根据当前的播放模式和下标位置去播放音频
        openPretAudio();
    }

    private void openPretAudio() {
        int playmode = getPlayerMode();
        if (playmode == MusicPlayerService.REPEAT_NORMAL) {
            if (position >= 0) {
                //正常范围
                openAudio(position);
            } else {
                position = 0;
            }
        } else if (playmode == MusicPlayerService.REPEAT_SINGLE) {
            openAudio(position);
        } else if (playmode == MusicPlayerService.REPEAT_ALL) {
            openAudio(position);
        } else {
            if (position >= 0) {
                //正常范围
                openAudio(position);
            } else {
                position = 0;
            }
        }
    }

    private void setPrePosition() {
        int playmode = getPlayerMode();
        if (playmode == MusicPlayerService.REPEAT_NORMAL) {
            position--;
        } else if (playmode == MusicPlayerService.REPEAT_SINGLE) {
            position--;
            if (position < 0) {
                position = mediaItems.size()-1;
            }
        } else if (playmode == MusicPlayerService.REPEAT_ALL) {
            position--;
            if (position < 0) {
                position = mediaItems.size()-1;
            }
        } else {
            position--;
        }
    }

    /**
     * 设置播放模式
     */
    private void setPlayerMode(int playerMode) {
        this.playmode = playerMode;
        CacheUtils.putPlaymode(this, "playMode", playerMode);
        if (playmode == MusicPlayerService.REPEAT_SINGLE) {
            //单曲循环播放-不会触发播放完成的回调
            mediaPlayer.setLooping(true);
        } else {
            //不循环播放
            mediaPlayer.setLooping(false);
        }
    }

    /**
     * 得到播放模式
     */
    private  int getPlayerMode() {
        return playmode;
    }

    /**
     * 是否在播放音频
     *
     * @return
     */
    private boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }


}
