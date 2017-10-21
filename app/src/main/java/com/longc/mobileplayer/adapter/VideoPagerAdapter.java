package com.longc.mobileplayer.adapter;

import android.content.Context;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.longc.mobileplayer.R;
import com.longc.mobileplayer.domain.MediaItem;
import com.longc.mobileplayer.utils.Utils;

import java.util.ArrayList;


/**
 * Created by longc on 2016/12/9.
 */
public class VideoPagerAdapter extends BaseAdapter{

    private final ArrayList<MediaItem> mediaItems;
    /**
     * 是否是视频文件
     */
    private final boolean isVideo;

    Utils utile = new Utils();

    private Context context;

    public VideoPagerAdapter(Context context,ArrayList<MediaItem> mediaItems,boolean isVideo) {
        this.context=context;
        this.mediaItems = mediaItems;
        this.isVideo = isVideo;
    }

    @Override

    public int getCount() {
        return mediaItems.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null) {
            convertView = View.inflate(context,R.layout.item_video_pager, null);
            viewHolder=new ViewHolder();
            viewHolder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
            viewHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            viewHolder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
            viewHolder.tv_size = (TextView) convertView.findViewById(R.id.tv_size);
            convertView.setTag(viewHolder);
        }else{
            viewHolder= (ViewHolder) convertView.getTag();
        }
        //根据position得到列表中对应位置的数据
        MediaItem mediaItem = mediaItems.get(position);
        viewHolder.tv_name.setText(mediaItem.getName());
        viewHolder.tv_size.setText(Formatter.formatFileSize(context, mediaItem.getSize()));
        viewHolder.tv_time.setText(utile.stringForTime((int) mediaItem.getDuration()));
        if(!isVideo){//不是视频，是音乐
            //设置图片
            viewHolder.iv_icon.setImageResource(R.drawable.audio_default_bg);
        }
        return convertView;
    }

    static class ViewHolder{
        ImageView iv_icon;
        TextView tv_name;
        TextView tv_size;
        TextView tv_time;
    }
}
