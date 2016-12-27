package cn.edu.pku.cyao.miniweather;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.pku.cyao.app.MyApplication;
import cn.edu.pku.cyao.bean.TodayWeather;
import cn.edu.pku.cyao.bean.WeekDayWeather;
import cn.edu.pku.cyao.util.Location;
import cn.edu.pku.cyao.util.NetUtil;
import cn.edu.pku.cyao.util.XmlUtil;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;


/**
 * Created by cyao on 16-9-21.
 * 1.创建Activity类（MainActivity）
 * 2.创建layout布局文件 （weather_info.xml）
 * 3.在AndroidManifest中注册创建的Activity类 （AndroidManifest.xml）
 */

public class MainActivity extends Activity implements View.OnClickListener, ViewPager.OnPageChangeListener{

    private static final int UPDATE_TODAY_WEATHER = 1;
    private static final int GET_LOCATION = 2;

    private static final String TAG = "MiniWeather";

    private String currentCityCode;
    private String currentCityName;
    private String shareInfo;

    private ImageView mUpdateBtn;
    private ProgressBar updateProgress;
    private ImageView mCitySelect;
    private ImageView mLocationBtn;
    private ImageView mShareBtn;

    private TextView cityTv, timeTv, humidityTv, weekTv, pmDataTv,
            pmQualityTv, temperatureTv, climateTv, windTv, city_name_Tv, now_temperature_Tv;
    private ImageView weatherImg, pmImg;

    private Map<String,Integer> weatherImgMap;

    //六日天气使用变量
    private ViewPagerAdapter weatherWeeklyAdapter;
    private ViewPager weatherWeeklyViewPager;
    private List<View> weatherWeeklyViews;

    private ImageView[] dots;
    private int[] ids = {R.id.iv1, R.id.iv2};

    private final int WEEK = 6;

    private TextView[] weeksTv, temperatureWeeksTv, climateWeeksTv, windWeeksTv;
    private ImageView[] weeksImg;

    private MyApplication app;

