package com.longc.mobileplayer.pager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.longc.mobileplayer.R;
import com.longc.mobileplayer.View.XListView;
import com.longc.mobileplayer.activity.SystemVideoPlayer;
import com.longc.mobileplayer.adapter.NetVideoPagerAdapter;
import com.longc.mobileplayer.base.BasePager;
import com.longc.mobileplayer.domain.MediaItem;
import com.longc.mobileplayer.utils.CacheUtils;
import com.longc.mobileplayer.utils.Constants;
import com.longc.mobileplayer.utils.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * 网络音乐界面
 *
 * @author longc
 */
public class NetVideoPager extends BasePager {

    private ArrayList<MediaItem> mediaItems;
    private NetVideoPagerAdapter adapter;

    @ViewInject(R.id.listview )
    private XListView xlistView;

    @ViewInject(R.id.tv_nonet)
    private TextView tv_nonet;

    @ViewInject(R.id.pb_loading)
    private ProgressBar pb_loading;
    /**
     * 是否已经加载更多了
     */
    private boolean isLoadMore = false;

    public NetVideoPager(Context context) {
        super(context);
    }

    @Override
    public View initView() {
        View view = View.inflate(context, R.layout.netvideo_pager, null);
        x.view().inject(this, view);
        xlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MediaItem mediaItem = mediaItems.get(position);
                //传递列表数据对象-序列化(bundle(mediaItems)-position)
                Intent intent = new Intent(context, SystemVideoPlayer.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("videolist", mediaItems);
                intent.putExtras(bundle);
                intent.putExtra("position", position-1);
                context.startActivity(intent);
            }

        });
        xlistView.setPullLoadEnable(true);
        xlistView.setXListViewListener(new myOnXListViewListener());
        return view;
    }

    private void getMoreDataFromNet() {
        //联网
        //视频内容
        RequestParams params = new RequestParams(Constants.NET_URL);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                LogUtil.e("联网成功==" + result);
                isLoadMore = true;
                //主线程
                processData(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                LogUtil.e("联网失败==" + ex.getMessage());
                isLoadMore = false;
            }

            @Override
            public void onCancelled(CancelledException cex) {
                LogUtil.e("onCancelled==" + cex.getMessage());
                isLoadMore = false;
            }

            @Override
            public void onFinished() {
                LogUtil.e("onFinished==");
                isLoadMore = false;
            }
        });
    }



    class myOnXListViewListener implements XListView.IXListViewListener {

        @Override
        public void onRefresh() {
            getDataFromNet();
        }

        @Override
        public void onLoadMore() {
            getMoreDataFromNet();
        }
    }

    @Override
    public void initData() {
        super.initData();
        String saveJSON = CacheUtils.getString(context,Constants.NET_URL);
        if(!TextUtils.isEmpty(saveJSON)){
            processData(saveJSON);
        }
        getDataFromNet();
    }

    private void getDataFromNet() {
        RequestParams params = new RequestParams(Constants.NET_URL);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                /**
                 * 联网成功
                 */
                LogUtil.e("onSuccess***" + result);
                CacheUtils.putString(context,Constants.NET_URL,result);
                processData(result);

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                LogUtil.e("onError***" + ex.getMessage());
                showData();
            }

            @Override
            public void onCancelled(CancelledException cex) {
                LogUtil.e("onCancelled***" + cex.getMessage());
            }

            @Override
            public void onFinished() {
                LogUtil.e("onFinished***");
            }
        });
    }

    private void processData(String json) {
        if(!isLoadMore){//不加载更多
            mediaItems = paseJSON(json);
            showData();

        }else{
            isLoadMore = false;
            //加载更多
            //要把得到更多的数据，添加到原来的集合中
            ArrayList<MediaItem> moredata = paseJSON(json);
            mediaItems.addAll(moredata);
            //刷新适配器
            adapter.notifyDataSetChanged();
            onLoad();
        }

    }

    private void showData() {
        //设置适配器
        if (mediaItems != null && mediaItems.size() > 0) {
            //有数据
            //设置Adapter
            adapter = new NetVideoPagerAdapter(context,mediaItems);
            xlistView.setAdapter(adapter);
            onLoad();
            //把文本隐藏
            tv_nonet.setVisibility(View.GONE);
        } else {
            //没有数据net
            //显示文本
            tv_nonet.setVisibility(View.VISIBLE);
        }
        //把progressBar隐藏
        pb_loading.setVisibility(View.GONE);
    }

    private void onLoad() {
        xlistView.stopRefresh();
        xlistView.stopLoadMore();
        xlistView.setRefreshTime("更新时间：" + getSysTime());
    }
    /**
     * 得到系统时间
     * @return
     */
    public String getSysTime() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(new Date());
    }
    /**
     * 解析json数据
     * 1.用系统接口
     * 2.用第三方解析工具（Gson fastjson）
     *
     * @param json
     * @return
     */
    private ArrayList<MediaItem> paseJSON(String json) {
        ArrayList<MediaItem> mediaItems = new ArrayList<MediaItem>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.optJSONArray("trailers");
            if (jsonArray != null && jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObjectItem = (JSONObject) jsonArray.get(i);
                    if (jsonObjectItem != null) {
                        MediaItem mediaItem = new MediaItem();

                        String movName = jsonObjectItem.optString("movieName");//name
                        mediaItem.setName(movName);

                        String videoTitle = jsonObjectItem.optString("videoTitle");//dec
                        mediaItem.setDesc(videoTitle);

                        String coverImg = jsonObjectItem.optString("coverImg");//imageUrl
                        mediaItem.setImageUrl(coverImg);

                        String hightUrl = jsonObjectItem.optString("hightUrl");//data
                        mediaItem.setData(hightUrl);

                        mediaItems.add(mediaItem);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mediaItems;
    }

}
