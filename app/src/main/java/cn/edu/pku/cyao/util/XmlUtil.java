package cn.edu.pku.cyao.util;




import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import cn.edu.pku.cyao.bean.TodayWeather;
import cn.edu.pku.cyao.bean.WeekDayWeather;

/**
 * Created by cyao on 16-11-13.
 */

public class XmlUtil {

    public static String getXml(String address) throws Exception{
        URL url = new URL(address);
        URLConnection connection = url.openConnection();
        HttpURLConnection urlConnection = (HttpURLConnection) connection;
        urlConnection.connect();
        String responseStr = null;
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
        return responseStr;
    }

    public static ArrayList<WeekDayWeather> parseXmlByPullWeeks(String xmldata){
        List<WeekDayWeather> weekDayWeathers = new ArrayList<>();
        WeekDayWeather weekDayWeather = null;
        try{
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldata));
            int evetType = xmlPullParser.getEventType();
            while (evetType != xmlPullParser.END_DOCUMENT) {
                switch (evetType) {
                    case XmlPullParser.START_TAG:
                        if (xmlPullParser.getName().equals("weather")) {
                            weekDayWeather = new WeekDayWeather();
                        }
                        if (weekDayWeather != null) {
                            if (xmlPullParser.getName().equals("date")) {
                                evetType = xmlPullParser.next();
                                weekDayWeather.setWeek(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("high")) {
                                evetType = xmlPullParser.next();
                                weekDayWeather.setHigh(xmlPullParser.getText().substring(3));
                            } else if (xmlPullParser.getName().equals("low")) {
                                evetType = xmlPullParser.next();
                                weekDayWeather.setLow(xmlPullParser.getText().substring(3));
                            } else if (xmlPullParser.getName().equals("type")) {
                                evetType = xmlPullParser.next();
                                weekDayWeather.setType(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("fengli")) {
                                evetType = xmlPullParser.next();
                                String fengli = xmlPullParser.getText();
                                if (fengli.equals("微风级")) {
                                    fengli = "微风";
                                }
                                weekDayWeather.setWind(fengli);
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (xmlPullParser.getName().equals("weather")&&weekDayWeather!=null){
                            Log.d("test", "parseXmlByPullWeeks: " + weekDayWeather);
                            weekDayWeathers.add(weekDayWeather);
                        }
                        break;
                    default:
                        break;
                }
                evetType = xmlPullParser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (ArrayList<WeekDayWeather>) weekDayWeathers;
    }

    public static TodayWeather parseXmlByPull(String xmldata) {
        TodayWeather todayWeather = null;
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
            while (evetType != xmlPullParser.END_DOCUMENT) {
                switch (evetType) {
                    case XmlPullParser.START_TAG:
                        if (xmlPullParser.getName().equals("resp")) {
                            todayWeather = new TodayWeather();
                        }
                        if (todayWeather != null) {
                            if (xmlPullParser.getName().equals("city")) {
                                evetType = xmlPullParser.next();
                                todayWeather.setCity(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("updatetime")) {
                                evetType = xmlPullParser.next();
                                todayWeather.setUpdatetime(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("shidu")) {
                                evetType = xmlPullParser.next();
                                todayWeather.setShidu(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("wendu")) {
                                evetType = xmlPullParser.next();
                                todayWeather.setWendu(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("pm25")) {
                                evetType = xmlPullParser.next();
                                todayWeather.setPm25(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("quality")) {
                                evetType = xmlPullParser.next();
                                todayWeather.setQuality(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("fengxiang")
                                    && fengxiangCount == 0) {
                                evetType = xmlPullParser.next();
                                todayWeather.setFengxiang(xmlPullParser.getText());
                                fengxiangCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 0) {
                                evetType = xmlPullParser.next();
                                todayWeather.setFengli(xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("date") && dateCount == 0) {
                                evetType = xmlPullParser.next();
                                todayWeather.setDate(xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 0) {
                                evetType = xmlPullParser.next();
                                todayWeather.setHigh(xmlPullParser.getText().substring(3));
                                highCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 0) {
                                evetType = xmlPullParser.next();
                                todayWeather.setLow(xmlPullParser.getText().substring(3));
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 0) {
                                evetType = xmlPullParser.next();
                                todayWeather.setType(xmlPullParser.getText());
                                typeCount++;
                            }
                        }
                        break;
                    default:
                        break;
                }
                evetType = xmlPullParser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return todayWeather;
    }

    public static TodayWeather parseXmlBySax(String xmldata) throws Exception{
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        SaxHandler saxHandler = new SaxHandler();
        InputStream is = new ByteArrayInputStream(xmldata.getBytes());
        parser.parse(is,saxHandler);
        return saxHandler.getTodayWeather();
    }
}