    private Handler mhandler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UPDATE_TODAY_WEATHER:
                    updateWeather(msg.obj);
                    break;
                case GET_LOCATION:
                    queryWeather();
                    break;
                default:
                    break;
            }
        }
    };

    private IntentFilter filter = new IntentFilter("time_to_update_weather");
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: 自动更新天气");
            queryWeather();
        }
    };




    private void updateWeather(Object p){
        HashMap<String,Object> result = (HashMap<String,Object>)p;
        updateTodayWeather((TodayWeather) result.get("todayWeather"));
        ArrayList<WeekDayWeather> weekDayWeathers = (ArrayList<WeekDayWeather>) result.get("weekDayWeathers");
        updateWeatherWeeklyView(weekDayWeathers);
        final Bitmap shot = myShot(this);
        updateProgress.setVisibility(View.INVISIBLE);
        mUpdateBtn.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    saveToSD(shot,"mnt/sdcard/pictures/","shot.png");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //加载布局
        setContentView(R.layout.weather_info);
        mUpdateBtn = (ImageView) findViewById(R.id.title_update_btn);
        updateProgress = (ProgressBar) findViewById(R.id.title_update_progress);
        mUpdateBtn.setOnClickListener(this);
        mCitySelect = (ImageView) findViewById(R.id.title_city_manager);
        mCitySelect.setOnClickListener(this);
        mLocationBtn = (ImageView) findViewById(R.id.title_location);
        mLocationBtn.setOnClickListener(this);
        Log.d("MyApp","MainActivity->onCreate");
        setWeatherImgMap();
        app = (MyApplication) getApplicationContext();
        mShareBtn = (ImageView) findViewById(R.id.title_share);
        mShareBtn.setOnClickListener(this);
        ShareSDK.initSDK(this);
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("cityCode", currentCityCode);
        editor.putString("cityName", currentCityName);
        editor.commit();
    }

    private void checkCurrentCity(){
        SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
        currentCityCode = sharedPreferences.getString("cityCode",null);
        currentCityName = sharedPreferences.getString("cityName", "北京");
        city_name_Tv.setText(currentCityName+"天气");
        cityTv.setText(currentCityName);
        if(currentCityCode==null){
            //定位
            getCurrentLocation();
        }
        queryWeather();
        startAutoUpdateWeatherService();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.title_city_manager) {
            Intent i = new Intent(this, SelectCity.class);
            i.putExtra("currentCity", currentCityName);
            i.putExtra("currentCode", currentCityCode);
            startActivityForResult(i,1);


        }
        if (view.getId() == R.id.title_update_btn) {
            queryWeather();
        }
        if (view.getId() == R.id.title_location) {
            getCurrentLocation();
        }
        if (view.getId() == R.id.title_share) {
            Log.d(TAG, "onClick: "+shareInfo);
            showShare();
        }
    }


    private void queryWeatherCode(String cityCode) {
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
        Log.d("myWeather", address);
        mUpdateBtn.setVisibility(View.INVISIBLE);
        updateProgress.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
            try {
                String responseStr = XmlUtil.getXml(address);
                Log.d("myWeather", responseStr);
                TodayWeather todayWeather = null;
                ArrayList<WeekDayWeather> weekDayWeathers = null;
                Map<String,Object> result = new HashMap();
                if(!responseStr.isEmpty()){
                    todayWeather = XmlUtil.parseXmlByPull(responseStr);
                    weekDayWeathers = XmlUtil.parseXmlByPullWeeks(responseStr);
                }
                if (todayWeather != null || weekDayWeathers != null) {
                    result.put("todayWeather", todayWeather);
                    result.put("weekDayWeathers", weekDayWeathers);
                    Message msg = new Message();
                    msg.what = UPDATE_TODAY_WEATHER;
                    msg.obj = result;
                    Thread.sleep(2000);// 测试ProgressBar
                    mhandler.sendMessage(msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            }
        }).start();
    }

    /*
    初始化控件内容
     */
    private void initView() {
        city_name_Tv = (TextView) findViewById(R.id.title_city_name);
        cityTv = (TextView) findViewById(R.id.city);
        timeTv = (TextView) findViewById(R.id.time);
        humidityTv = (TextView) findViewById(R.id.humidity);
        weekTv = (TextView) findViewById(R.id.week_today);
        pmDataTv = (TextView) findViewById(R.id.pm_data);
        pmQualityTv = (TextView) findViewById(R.id.pm2_5_quality);
        pmImg = (ImageView) findViewById(R.id.pm2_5_img);
        temperatureTv = (TextView) findViewById(R.id.temperature);
        climateTv = (TextView) findViewById(R.id.climate);
        windTv = (TextView) findViewById(R.id.wind);
        weatherImg = (ImageView) findViewById(R.id.weather_img);
        now_temperature_Tv = (TextView) findViewById(R.id.now_temperature);
        initWeatherWeeklyView();
        initDots();
        checkCurrentCity();
        weatherImg.setImageResource(R.drawable.biz_plugin_weather_qing);
        pmImg.setImageResource(R.drawable.biz_plugin_weather_0_50);
    }

    /*
    更新今日天气
     */
    private void updateTodayWeather(TodayWeather todayWeather) {
        shareInfo = todayWeather.getShareString();
        city_name_Tv.setText(todayWeather.getCity() + "天气");
        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime() + "发布");
        humidityTv.setText("湿度：" + todayWeather.getShidu());
        if(todayWeather.getPm25().equals("999"))
            pmDataTv.setText("无");
        else{
            pmDataTv.setText(todayWeather.getPm25());
        }
        pmQualityTv.setText(todayWeather.getQuality());
        weekTv.setText(todayWeather.getDate());
        temperatureTv.setText(todayWeather.getHigh() + "~" + todayWeather.getLow());
        climateTv.setText(todayWeather.getType());
        windTv.setText(todayWeather.getFengli());
        now_temperature_Tv.setText(todayWeather.getWendu()+"℃");
        int pmImgId;
        int pm25 = Integer.parseInt(todayWeather.getPm25());
        if(pm25<51)
            pmImgId = R.drawable.biz_plugin_weather_0_50;
        else if(pm25<101)
            pmImgId = R.drawable.biz_plugin_weather_51_100;
        else if(pm25<151)
            pmImgId = R.drawable.biz_plugin_weather_101_150;
        else if(pm25<201)
            pmImgId = R.drawable.biz_plugin_weather_151_200;
        else if(pm25<301)
            pmImgId = R.drawable.biz_plugin_weather_201_300;
        else
            pmImgId = R.drawable.biz_plugin_weather_0_50;
        pmImg.setImageResource(pmImgId);
        int typeImgId= R.drawable.biz_plugin_weather_qing;
        if(weatherImgMap.containsKey(todayWeather.getType()))
            typeImgId = weatherImgMap.get(todayWeather.getType());
        weatherImg.setImageResource(typeImgId);
        Toast.makeText(MainActivity.this, "更新成功！", Toast.LENGTH_SHORT).show();
        currentCityName = todayWeather.getCity();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {

            currentCityCode = data.getStringExtra("cityCode");
            queryWeather();
            startAutoUpdateWeatherService();
        }
    }

    private void queryWeather(){
        Log.d("myApp", "选择的城市代码为"+currentCityCode);
        if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
            Log.d("myWeather", "网络OK");
            queryWeatherCode(currentCityCode);
        } else {
            Log.d("myWeather", "网络挂了");
            Toast.makeText(MainActivity.this, "网络挂了！", Toast.LENGTH_LONG).show();
        }
    }

    private void startAutoUpdateWeatherService(){
        Intent i = new Intent(getBaseContext(), UpdateWeatherService.class);
        i.putExtra("update_interval",0);
        startService(i);
        filter.addAction("time_to_update_weather");
        registerReceiver(receiver,filter);
    }

    //修改自动更新天气的间隔，interval==0的话不自动更新天气
    private void changeUpdateInterval(int interval){
        Intent i = new Intent(getBaseContext(), UpdateWeatherService.class);
        stopService(i);
        if (interval > 0) {
            i.putExtra("update_interval",interval);
            startService(i);
            filter.addAction("time_to_update_weather");
            registerReceiver(receiver,filter);
        }
    }

    private void initWeatherWeeklyView(){
        LayoutInflater inflater = LayoutInflater.from(this);
        weatherWeeklyViews = new ArrayList<View>();
        weatherWeeklyViews.add(inflater.inflate(R.layout.weather_info_page_1,null));
        weatherWeeklyViews.add(inflater.inflate(R.layout.weather_info_page_2,null));
        weatherWeeklyAdapter = new ViewPagerAdapter(weatherWeeklyViews, this);
        weatherWeeklyViewPager = (ViewPager) findViewById(R.id.weather_this_week);
        weatherWeeklyViewPager.setAdapter(weatherWeeklyAdapter);
        weatherWeeklyViewPager.setOnPageChangeListener(this);
        weeksTv = new TextView[WEEK];
        temperatureWeeksTv = new TextView[WEEK];
        climateWeeksTv = new TextView[WEEK];
        windWeeksTv = new TextView[WEEK];
        weeksImg = new ImageView[WEEK];
        Resources res = getResources();
        int idxView = 0;
        for(int i=0;i<WEEK;++i) {
            if(i==3){
                idxView = 1;
            }
            int id = res.getIdentifier("day_" + i, "id", getPackageName());
            LinearLayout dayInfo = (LinearLayout) weatherWeeklyViews.get(idxView).findViewById(id);
            weeksTv[i] = (TextView) dayInfo.findViewById(R.id.week_day);
            temperatureWeeksTv[i] = (TextView) dayInfo.findViewById(R.id.week_temperature);
            climateWeeksTv[i] = (TextView) dayInfo.findViewById(R.id.week_climate);
            windWeeksTv[i] = (TextView) dayInfo.findViewById(R.id.week_wind);
            weeksImg[i] = (ImageView) dayInfo.findViewById(R.id.week_img);
        }
    }

    private void updateWeatherWeeklyView(ArrayList<WeekDayWeather> weekDayWeathers){
        Log.d(TAG, "updateWeatherWeeklyView: "+weekDayWeathers);
        for (int i = 0; i < Math.min(6, weekDayWeathers.size()); ++i) {
            Log.d(TAG, "updateWeatherWeeklyView: "+weeksTv[i].getText());
            weeksTv[i].setText(weekDayWeathers.get(i).getWeek());
            temperatureWeeksTv[i].setText(weekDayWeathers.get(i).getHigh() + "~" + weekDayWeathers.get(i).getLow());
            climateWeeksTv[i].setText(weekDayWeathers.get(i).getType());
            windWeeksTv[i].setText(weekDayWeathers.get(i).getWind());
            int typeImgId = R.drawable.biz_plugin_weather_qing;
            if (weatherImgMap.containsKey(weekDayWeathers.get(i).getType()))
                typeImgId = weatherImgMap.get(weekDayWeathers.get(i).getType());
            weeksImg[i].setImageResource(typeImgId);
            Log.d(TAG, "updateWeatherWeeklyView: "+weeksTv[i].getText());
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        for (int a=0;a<ids.length;++a) {
            if (a == position) {
                dots[a].setImageResource(R.drawable.page_indicator_focused);
            } else {
                dots[a].setImageResource(R.drawable.page_indicator_unfocused);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private void initDots(){
        dots = new ImageView[weatherWeeklyViews.size()];
        for (int i=0;i<weatherWeeklyViews.size();++i) {
            dots[i] = (ImageView) findViewById(ids[i]);
        }
    }

    private void setWeatherImgMap(){
        weatherImgMap = new HashMap<String,Integer>();
        weatherImgMap.put("晴",R.drawable.biz_plugin_weather_qing);
        weatherImgMap.put("多云",R.drawable.biz_plugin_weather_duoyun);
        weatherImgMap.put("雾",R.drawable.biz_plugin_weather_wu);
        weatherImgMap.put("阴",R.drawable.biz_plugin_weather_yin);
        weatherImgMap.put("小雨",R.drawable.biz_plugin_weather_xiaoyu);
        weatherImgMap.put("暴雨",R.drawable.biz_plugin_weather_baoyu);
        weatherImgMap.put("暴雪",R.drawable.biz_plugin_weather_baoxue);
        weatherImgMap.put("大暴雨",R.drawable.biz_plugin_weather_dabaoyu);
        weatherImgMap.put("大雪",R.drawable.biz_plugin_weather_daxue);
        weatherImgMap.put("大雨",R.drawable.biz_plugin_weather_dayu);
        weatherImgMap.put("雷阵雨",R.drawable.biz_plugin_weather_leizhenyu);
        weatherImgMap.put("雷阵雨冰雹",R.drawable.biz_plugin_weather_leizhenyubingbao);
        weatherImgMap.put("沙尘暴",R.drawable.biz_plugin_weather_shachenbao);
        weatherImgMap.put("特大暴雨",R.drawable.biz_plugin_weather_tedabaoyu);
        weatherImgMap.put("小雪",R.drawable.biz_plugin_weather_xiaoxue);
        weatherImgMap.put("雨夹雪",R.drawable.biz_plugin_weather_yujiaxue);
        weatherImgMap.put("阵雨",R.drawable.biz_plugin_weather_zhenyu);
        weatherImgMap.put("阵雪",R.drawable.biz_plugin_weather_zhenxue);
        weatherImgMap.put("中雪",R.drawable.biz_plugin_weather_zhongxue);
    }

    private void getCurrentLocation() {
        Toast.makeText(MainActivity.this,"定位中...",Toast.LENGTH_SHORT).show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String city = Location.getCity();
                    if (city.equals("no result")) {
                        Log.d(TAG, "run: 定位失败，未得到结果");
                    }else{
                        String[] nameList = app.getCityNameList();
                        String[] codeList = app.getCityCodeList();
                        int idx = 0;
                        for (;idx<nameList.length;++idx) {
                            if(nameList[idx].equals(city)) break;
                        }
                        currentCityName = nameList[idx];
                        currentCityCode = codeList[idx];
                        Log.d(TAG, "run: "+currentCityName+" "+currentCityCode);
                        Message msg = new Message();
                        msg.what = GET_LOCATION;
                        mhandler.sendMessage(msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void showShare() {
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();
        // title标题，印象笔记、邮箱、信息、微信、人人网、QQ和QQ空间使用
        oks.setTitle("标题");
        // titleUrl是标题的网络链接，仅在Linked-in,QQ和QQ空间使用
        oks.setTitleUrl("http://sharesdk.cn");
        // text是分享文本，所有平台都需要这个字段
        oks.setText(shareInfo);
        oks.setImagePath("/mnt/sdcard/pictures/shot.png");
        //分享网络图片，新浪微博分享网络图片需要通过审核后申请高级写入接口，否则请注释掉测试新浪微博
//        oks.setImageUrl("http://f1.sharesdk.cn/imgs/2014/02/26/owWpLZo_638x960.jpg");
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        //oks.setImagePath("/sdcard/test.jpg");//确保SDcard下面存在此张图片
        // url仅在微信（包括好友和朋友圈）中使用
//        oks.setImageUrl(shareImg);
//        oks.setUrl("http://sharesdk.cn");
        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
        oks.setComment("我是测试评论文本");
        // site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite("ShareSDK");
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl("http://sharesdk.cn");

        // 启动分享GUI
        oks.show(this);
    }

    private Bitmap myShot(Activity activity) {
        // 获取windows中最顶层的view
        View view = activity.getWindow().getDecorView();
        view.buildDrawingCache();
        // 获取状态栏高度
        Rect rect = new Rect();
        view.getWindowVisibleDisplayFrame(rect);
        int statusBarHeights = rect.top;
        Display display = activity.getWindowManager().getDefaultDisplay();
        // 获取屏幕宽和高
        int widths = display.getWidth();
        int heights = display.getHeight();
        // 允许当前窗口保存缓存信息
        view.setDrawingCacheEnabled(true);
        // 去掉状态栏
        Bitmap bmp = Bitmap.createBitmap(view.getDrawingCache(), 0,
                statusBarHeights, widths, heights - statusBarHeights);
        // 销毁缓存信息
        view.destroyDrawingCache();
        return bmp;
    }

    private String bitmapToBase64(Bitmap bitmap){
        String result="";
        ByteArrayOutputStream bos=null;
        try {
            if(null!=bitmap){
                bos=new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);//将bitmap放入字节数组流中

                bos.flush();//将bos流缓存在内存中的数据全部输出，清空缓存
                bos.close();

                byte []bitmapByte=bos.toByteArray();
                result= Base64.encodeToString(bitmapByte, Base64.DEFAULT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            if(null!=null){
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    private void saveToSD(Bitmap bmp, String dirName,String fileName) throws IOException {
        // 判断sd卡是否存在
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            File dir = new File(dirName);
            // 判断文件夹是否存在，不存在则创建
            if(!dir.exists()){
                dir.mkdir();
            }

            File file = new File(dirName + fileName);
            // 判断文件是否存在，不存在则创建
            if (!file.exists()) {
                file.createNewFile();
            }

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
                if (fos != null) {
                    // 第一参数是图片格式，第二个是图片质量，第三个是输出流
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    // 用完关闭
                    fos.flush();
                    fos.close();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
