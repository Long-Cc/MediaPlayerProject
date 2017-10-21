package com.longc.mobileplayer.domain;

import java.io.Serializable;

/**
 * Created by longc on 2016/12/9.
 */
public class MediaItem implements Serializable {
    private String name;
    private long duration;
    private long size;
    private String data;
    private String aritst;
    private String imageUrl;
    private String desc;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }


    public String getName() {
        return name;
    }

    public long getDuration() {
        return duration;
    }

    public long getSize() {
        return size;
    }

    public String getData() {
        return data;
    }

    public String getAritst() {
        return aritst;
    }

    public void setName(String name) {
        /**
         * 去掉.mp3与.mp4
         */
        int end = name.length();
        String str = name.substring(end-4,end);
        if(str.equals(".mp3")||str.equals(".mp4")||str.equals(".flv")){
            this.name = name.substring(0,end-4);
        }else{
            this.name = name;
        }
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setAritst(String aritst) {
        this.aritst = aritst;
    }

    @Override
    public String toString() {
        return "MediaItem{" +
                "name='" + name + '\'' +
                ", duration=" + duration +
                ", size=" + size +
                ", data='" + data + '\'' +
                ", aritst='" + aritst + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", desc='" + desc + '\'' +
                '}';
    }
}
