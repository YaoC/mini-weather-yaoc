package cn.edu.pku.cyao.miniweather;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import cn.edu.pku.cyao.bean.TodayWeather;
import cn.edu.pku.cyao.util.NetUtil;
import cn.edu.pku.cyao.util.XmlUtil;

/**
 * Created by cyao on 16-9-21.
 * 1.创建Activity类（MainActivity）
 * 2.创建layout布局文件 （weather_info.xml）
 * 3.在AndroidManifest中注册创建的Activity类 （AndroidManifest.xml）
 */

public class MainActivity extends Activity implements View.OnClickListener {

    public static final int UPDATE_TODAY_WEATHER = 1;

    private String currentCityCode;
    private String currentCityName;

    private ImageView mUpdateBtn;
    private ProgressBar updateProgress;
    private ImageView mCitySelect;

    private TextView cityTv, timeTv, humidityTv, weekTv, pmDataTv,
            pmQualityTv, temperatureTv, climateTv, windTv, city_name_Tv, now_temperature_Tv;
    private ImageView weatherImg, pmImg;

    private Handler mhandler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather) msg.obj);
                    updateProgress.setVisibility(View.INVISIBLE);
                    mUpdateBtn.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }
    };

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

        Log.d("MyApp","MainActivity->onCreate");

        initView();
    }

    private void checkCurrentCity(){
        if(currentCityCode==null){
            //GPS获取
            //如果GPS未获取到地理位置则显示北京
            currentCityCode = "101010100";
            currentCityName = "北京";
            queryWeather();
            //TODO 设置每小时自动更新天气


        }
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
//            SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
//            String cityCode = sharedPreferences.getString("main_city_code", "101010100");

            queryWeather();
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
                if(!responseStr.isEmpty())
                    todayWeather = XmlUtil.parseXmlByPull(responseStr);
                if (todayWeather != null) {
                    Log.d("myMiniWeather", todayWeather.toString());
                    Message msg = new Message();
                    msg.what = UPDATE_TODAY_WEATHER;
                    msg.obj = todayWeather;
                    //              测试ProgressBar
                    Thread.sleep(2000);
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

        checkCurrentCity();
//        city_name_Tv.setText("N/A");
//        cityTv.setText("N/A");
//        timeTv.setText("N/A");
//        humidityTv.setText("N/A");
//        pmDataTv.setText("N/A");
//        pmQualityTv.setText("N/A");
//        weekTv.setText("N/A");
//        temperatureTv.setText("N/A");
//        climateTv.setText("N/A");
//        windTv.setText("N/A");
//        now_temperature_Tv.setText("N/A");
        weatherImg.setImageResource(R.drawable.biz_plugin_weather_qing);
        pmImg.setImageResource(R.drawable.biz_plugin_weather_0_50);
    }

    /*
    更新今日天气
     */
    private void updateTodayWeather(TodayWeather todayWeather) {
        city_name_Tv.setText(todayWeather.getCity() + "天气");
        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime() + "发布");
        humidityTv.setText("湿度：" + todayWeather.getShidu());
        pmDataTv.setText(todayWeather.getPm25());
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
            pmImgId = R.drawable.biz_plugin_weather_greater_300;
        pmImg.setImageResource(pmImgId);
        int typeImgId= R.drawable.biz_plugin_weather_qing;
        switch (todayWeather.getType()){
            case "晴":
                typeImgId = R.drawable.biz_plugin_weather_qing;break;
            case "多云":
                typeImgId = R.drawable.biz_plugin_weather_duoyun;break;
            case "雾":
                typeImgId = R.drawable.biz_plugin_weather_wu;break;
            case "阴":
                typeImgId = R.drawable.biz_plugin_weather_yin;break;
            case "小雨":
                typeImgId = R.drawable.biz_plugin_weather_xiaoyu;break;
            case "暴雨":
                typeImgId = R.drawable.biz_plugin_weather_baoyu;break;
            case "暴雪":
                typeImgId = R.drawable.biz_plugin_weather_baoxue;break;
            case "大暴雨":
                typeImgId = R.drawable.biz_plugin_weather_dabaoyu;break;
            case "大雪":
                typeImgId = R.drawable.biz_plugin_weather_daxue;break;
            case "大雨":
                typeImgId = R.drawable.biz_plugin_weather_dayu;break;
            case "雷阵雨":
                typeImgId = R.drawable.biz_plugin_weather_leizhenyu;break;
            case "雷阵雨冰雹":
                typeImgId = R.drawable.biz_plugin_weather_leizhenyubingbao;break;
            case "沙尘暴":
                typeImgId = R.drawable.biz_plugin_weather_shachenbao;break;
            case "特大暴雨":
                typeImgId = R.drawable.biz_plugin_weather_tedabaoyu;break;
            case "小雪":
                typeImgId = R.drawable.biz_plugin_weather_xiaoxue;break;
            case "雨夹雪":
                typeImgId = R.drawable.biz_plugin_weather_yujiaxue;break;
            case "阵雨":
                typeImgId = R.drawable.biz_plugin_weather_zhenyu;break;
            case "阵雪":
                typeImgId = R.drawable.biz_plugin_weather_zhenxue;break;
            case "中雪":
                typeImgId = R.drawable.biz_plugin_weather_zhongxue;break;
            case "中雨":
                typeImgId = R.drawable.biz_plugin_weather_zhongyu;break;
            default:
                break;
        }
        weatherImg.setImageResource(typeImgId);
        Toast.makeText(MainActivity.this, "更新成功！", Toast.LENGTH_SHORT).show();
        currentCityName = todayWeather.getCity();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {

            currentCityCode = data.getStringExtra("cityCode");
            queryWeather();

            //TODO 重新设置每小时自动更新天气
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
        i.putExtra("cityCode", currentCityCode);
        startService(i);
    }

    private void changeAutoUpdateWeatherService(){

    }

}
