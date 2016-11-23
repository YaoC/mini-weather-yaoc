package cn.edu.pku.cyao.util;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import cn.edu.pku.cyao.bean.TodayWeather;

/**
 * Created by cyao on 16-11-13.
 */

public class SaxHandler extends DefaultHandler {
    private TodayWeather todayWeather;
    private int fengxiangCount;
    private int fengliCount ;
    private int dateCount;
    private int highCount;
    private int lowCount;
    private int typeCount;

    private String currentTag;

    public TodayWeather getTodayWeather(){
        return todayWeather;
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        todayWeather = new TodayWeather();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        currentTag = qName;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        String currentContent = new String(ch, start, length);
        if(currentContent != null && !"".equals(currentContent) && !"\n".equals(currentContent)){
            if (todayWeather != null) {
                if (currentTag.equals("city")) {
                    todayWeather.setCity(currentContent);
                } else if (currentTag.equals("updatetime")) {
                    todayWeather.setUpdatetime(currentContent);
                } else if (currentTag.equals("shidu")) {
                    todayWeather.setShidu(currentContent);
                } else if (currentTag.equals("wendu")) {
                    todayWeather.setWendu(currentContent);
                } else if (currentTag.equals("pm25")) {
                    todayWeather.setPm25(currentContent);
                } else if (currentTag.equals("quality")) {
                    todayWeather.setQuality(currentContent);
                } else if (currentTag.equals("fengxiang") && fengxiangCount == 0) {
                    todayWeather.setFengxiang(currentContent);
                    fengxiangCount++;
                } else if (currentTag.equals("fengli") && fengliCount == 0) {
                    todayWeather.setFengli(currentContent);
                    fengliCount++;
                } else if (currentTag.equals("date") && dateCount == 0) {
                    todayWeather.setDate(currentContent);
                    dateCount++;
                } else if (currentTag.equals("high") && highCount == 0) {
                    todayWeather.setHigh(currentContent.substring(3));
                    highCount++;
                } else if (currentTag.equals("low") && lowCount == 0) {
                    todayWeather.setLow(currentContent.substring(3));
                    lowCount++;
                } else if (currentTag.equals("type") && typeCount == 0) {
                    todayWeather.setType(currentContent);
                    typeCount++;
                }
            }
        }
    }
}
