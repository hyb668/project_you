/*
 * 文件名：MyOrderListFragment.java
 * 版权：Copyright 2015 Yiba Tech. Co. Ltd. All Rights Reserved. 
 * 描述： MyOrderListFragment.java
 * 修改人：wuchenhui
 * 修改时间：2015-5-27
 * 修改内容：新增
 */
package com.zxly.o2o.fragment;


import u.aly.ar;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;

import com.easemob.easeui.AppException;
import com.easemob.easeui.utils.GsonParser;
import com.google.gson.reflect.TypeToken;
import com.zxly.o2o.account.Account;
import com.zxly.o2o.activity.MyOrderAct;
import com.zxly.o2o.adapter.MyOrderAdapter;
import com.zxly.o2o.adapter.ObjectAdapter;
import com.zxly.o2o.model.BuyItem;
import com.zxly.o2o.model.OrderInfo;
import com.zxly.o2o.model.OrderStatistics;
import com.zxly.o2o.pullrefresh.PullToRefreshBase;
import com.zxly.o2o.pullrefresh.PullToRefreshListView;
import com.zxly.o2o.pullrefresh.PullToRefreshBase.Mode;
import com.zxly.o2o.pullrefresh.PullToRefreshBase.OnRefreshListener;
import com.zxly.o2o.request.BaseRequest;
import com.zxly.o2o.request.BaseRequest.ResponseStateListener;
import com.zxly.o2o.request.MyOrderListRequest;
import com.zxly.o2o.shop.R;
import com.zxly.o2o.util.Constants;
import com.zxly.o2o.util.DataCallBack;
import com.zxly.o2o.util.DataUtil;
import com.zxly.o2o.util.DesityUtil;
import com.zxly.o2o.util.ViewUtils;
import com.zxly.o2o.view.LoadingView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * TODO 添加类的一句话简单描述。
 * <p>
 * TODO 详细描述
 * <p>
 * TODO 示例代码
 * <pre>
 * </pre>
 * 
 * @author     wuchenhui
 * @version    YIBA-O2O 2015-5-27
 * @since      YIBA-O2O
 */
public class MyOrderListFragment extends BaseFragment implements ResponseStateListener {
	
	private PullToRefreshListView mListView;
	private ObjectAdapter adapter;
	private LoadingView loadingView;
	private int status=-1;
	private int pageIndex=1;
	private int type;
	OrderListRequest request;
	public static MyOrderListFragment newInstance(int type,int status,DataCallBack calllBack){
		MyOrderListFragment f=new MyOrderListFragment();
		Bundle args = new Bundle();
		args.putInt("status", status);
		args.putInt("type", type);
		f.setArguments(args);
		return f;
	}

