package com.longc.mobileplayer.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

import com.longc.mobileplayer.domain.Lyric;
import com.longc.mobileplayer.utils.DensityUtil;

import java.util.ArrayList;


/**
 * Created by longc on 2016/12/24.
 */
public class ShowLyricView extends TextView {
    /**
     * 歌词列表
     */
    private ArrayList<Lyric> lyrics;
    private Paint paint;
    private Paint otherpaint;

    private int width;
    private int height;
    /**
     * 歌词列表中的索引，是第几句歌词
     */
    private int index;
    /**
     * 每行的高
     */
    private float textHeight ;
    /**
     * 当前播放进度
     */
    private float currentPosition;
    /**
     * 高亮显示的时间或者休眠时间
     */
    private float sleepTime;
    /**
     * 时间戳，什么时刻到高亮哪句歌词
     */
    private float timePoint;




    public ShowLyricView(Context context) {
        this(context, null);
    }

    public ShowLyricView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShowLyricView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
    }

    private void initView(Context context) {
        textHeight = DensityUtil.dip2px(context,18);
        //绿色画笔
        paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setAntiAlias(true);
        paint.setTextSize(DensityUtil.dip2px(context, 16));
        //位置居中
        paint.setTextAlign(Paint.Align.CENTER);

        //白色画笔
        otherpaint = new Paint();
        otherpaint.setColor(Color.parseColor("#DCDCDC"));
        otherpaint.setTextSize(DensityUtil.dip2px(context, 16));
        otherpaint.setAntiAlias(true);
        //位置居中
        otherpaint.setTextAlign(Paint.Align.CENTER);
//
//        lyrics = new ArrayList<>();
//        Lyric lyric = new Lyric();
//        for (int i = 0; i < 1000; i++) {
//            lyric.setSleepTime(1500 + i);
//            lyric.setTimePoint(1000 * i);
//            lyric.setContent(i + "bcbcbcbbcbcb" + i);
//            lyrics.add(lyric);
//            lyric = new Lyric();
//        }
    }

    /**
     * 设置歌词列表
     *
     * @param lyrics
     */
    public void setLyrics(ArrayList<Lyric> lyrics) {
        this.lyrics = lyrics;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (lyrics != null && lyrics.size() > 0) {

            //往上推移

            float plush = 0;
            if(sleepTime ==0){
                plush = 0;
            }else{
                //平移
                //这一句所花的时间 ：休眠时间 = 移动的距离 ： 总距离（行高）
                //移动的距离 =  (这一句所花的时间 ：休眠时间)* 总距离（行高）
//                float delta = ((currentPosition-timePoint)/sleepTime )*textHeight;

                //屏幕的的坐标 = 行高 + 移动的距离
                plush = textHeight + ((currentPosition-timePoint)/sleepTime )*textHeight;
            }
            canvas.translate(0,-plush);

            //绘制歌词
            //绘制当前部分
            String currentText = lyrics.get(index).getContent();
            canvas.drawText(currentText, width / 2, height / 2, paint);
            //绘制前面部分
            float tempY = height / 2;//Y轴中间坐标
            for (int i = index - 1; i >= 0; i--) {
                //每一句歌词
                String preContent = lyrics.get(i).getContent();
                tempY = tempY - textHeight;
                if (tempY < 0) {
                    break;
                }
                canvas.drawText(preContent, width / 2, tempY, otherpaint);
            }

            //绘制后面部分

            tempY = height / 2;//Y轴中间坐标
            for (int i = index + 1; i < lyrics.size(); i++) {
                //每一句歌词
                String nextContent = lyrics.get(i).getContent();
                tempY = tempY + textHeight;
                if (tempY > height) {
                    break;
                }
                canvas.drawText(nextContent, width / 2, tempY, otherpaint);
            }
        } else {
            //没有歌词数据
            canvas.drawText("没有发现歌词...", width / 2, height / 2, paint);
        }
    }
    /**
     * 根据当前播放的位置，找出该高亮显示哪句歌词
     *
     * @param currentPosition
     */
    public void setShowNextLyric(int currentPosition) {
        this.currentPosition = currentPosition;
        if(lyrics == null || lyrics.size() == 0)
            return;
        for (int i = 1;i <lyrics.size();i++){

            if(currentPosition < lyrics.get(i).getTimePoint()){
                int tempIndex = i-1;
                if(currentPosition >= lyrics.get(tempIndex).getTimePoint()){
                    //当前正在播放的哪句歌词
                    index = tempIndex;
                    sleepTime = lyrics.get(index).getSleepTime();
                    timePoint = lyrics.get(index).getTimePoint();
                }
            }
        }
        //重新绘制
        invalidate();//在主线程中
    }

}
