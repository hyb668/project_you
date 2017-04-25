package com.zxly.o2o.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.zxly.o2o.adapter.IMSelectArticleAdapter;
import com.zxly.o2o.pullrefresh.PullToRefreshBase;
import com.zxly.o2o.pullrefresh.PullToRefreshListView;
import com.zxly.o2o.request.BaseRequest;
import com.zxly.o2o.request.PromotionArticleListRequest;
import com.zxly.o2o.shop.R;
import com.zxly.o2o.util.StringUtil;
import com.zxly.o2o.util.ViewUtils;
import com.zxly.o2o.view.LoadingView;

/**
 * Created by dsnx on 2016/1/21.
 */
public class SearchActicleAct extends BasicAct implements PullToRefreshBase.OnRefreshListener {

    private PullToRefreshListView articleListView;
    private IMSelectArticleAdapter searchArticleAdapter;
    private int pageIndex = 1;
    private String searchkey;
    private boolean isLastData;
    private TextView btnSearch;
    private EditText txtSearchContent;
    private LoadingView loadingview;
    private View btnDel, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.win_search);
        articleListView = (PullToRefreshListView) findViewById(R.id.listview);
        btnSearch = (TextView) findViewById(R.id.btn_search);
        txtSearchContent = (EditText) findViewById(R.id.txt_search_content);
        btnDel = findViewById(R.id.btn_del);
        btnBack = findViewById(R.id.btn_back);
        loadingview = (LoadingView) findViewById(R.id.view_loading);
        ViewUtils.setRefreshText(articleListView);
        searchArticleAdapter = new IMSelectArticleAdapter(this);
        articleListView.setAdapter(searchArticleAdapter);
        articleListView.setOnRefreshListener(this);
        articleListView.setMode(PullToRefreshBase.Mode.PULL_FROM_END);
        articleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Gson gson = new Gson();
                String result = gson.toJson(searchArticleAdapter.getItem(position-1));

                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                // intent.putArrayListExtra("dataList", dataList);
                bundle.putString("object", result);
                intent.putExtras(bundle);
                setResult(IMMultiPushActivity.SEARCH_ACTICLE_CODE,intent);
                finish();
            }
        });
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchkey = txtSearchContent.getText().toString();
                if (!StringUtil.isNull(searchkey)) {
                    pageIndex = 1;
                    loadData(pageIndex, searchkey);
                    keyBoardCancle();
                } else {
                    ViewUtils.showToast("请输入关键词!");
                }
            }
        });

        btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtSearchContent.setText("");
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        showKeyboard();

    }


    private void loadData(final int _pageIndex, String searchkey) {
        if (StringUtil.isNull(searchkey)) {
            return;
        }
        final PromotionArticleListRequest reqeust = new PromotionArticleListRequest(searchkey, _pageIndex);
        reqeust.setOnResponseStateListener(new BaseRequest.ResponseStateListener() {
            @Override
            public void onOK() {
                boolean isEmpty = true;
                if (reqeust.getArticles() != null && !reqeust.getArticles().isEmpty()) {
                    isEmpty = false;
                }
                articleListView.onRefreshComplete();
                if (isEmpty) {
                    if (_pageIndex == 1) {
                        ViewUtils.setGone(articleListView);
                        loadingview.onDataEmpty("无结果",R.drawable.kb_icon_d);
                    } else {
                        isLastData = true;
                        ViewUtils.showToast("亲! 没有更多了");
                    }

                } else {
                    loadingview.onLoadingComplete();
                    if (_pageIndex == 1) {
                        searchArticleAdapter.clear();
                    }
                    searchArticleAdapter.addItem(reqeust.getArticles());
                    searchArticleAdapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onFail(int code) {
                articleListView.onRefreshComplete();
                loadingview.onDataEmpty("加载失败");
            }
        });
        if (pageIndex == 1) {
            loadingview.startLoading();
        }

        reqeust.start(this);
    }

    public static void start(Activity curAct) {
        Intent intent = new Intent(curAct, SearchActicleAct.class);
        ViewUtils.startActivityForResult(intent, curAct, IMMultiPushActivity.SEARCH_ACTICLE_CODE);
    }

    @Override
    public void onRefresh(PullToRefreshBase refreshView) {
        {
            if (refreshView.getCurrentMode() == PullToRefreshBase.Mode.PULL_FROM_START) {
                pageIndex = 1;

                loadData(pageIndex, searchkey);

            } else if (refreshView.getCurrentMode() == PullToRefreshBase.Mode.PULL_FROM_END) {
                // 加载上拉数据
                if (!isLastData) {
                    pageIndex++;
                    loadData(pageIndex, searchkey);
                } else {
                    mMainHandler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            articleListView.onRefreshComplete();
                        }
                    }, 1000);
                }

            }

        }
    }
}
