package com.zxly.o2o.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.zxly.o2o.account.Account;
import com.zxly.o2o.dialog.ShareDialog;
import com.zxly.o2o.model.ActicityInfo;
import com.zxly.o2o.model.ShareInfo;
import com.zxly.o2o.request.BaseRequest;
import com.zxly.o2o.service.RemoteInvokeService;
import com.zxly.o2o.shop.R;
import com.zxly.o2o.util.ShareListener;
import com.zxly.o2o.util.ViewUtils;
import com.zxly.o2o.view.LoadingView;

/**
 * todo:
 * 这个界面类型复杂之后分类难处理，要重新梳理
 *
 * @author wuchenhui 2015-7-2
 * @description H5页面
 */
public class H5DetailAct extends BasicAct implements View.OnClickListener {
	public static final int TYPE_OPEN_PRODUCT_H5=1;
	public static final int TYPE_OPEN_ARTICLE_H5=2;
	public static final int TYPE_OPEN_ACTICITY_H5=3;

	public static final int TYPE_DEFAULT=-1;
	public static final int TYPE_H5_GAME=5;

	public static final int TYPE_GAME=1; //H5游戏

	private int pageType;

	private LoadingView loadingview = null;
	private WebView webView;
	private String loadUrl;
	private String title;
	static ShareInfo shareInfo;
	private ShareDialog shareDialog;

	private boolean shouldOverrideUrlLoading;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayoutId());

		shareDialog=new ShareDialog();
		title = getIntent().getStringExtra("title");
		loadUrl=getIntent().getStringExtra("loadUrl");
		shouldOverrideUrlLoading=getIntent().getBooleanExtra("shouldOverrideUrlLoading",false);
		pageType=getIntent().getIntExtra("pageType", TYPE_DEFAULT);
		if(pageType==TYPE_H5_GAME){
			if(shareInfo.getType()==ActicityInfo.TYPE_DSD){
				title="剁手党活动";
				loadUrl=loadUrl+"&fromApp=true";
			} else if(shareInfo.getType()==ActicityInfo.TYPE_DZP){
				title="大转盘活动";
//				loadUrl = loadUrl+"&DeviceID="+Config.imei+
//						"&Authorization="+ PreferUtil.getInstance().getLoginToken()+
//						"&type="+ Constants.OPEN_FROM_APP+"&baseUrl="+Config.dataBaseUrl+"&brand="+ Build.BRAND;
			}

		}

		initViews();
		loadH5(loadUrl);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		shareInfo=null;
	}

	public int getLayoutId(){
		return R.layout.win_h5_detail;
	}
	public static void start(Activity curAct, String loadUrl, String title) {
		Intent intent = new Intent(curAct, H5DetailAct.class);
		intent.putExtra("title", title);
		intent.putExtra("loadUrl",loadUrl);
		ViewUtils.startActivity(intent, curAct);
	}

	public static void start(int pageType,Activity curAct, String loadUrl, String title,ShareInfo shareInfo) {
		Intent intent = new Intent(curAct, H5DetailAct.class);
		intent.putExtra("title", title);
		intent.putExtra("loadUrl",loadUrl);
		intent.putExtra("pageType",pageType);
		H5DetailAct.shareInfo=shareInfo;
		ViewUtils.startActivity(intent, curAct);
	}

	public static void start(int pageType,Activity curAct, String loadUrl, String title,ShareInfo shareInfo,boolean shouldOverrideUrlLoading) {
		Intent intent = new Intent(curAct, H5DetailAct.class);
		intent.putExtra("title", title);
		intent.putExtra("loadUrl",loadUrl);
		intent.putExtra("pageType",pageType);
		intent.putExtra("shouldOverrideUrlLoading",shouldOverrideUrlLoading);
		H5DetailAct.shareInfo=shareInfo;
		ViewUtils.startActivity(intent, curAct);
	}

	private void initViews() {
		View btnShare=findViewById(R.id.btn_share);
		btnShare.setOnClickListener(this);
		if (shareInfo==null)
			btnShare.setVisibility(View.GONE);

		findViewById(R.id.btn_back).setOnClickListener(this);
		if(title != null){
			((TextView) findViewById(R.id.txt_title)).setText(title);
		}

		loadingview = (LoadingView) findViewById(R.id.view_loading);
		webView = ((WebView) findViewById(R.id.web_view));
	}

	private void loadH5(String loadUrl) {
		webView.getSettings().setBuiltInZoomControls(false);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setSupportZoom(true);
		webView.getSettings().setUseWideViewPort(true);
	//	webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
		webView.getSettings().setLoadWithOverviewMode(true);
		webView.getSettings().setSaveFormData(false); // 支持保存数据
		webView.clearCache(true); // 清除缓存
		webView.clearHistory(); // 清除历史记录
		webView.getSettings().setAppCacheEnabled(true);//是否使用缓存
		webView.getSettings().setDomStorageEnabled(true);//DOM Storage

		webView.setHorizontalScrollBarEnabled(false);//水平不显示
		webView.setVerticalScrollBarEnabled(false); //垂直不显示

		webView.setWebViewClient(new WebViewClient() {


			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (shouldOverrideUrlLoading) {
					return true;
				}
				if (url.startsWith("tel:")) {
					Intent intent = new Intent(Intent.ACTION_VIEW,
							Uri.parse(url));
					startActivity(intent);
					return true;
				}
				return false;
			}


			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				loadingview.startLoading();
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
			//	ViewUtils.setVisible(webView);
				//loadingview.startLoading();
				loadingview.onLoadingComplete();
			}
		});

		RemoteInvokeService jsObj = new RemoteInvokeService(this, webView, null);

		webView.addJavascriptInterface(jsObj, "js_invoke");
		loadingview.startLoading();
		webView.loadUrl(loadUrl);

	}


	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.btn_back:
				finish();
				break;

			case R.id.btn_share:
				if (shareInfo==null)
					return;
				String url = shareInfo.getUrl();
//				if (shareInfo.getType() == ActicityInfo.TYPE_DZP) {
//					url = url + "&DeviceID=" + Config.imei +
//							"&Authorization=" + PreferUtil.getInstance().getLoginToken() +
//							"&type=" + Constants.OPEN_FROM_SHARE + "&baseUrl=" + Config.dataBaseUrl;
//				}

				shareDialog.show(shareInfo.getTitle(),
						         shareInfo.getDesc(),
						         url,
						         shareInfo.getIconUrl(),
						new ShareListener() {
							@Override
							public void onComplete(Object var1) {
								switch (pageType){

									case TYPE_H5_GAME:
										shareUrl="/makeFans/addShareAmount";
										if(shareSuccessRequest==null)
											shareSuccessRequest=new ShareSuccessRequest();
										shareSuccessRequest.addParams("type",shareInfo.getType());
										shareSuccessRequest.addParams("id",shareInfo.getId());
										shareSuccessRequest.addParams("title",shareInfo.getTitle());
										shareSuccessRequest.addParams("shopId", Account.user.getShopId());
										shareSuccessRequest.start();
										break;

									default:
										break;

								}

							}

							@Override
							public void onFail(int errorCode) {

							}
						}
				);
				break;
		}

	}


	private String shareUrl;

	private ShareSuccessRequest shareSuccessRequest;
	 class ShareSuccessRequest extends BaseRequest{
		@Override
		protected String method() {
			return shareUrl;
		}
	}

}
