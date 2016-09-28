package cn.edu.pku.cyao.miniweather;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;


import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;

import cn.edu.pku.cyao.util.NetUtil;

/**
 * Created by cyao on 16-9-21.
 * 1.创建Activity类（MainActivity）
 * 2.创建layout布局文件 （weather_info.xml）
 * 3.在AndroidManifest中注册创建的Activity类 （AndroidManifest.xml）
 */

public class MainActivity extends Activity implements View.OnClickListener{


    private ImageView mUpdateBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //加载布局
        setContentView(R.layout.weather_info);
        mUpdateBtn = (ImageView) findViewById(R.id.title_update_btn);
        mUpdateBtn.setOnClickListener(this);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu){
//        getMenuInflater().inflate(R.menu.menu_main,menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item){
//        int id = item.getItemId();
//        if(id==R.id.action_settings){
//            return true;
//        }
//    }

    @Override
    public void onClick(View view){
        if(view.getId()==R.id.title_update_btn){
            SharedPreferences sharedPreferences = getSharedPreferences("config",MODE_PRIVATE);
            String cityCode = sharedPreferences.getString("main_city_code","101010100");
            Log.d("myWeather",cityCode);
            if(NetUtil.getNetworkState(this)!=NetUtil.NETWORN_NONE){
                Log.d("myWeather","网络OK");
                queryWeatherCode(cityCode);
            }else{
                Log.d("myWeather","网络挂了");
                Toast.makeText(MainActivity.this,"网络挂了！",Toast.LENGTH_LONG).show();
            }

        }
    }


    private void queryWeatherCode(String cityCode){
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
        Log.d("myWeather",address   );
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(address);
                    URLConnection connection = url.openConnection();
                    HttpURLConnection urlConnection = (HttpURLConnection) connection;
                    urlConnection.connect();
                    if(200==urlConnection.getResponseCode()){
                        InputStream responseStream = urlConnection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream));
                        StringBuilder response = new StringBuilder();
                        String str;
                        while ((str=reader.readLine())!=null){
                            response.append(str);
                        }
                        String responseStr = response.toString();
                        Log.d("myWeather",responseStr);
                        parseXml(responseStr);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /*
     *解析XML
     */
    private void parseXml(String xmldata){
        try {
            int fengxiangCount = 0;
            int fengliCount = 0;
            int dateCount = 0;
            int highCount = 0;
            int lowCount = 0;
            int typeCount = 0;

            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldata));
            int evetType = xmlPullParser.getEventType();
            Log.d("myWeather","parseXML");
            while(evetType!=xmlPullParser.END_DOCUMENT){
                switch (evetType){
                    case XmlPullParser.START_TAG:
                        if(xmlPullParser.getName().equals("city")){
                            evetType = xmlPullParser.next();
                            Log.d("myWeather","city:  "+xmlPullParser.getText());
                        }else if(xmlPullParser.getName().equals("updatetime")){
                            evetType = xmlPullParser.next();
                            Log.d("myWeather","updatetime:  "+xmlPullParser.getText());
                        }else if(xmlPullParser.getName().equals("shidu")){
                            evetType = xmlPullParser.next();
                            Log.d("myWeather","湿度:  "+xmlPullParser.getText());
                        }else if(xmlPullParser.getName().equals("wendu")){
                            evetType = xmlPullParser.next();
                            Log.d("myWeather","温度:  "+xmlPullParser.getText());
                        }else if(xmlPullParser.getName().equals("pm25")){
                            evetType = xmlPullParser.next();
                            Log.d("myWeather","pm2.5:  "+xmlPullParser.getText());
                        }else if(xmlPullParser.getName().equals("quality")){
                            evetType = xmlPullParser.next();
                            Log.d("myWeather","空气质量:  "+xmlPullParser.getText());
                        }else if(xmlPullParser.getName().equals("fengxiang")&&fengxiangCount==0){
                            evetType = xmlPullParser.next();
                            Log.d("myWeather","风向:  "+xmlPullParser.getText());
                            fengxiangCount++;
                        }else if(xmlPullParser.getName().equals("fengli")&&fengliCount==0){
                            evetType = xmlPullParser.next();
                            Log.d("myWeather","风力:  "+xmlPullParser.getText());
                            fengliCount++;
                        }else if(xmlPullParser.getName().equals("date")&&dateCount==0){
                            evetType = xmlPullParser.next();
                            Log.d("myWeather","date:  "+xmlPullParser.getText());
                            dateCount++;
                        }else if(xmlPullParser.getName().equals("high")&&highCount==0){
                            evetType = xmlPullParser.next();
                            Log.d("myWeather","high:  "+xmlPullParser.getText().substring(2));
                            highCount++;
                        }else if(xmlPullParser.getName().equals("low")&&lowCount==0){
                            evetType = xmlPullParser.next();
                            Log.d("myWeather","low:  "+xmlPullParser.getText().substring(2));
                            lowCount++;
                        }else if(xmlPullParser.getName().equals("type")&&typeCount==0){
                            evetType = xmlPullParser.next();
                            Log.d("myWeather","type:  "+xmlPullParser.getText());
                            typeCount++;
                        }
                        break;
                    default:
                        break;
                }
                evetType = xmlPullParser.next();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
