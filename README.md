# ParallelApp
## 封装rxjava+retrofit 网络缓存并行,延时请求,错误重试机制都有
项目的MultipleMVPActivity,SingMVPActivity是绑定式的mvp,一个activity可以绑定多个P和V,这样可以模块化P
这框架是为了改公司项目成MVP写的,后面一些原因放弃了,所以可能mvp那部分不太理想,得空改改
但是很适合mvc用,哈哈哈哈
### mvc使用
<pre><code>ObservableBuilder.
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
               });</code></pre>
                -----------------------输出---------------------------
                取缓存
                获取数据:PublicBean{errorCode=0, errorMsg='缓存数据', data=null} 是否缓存:true
                获取数据:PublicBean{errorCode=0, errorMsg='网络数据', data=null} 是否缓存:false
                存缓存:ParaseData{data=PublicBean{errorCode=0, errorMsg='网络数据', data=null}, result='网络数据', cache=false, mThrowable=null}
### mvp不好说明使用请看代码

### 缓存比网络快/网络比缓存快/zip/flatMap 请执行testactivity查看log

### 绑定式MVP
![绑定式MVP](/TIM20190510222301.png )

                
