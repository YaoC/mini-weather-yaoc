package cn.edu.pku.cyao.miniweather;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import com.alibaba.fastjson.JSON;

/**
 * Created by cyao on 16-11-13.
 */

public class JsonTest {
    @Test
    public void testFastJson(){
        String responseStr = null;
        try {
            URL url = new URL("http://www.weather.com.cn/data/sk/101010100.html");
            URLConnection connection = url.openConnection();
            HttpURLConnection urlConnection = (HttpURLConnection) connection;
            urlConnection.setRequestProperty("Accept-Charset", "UTF-8");
            urlConnection.setRequestProperty("contentType", "UTF-8");
            urlConnection.connect();

            if (200 == urlConnection.getResponseCode()) {
                InputStream responseStream = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream));
                StringBuilder response = new StringBuilder();
                String str;
                while ((str = reader.readLine()) != null) {
                    response.append(str);
                }
                responseStr = response.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        com.alibaba.fastjson.JSONObject info = JSON.parseObject(responseStr);
        com.alibaba.fastjson.JSONObject weatherInfo = JSON.parseObject(info.getString("weatherinfo"));
        System.out.println(weatherInfo.get("temp"));
    }
}
