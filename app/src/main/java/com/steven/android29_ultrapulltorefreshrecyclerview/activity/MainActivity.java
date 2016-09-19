package com.steven.android29_ultrapulltorefreshrecyclerview.activity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import com.steven.android29_ultrapulltorefreshrecyclerview.R;
import com.steven.android29_ultrapulltorefreshrecyclerview.adapter.QiushiAdapter;
import com.steven.android29_ultrapulltorefreshrecyclerview.decoration.DividerItemDecoration;
import com.steven.android29_ultrapulltorefreshrecyclerview.helper.OkHttpClientHelper;
import com.steven.android29_ultrapulltorefreshrecyclerview.model.QiushiModel;
import com.steven.android29_ultrapulltorefreshrecyclerview.utils.Constant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import in.srain.cube.views.ptr.PtrClassicDefaultHeader;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.header.StoreHouseHeader;

public class MainActivity extends AppCompatActivity {

    private Context mContext = this;
    private PtrFrameLayout ptrFrameLayout_main;
    private RecyclerView recyclerView_main;
    private QiushiAdapter adapter = null;
    private List<QiushiModel.ItemsEntity> totalList = new ArrayList<>();
    private ProgressBar progressBar_main;
    private int curPage = 1;

    private int lastVisibleItem = 0;
    private LinearLayoutManager linearLayoutManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        loadNetworkData();
    }

    private void loadNetworkData() {
        OkHttpClientHelper.getDataAsync(mContext, String.format(Constant.URL_LATEST, curPage),
                new Callback() {

                    @Override
                    public void onFailure(Request request, IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, "No data!!!", Toast
                                        .LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        if (response.isSuccessful()) {
                            ResponseBody body = response.body();
                            if (body != null) {
                                String jsonString = body.string();

                                //json解析
                                QiushiModel result_model = parseJsonToQiushiModel(jsonString);
                                final List<QiushiModel.ItemsEntity> list = result_model.getItems();

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (progressBar_main.isShown()) {
                                            progressBar_main.setVisibility(View.GONE);
                                        }
                                        if (curPage == 1) {
                                            adapter.reloadListView(list, true);
                                        } else {
                                            adapter.reloadListView(list, false);
                                        }
                                        // 刷新完成，让刷新Loading消失
                                        ptrFrameLayout_main.refreshComplete();
                                    }
                                });
                            }
                        }
                    }
                }, "qiushi_latest");
    }

    private void initView() {
        progressBar_main = (ProgressBar) findViewById(R.id.progressBar_main);
        ptrFrameLayout_main = (PtrFrameLayout) findViewById(R.id.ptrFrameLayout_main);

        recyclerView_main = (RecyclerView) findViewById(R.id.recyclerView_main);

        // 如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
        recyclerView_main.setHasFixedSize(true);

        // 设置一个垂直方向的layout manager
        linearLayoutManager = new LinearLayoutManager(
                mContext, LinearLayoutManager.VERTICAL, false);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, 2);
        // 设置布局管理器
        //recyclerView_main.setLayoutManager(gridLayoutManager);
        recyclerView_main.setLayoutManager(linearLayoutManager);

        //设置分割线或者分割空间
        recyclerView_main.addItemDecoration(new DividerItemDecoration(mContext,
                DividerItemDecoration.VERTICAL_LIST));

        adapter = new QiushiAdapter(mContext, totalList);
        recyclerView_main.setAdapter(adapter);
        recyclerView_main.setItemAnimator(new DefaultItemAnimator());

        //利用RecyclerView的滚动监听实现上拉加载下一页
        recyclerView_main.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView,
                                             int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE
                        && lastVisibleItem == adapter.getItemCount() - 1) {
                    curPage++;
                    loadNetworkData();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
            }

        });

        //使用PtrFrameLayout实现下拉刷新
        //效果1：设置默认的经典的头视图
        PtrClassicDefaultHeader defaultHeader = new PtrClassicDefaultHeader(mContext);


        //效果2：特殊效果，目前只支持英文字符（闪动的文字Header：闪动文字效果的header）
        StoreHouseHeader storeHouseHeader = new StoreHouseHeader(this);
        //storeHouseHeader.setPadding(0, 30, 0, 0);
        storeHouseHeader.setBackgroundColor(Color.BLACK);
        storeHouseHeader.setTextColor(Color.WHITE);
        // 文字只能是0-9,a-z不支持中文
        storeHouseHeader.initWithString("loading...");

        //设置头视图
        ptrFrameLayout_main.setHeaderView(storeHouseHeader);
        // 绑定UI与刷新状态的监听
        ptrFrameLayout_main.addPtrUIHandler(storeHouseHeader);

        // 添加刷新动作监听
        ptrFrameLayout_main.setPtrHandler(new PtrDefaultHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                curPage = 1;
                loadNetworkData();
            }
        });



    }

    //gson解析
    private QiushiModel parseJsonToQiushiModel(String jsonString) {
        Gson gson = new Gson();
        QiushiModel model = gson.fromJson(jsonString, new TypeToken<QiushiModel>() {
        }.getType());
        return model;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (totalList != null) {
            totalList = null;
        }
        OkHttpClientHelper.cancelCall("qiushi_latest");
    }

}
