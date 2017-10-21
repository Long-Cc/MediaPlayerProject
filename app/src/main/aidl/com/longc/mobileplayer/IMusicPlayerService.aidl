// IMusicPlayerService.aidl
package com.longc.mobileplayer;

// Declare any non-default types here with import statements

interface IMusicPlayerService {
/**
     * 根据位置打开音乐
     */
     void openAudio(int position);

    /**
     * 播放音乐
     */
     void start();

    /**
     * 暂停音乐
     */
     void pause();

    /**
     * 停止播放音乐
     */
     void stop();

    /**
     * 得到当前播放进度
     */
     int getCurrentPosition();

    /**
     * 得到播放的总时长
     */
     int getDuration();

    /**
     * 得到艺术家
     */
     String getArtist();

    /**
     * 得到歌曲名字
     */
     String getName();

    /**
     * 播放下一个
     */
     void next();

    /**
     * 播放上一个
     */
     void pre() ;

    /**
     * 设置播放模式
     */
     void setPlayerMode(int playerMode);

    /**
     * 得到播放模式
     */
     int getPlayerMode();
      /**
       * 是否在播放
       */
      boolean isPlaying();
      /**
      *拖动音乐的progressBar
      */
      void seekTo(int progress);
       /**
        * 得到歌曲路径
        */
      String getAudioPath();

      int getAudioSessionId();
}
