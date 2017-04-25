package com.zxly.o2o.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;


import com.easemob.easeui.widget.viewpagerindicator.PagerSlidingTabStrip;
import com.easemob.easeui.widget.viewpagerindicator.ViewPageFragmentAdapter;
import com.zxly.o2o.activity.FragmentListAct;
import com.zxly.o2o.activity.QrCodePromotionAct;
import com.zxly.o2o.application.AppController;
import com.zxly.o2o.pullrefresh.PullToRefreshAdapterViewBase;
import com.zxly.o2o.pullrefresh.PullToRefreshBase;
import com.zxly.o2o.shop.R;
import com.zxly.o2o.util.CallBack;
import com.zxly.o2o.util.ViewUtils;
import com.zxly.o2o.view.FixedViewPager;
import com.zxly.o2o.view.MPagerSlidingTab;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("InflateParams")
public class PromotionFragment extends BaseViewPageFragment implements OnClickListener {

	private View btnQrCode;
	private TextView txtTitle;
	public static PromotionFragment newInstance(){
		PromotionFragment f=new PromotionFragment();
		return f;
	}

	@Override
	protected void initView() {
		pager = (FixedViewPager) findViewById(viewPagerId());
		tabs = (PagerSlidingTabStrip) findViewById(tabsId());
		pager.setTouchIntercept(false);
		findViewById(R.id.btn_back).setVisibility(View.INVISIBLE);
		txtTitle= (TextView) findViewById(R.id.txt_title);
		txtTitle.setText("找客");
		btnQrCode=findViewById(R.id.btn_qrCode);
		this.setSelectedTextColor("#ff5f19");
		this.setIndicatorColor("#ff5f19");
		tabs.setIndicatorColor(Color.parseColor("#626262"));
		DisplayMetrics dm = getResources().getDisplayMetrics();
		int tabPaddingLeft = (int) TypedValue
				.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, dm);
		int tabPaddingRight = (int) TypedValue
				.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, dm);
		tabs.setTabPaddingLeft(tabPaddingLeft);
		tabs.setTabPaddingRight(tabPaddingRight);
		StoreArticleFragement storeArticleFragement = StoreArticleFragement.newInstance(1);
		storeArticleFragement.setParam(callBack);
		fragments.add(storeArticleFragement);
		fragments.add(new TerraceArticleFragement());
		fragments.add(PromotionArticleFragment.newInstance());
		fragments.add(PromotionAcitcityFragment.newInstance());
		pager.setAdapter(getAdapter());
		tabs.setViewPager(pager);
		btnQrCode.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				QrCodePromotionAct.start(getActivity());
			}
		});
		setTabsValue(tabs);
	}

	CallBack callBack=new CallBack() {
		@Override
		public void onCall() {
			pager.setCurrentItem(1);
		}
	};
	@Override
	protected String[] tabName() {
		return new String[]{"店铺文章","网络热文","自定义文章","活动"};
	}
	@Override
	protected int layoutId() {

		return R.layout.win_promotion_article;
	}

	@Override
	protected List<Fragment> fragments() {
		return fragments;
	}

	@Override
	public void onClick(View v) {

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		callBack=null;
	}
}
