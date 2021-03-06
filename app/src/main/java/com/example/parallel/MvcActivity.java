package com.example.parallel;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.parallel.api.ApiUtil;
import com.example.parallel.api.Url;
import com.example.parallel.bean.PublicBean;

import intlapp.dragonpass.com.mvpmodel.base.ObjectObserver;
import intlapp.dragonpass.com.mvpmodel.base.ObservableBuilder;
import intlapp.dragonpass.com.mvpmodel.callback.Action;
import intlapp.dragonpass.com.mvpmodel.callback.EqualsCallback;
import intlapp.dragonpass.com.mvpmodel.callback.GetCacheCallback;
import intlapp.dragonpass.com.mvpmodel.callback.HandleCallback;
import intlapp.dragonpass.com.mvpmodel.callback.HandleResult;
import intlapp.dragonpass.com.mvpmodel.callback.PutCacheCallback;
import intlapp.dragonpass.com.mvpmodel.callback.TimeCallback;
import intlapp.dragonpass.com.mvpmodel.entity.ParaseData;
import intlapp.dragonpass.com.mvpmodel.utils.MyLog;
import io.reactivex.ObservableTransformer;

public class MvcActivity extends AppCompatActivity {
    private static final String TAG = MvcActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mvc);
    }

    public void request(View v) {
        ObservableBuilder.
                <PublicBean>newObservableBuilder(ApiUtil.getApiService().request(Url.url))
                .delay(1000)//延时请求
                .retryWhen(3)//重试次数
                .time(new TimeCallback() {//重试延迟的时间,默认次数*1000
                    @Override
                    public long timeBack(int currCount) {
                        return currCount*1000;
                    }
                })
                .getCache(new GetCacheCallback<PublicBean>() {
                    @Override
                    public ParaseData<PublicBean> returnCache() {
                        MyLog.rtLog(TAG, "取缓存");
                        ParaseData<PublicBean> data = new ParaseData<>();
                        data.data = getCacheData();
                        return data;
                    }
                })
                .putCache(new PutCacheCallback() {
                    @Override
                    public void putCache(ParaseData data) {
                        MyLog.rtLog(TAG, "存缓存:" + data);
                    }
                })
                .equalsCallback(new EqualsCallback<PublicBean>() {//如果使用了缓存,那么对比缓存和网络数据,默认对比result字符串
                    @Override
                    public boolean equalsData(ParaseData<PublicBean> data) {
                        return true;
                    }
                })
                .action(new Action<PublicBean>() {
                    @Override
                    public ParaseData<PublicBean> action(ParaseData<PublicBean> data) {
                        if(!data.cache) {
                            //修改网络数据,纯粹为了查看log
                            PublicBean bean = new PublicBean();
                            bean.setErrorMsg("网络数据");
                            data.data = bean;
                            data.result = "网络数据";
                        }
                        return data;
                    }
                })
                .submit(new ObjectObserver<PublicBean>(this,
                        new ObjectObserver.Builder()
                                .setShowLoading(false)//是否显示加载框
                                .setMvpView(new HandleCallback(this) {
                                    @Override
                                    public void showLoading() {
                                        super.showLoading();
                                    }

                                    @Override
                                    public void hindeLoading() {
                                        super.hindeLoading();
                                    }
                                })//默认使用HandleCallback,统一的加载可以写在里面,也可以对应请求重写
                ) {

                    @Override
                    public void onSuccess(PublicBean data) {
                        MyLog.rtLog(TAG, "获取数据:" + data + "\n是否缓存:" + getCurrParaseData().cache);
                    }

                    @Override
                    public boolean isPutCache(ParaseData<PublicBean> data) {
                        //这里可以做一些处理判断是否存缓存
                        return super.isPutCache(data);
                    }

                    @Override
                    protected ObservableTransformer<ParaseData<PublicBean>, ParaseData<PublicBean>> putDataThreadCompose() {
                        //修改存缓存执行的线程
                        return super.putDataThreadCompose();
                    }
                });
    }

    private PublicBean getCacheData() {
        PublicBean publicBean = new PublicBean();
        publicBean.setErrorMsg("缓存数据");
        return publicBean;
    }

}
