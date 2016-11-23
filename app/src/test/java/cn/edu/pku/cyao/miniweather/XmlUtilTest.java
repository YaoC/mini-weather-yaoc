package cn.edu.pku.cyao.miniweather;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;

import cn.edu.pku.cyao.util.XmlUtil;

/**
 * Created by cyao on 16-11-13.
 */

public class XmlUtilTest {
    @Test
    public void xmlUtilTest() throws Exception {
        String xml = getXml();
        if (!xml.isEmpty()) {
            System.out.println("Pull: " + XmlUtil.parseXmlByPull(xml));
            System.out.println("Sax: " + XmlUtil.parseXmlBySax(xml));
        }
    }




    private String getXml(){
        String responseStr = null;
        try {
            URL url = new URL("http://wthrcdn.etouch.cn/WeatherApi?citykey=101010100");
            URLConnection connection = url.openConnection();
            HttpURLConnection urlConnection = (HttpURLConnection) connection;
            urlConnection.setRequestProperty("Accept-Charset", "UTF-8");
            urlConnection.setRequestProperty("contentType", "UTF-8");
            urlConnection.connect();

            if (200 == urlConnection.getResponseCode()) {
                InputStream responseStream = urlConnection.getInputStream();
                responseStream = new GZIPInputStream(responseStream);
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
        return responseStr;
    }
}