	public static MyOrderListFragment newInstance(int type,int status){
		MyOrderListFragment f=new MyOrderListFragment();
		Bundle args = new Bundle();
		args.putInt("status", status);
		args.putInt("type", type);
		f.setArguments(args);
		return f;
	}

	
	@Override
	protected void initView(Bundle bundle) {
		loadingView=(LoadingView) findViewById(R.id.view_loading11);
		mListView = (PullToRefreshListView) findViewById(R.id.listview);
	//	mListView.setDivideHeight(DesityUtil.dp2px(getActivity(), 10));
        mListView.setIntercept(true);
		ViewUtils.setRefreshText(mListView);

		mListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                if (refreshView.getCurrentMode() == Mode.PULL_FROM_START) {
                    // 加载下啦数据
                    loadData(1);
                }
                if (refreshView.getCurrentMode() == Mode.PULL_FROM_END) {
                    // 加载上拉数据
                    loadData(pageIndex);
                }

            }
        });

		 
		loadingView.setOnAgainListener(new LoadingView.OnAgainListener() {
			@Override
			public void onLoading() {
				loadingView.startLoading();
				loadData(1);
			}
		});

		if (adapter==null){
			Bundle args=getArguments();	
			status=args.getInt("status");
			type=args.getInt("type");
			adapter = new MyOrderAdapter(getActivity());
			((MyOrderAdapter)adapter).setOrderType(type);
		}

		mListView.setAdapter(adapter);

		if(request==null){
			request = new OrderListRequest(status);
			request.setOnResponseStateListener(this);
		}


	}

	@Override
	protected void loadInitData() {
		loadData(1);
	}

	@Override
	protected int layoutId() {
		return R.layout.tag_listview;
	}

	public void loadData(final int pageId) {
		pageIndex=pageId;
		if(DataUtil.listIsNull(adapter.getContent()))
		  loadingView.startLoading();

		request.setPageIndex(pageId);
		request.start(getActivity());
	}

	@Override
	public void onOK() {
		if (!DataUtil.listIsNull(request.getOrderList())) {
			if(pageIndex==1)
				adapter.clear();
			
			adapter.addItem(request.getOrderList(), true);
			request.setOrderList(null);
			pageIndex++;
			loadingView.onLoadingComplete();
		} else {
			//下拉刷新的时候发现数据为空，
			if(pageIndex==1){
				adapter.clear();
				adapter.notifyDataSetChanged();
				loadingView.onDataEmpty();
			}else{
				//最后一页
			}
		}
		
		if (mListView.isRefreshing())
			mListView.onRefreshComplete();	
	}

	@Override
	public void onFail(int code) {
		if(DataUtil.listIsNull(adapter.getContent()))
			loadingView.onLoadingFail();
		
		if (mListView.isRefreshing())
			mListView.onRefreshComplete();		
	}


	@Override
	protected void initView() {
	}


	class OrderListRequest extends BaseRequest{

		private List<OrderInfo> orderList;

		private OrderStatistics statics;

		private int orderRequestStatus;

		public OrderListRequest(int status){
			orderRequestStatus=status;
			addParams("shopId",Account.user.getShopId());
			addParams("status",status);
		}

		public void setPageIndex(int pageIndex){
			addParams("pageIndex",pageIndex);
		}

		public List<OrderInfo> getOrderList() {
			return orderList;
		}

		public void setOrderList(List<OrderInfo> orderList) {
			this.orderList = orderList;
		}

		public OrderStatistics getStatics() {
			return statics;
		}

		@Override
		protected void fire(String data) throws AppException {

			if(type!=MyOrderAct.TYPE_ORDER_LIST){
				TypeToken<List<OrderInfo>> types = new TypeToken<List<OrderInfo>>() {};
				orderList= GsonParser.getInstance().fromJson(data, types);
				return;
			}

			try {
				JSONObject	json = new JSONObject(data);

				if (json.has("statics")) {
					String str = json.getString("statics");
					statics= GsonParser.getInstance().getBean(str, OrderStatistics.class);
				}

				if (json.has("orderList")) {
					String str = json.getString("orderList");
					TypeToken<List<OrderInfo>> types = new TypeToken<List<OrderInfo>>() {};
					orderList= GsonParser.getInstance().fromJson(str, types);

					//服务端叼毛不肯加状态，自行处理状态
					if (!DataUtil.listIsNull(orderList)&&orderRequestStatus==MyOrderAct.ORDER_REQUEST_REFUND){
						for (OrderInfo orderInfo:orderList) {
							orderInfo.setStatus(Constants.ORDER_REFUNDING);
							List<BuyItem> items=orderInfo.getBuyItems();
							for (BuyItem item :items){
								if(item.getStatus()== Constants.PRODUCT_STATE_FERUND_SUCCESS){
									//设置订单状态为退款成功
									orderInfo.setStatus(Constants.ORDER_REFUND_SUCCESS);
									break;
								}
							}
						}
					}

				}

			} catch (JSONException e) {
				e.printStackTrace();
			}


		}

		@Override
		protected String method() {
			if(type==MyOrderAct.TYPE_ORDER_LIST){
				return "/shop/order/list";
			}

			return "/shop/delivery/list";
		}

	}


}
