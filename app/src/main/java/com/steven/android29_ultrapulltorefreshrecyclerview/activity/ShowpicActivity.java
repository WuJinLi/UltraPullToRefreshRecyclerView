package com.steven.android29_ultrapulltorefreshrecyclerview.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.steven.android29_ultrapulltorefreshrecyclerview.R;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class ShowpicActivity extends AppCompatActivity {
    private Context mContext = this;
    private String mediumUrl = "";
    private PhotoView photoView;
    private PhotoViewAttacher attacher = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showpic);

        initView();

        loadImage();
    }

    private void initView() {
        photoView = (PhotoView) findViewById(R.id.photoView_show);
        attacher = new PhotoViewAttacher(photoView);
    }

    private void loadImage() {
        //接收Intent过来的url地址（该地址是小图所在的地址），将其更改为大图所在的url地址
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String imageUrl = bundle.getString("url");
            mediumUrl = imageUrl.replace("small", "medium");
        }

        Picasso.with(mContext)
                .load(mediumUrl)
                .into(photoView, new Callback() {
                    @Override
                    public void onSuccess() {
                        //要实现图片缩放，需要执行以下代码
                        attacher.update();
                    }

                    @Override
                    public void onError() {
                    }
                });
    }
}
