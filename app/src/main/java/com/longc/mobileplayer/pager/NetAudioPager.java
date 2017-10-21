package com.longc.mobileplayer.pager;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.longc.mobileplayer.R;
import com.longc.mobileplayer.View.XListView;
import com.longc.mobileplayer.adapter.NetAudioAdapter;
import com.longc.mobileplayer.base.BasePager;
import com.longc.mobileplayer.domain.NetAudioData;
import com.longc.mobileplayer.utils.CacheUtils;
import com.longc.mobileplayer.utils.Constants;
import com.longc.mobileplayer.utils.LogUtil;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 网络音乐界面
 * @author longc
 *
 */
public class NetAudioPager extends BasePager {

	/**
	 * 是否已经加载更多了
	 */
	private boolean isLoadMore = false;

	@ViewInject(R.id.listview)
	private XListView xListView;

	@ViewInject(R.id.tv_nonet)
	private TextView tvnonet;

	@ViewInject(R.id.pb_loading)
	private ProgressBar pbloading;

	private List<NetAudioData.ListEntity> datas;
	private NetAudioAdapter adapter;

	public NetAudioPager(Context context) {
		super(context);
	}

	@Override
	public View initView() {
		View view = View.inflate(context, R.layout.netaudio_pager,null);
		x.view().inject(NetAudioPager.this, view);
		xListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Toast.makeText(context,"还未设计，敬请期待！",Toast.LENGTH_SHORT).show();
			}
		});

		xListView.setPullLoadEnable(true);
		xListView.setXListViewListener(new myOnXListViewListener());
		return view;
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

	private void getMoreDataFromNet() {
		RequestParams param = new RequestParams(Constants.ALL_RES_URL);
		x.http().get(param, new Callback.CommonCallback<String>() {
			@Override
			public void onSuccess(String result) {
				LogUtil.e("onSuccess==="+result);
				isLoadMore = true;
				processData(result);
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				LogUtil.e("onError==="+ex.getMessage());
				isLoadMore = false;
			}

			@Override
			public void onCancelled(CancelledException cex) {
				LogUtil.e("onCancelled==="+cex.getMessage());
				isLoadMore = false;
			}

			@Override
			public void onFinished() {
				LogUtil.e("onFinished===");
				isLoadMore = false;
			}
		});
	}


	@Override
	public void initData() {
		super.initData();
		LogUtil.e("网络数据的初始化......");
		String saveJson = CacheUtils.getString(context, Constants.ALL_RES_URL);
		if(!TextUtils.isEmpty(saveJson)){
			//解析数据
			processData(saveJson);
		};
		getDataFromNet();
	}
	/**
	 * 解析json数据和显示数据
	 * 解析数据：1.GsonFormat生成bean对象；
	 * 			2.用gson解析数据
	 * @param json
	 */
	private void processData(String json) {
		NetAudioData data = parsedJson(json);
		//设置适配器
		if(!isLoadMore){//不加载更多
			datas = data.getList();
			showData();
		}else{//加载更多
			isLoadMore = false;
			//要把得到更多的数据，添加到原来的集合中
			NetAudioData moredata = parsedJson(json);
			datas.addAll(moredata.getList());
			//刷新适配器
			adapter.notifyDataSetChanged();
			onLoad();
		}
	}

	private void showData() {
		if(datas != null && datas.size() > 0){
            //有数据
            adapter = new NetAudioAdapter(context,datas);
            xListView.setAdapter(adapter);
            onLoad();
        }else{
            //未接收到数据
            tvnonet.setText("未接收到数据...");
            tvnonet.setVisibility(View.VISIBLE);
        }
		pbloading.setVisibility(View.GONE);
	}

	private void onLoad() {
		xListView.stopRefresh();
		xListView.stopLoadMore();
		xListView.setRefreshTime("" + getSysTime());
	}

	/**
	 * 得到系统时间
	 * @return
	 */
	public String getSysTime() {
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
		return format.format(new Date());
	}

	private NetAudioData parsedJson(String json) {
		return new Gson().fromJson(json,NetAudioData.class);
	}


	public void getDataFromNet() {
		RequestParams param = new RequestParams(Constants.ALL_RES_URL);
		x.http().get(param, new Callback.CommonCallback<String>() {
			@Override
			public void onSuccess(String result) {
				LogUtil.e("onSuccess==="+result);
				CacheUtils.putString(context, Constants.ALL_RES_URL, result);
				processData(result);
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				LogUtil.e("onError===" + ex.getMessage());
				showData();
			}

			@Override
			public void onCancelled(CancelledException cex) {
				LogUtil.e("onCancelled==="+cex.getMessage());
			}

			@Override
			public void onFinished() {
				LogUtil.e("onFinished===");
			}
		});
	}


}
