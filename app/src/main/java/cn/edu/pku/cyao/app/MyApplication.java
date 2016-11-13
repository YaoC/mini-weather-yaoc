package cn.edu.pku.cyao.app;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cn.edu.pku.cyao.bean.City;
import cn.edu.pku.cyao.db.CityDB;

/**
 * Created by cyao on 16-10-18.
 */

public class MyApplication extends Application{
    public static Application mApplication;
    public static final String TAG = "MyApp";

    private CityDB mCityDB;
    private List<City> mCityList;
    private String[] cityCode;
    private String[] cityName;
    private List<Map<String, Object>> listems;

    @Override
    public void onCreate(){
        super.onCreate();
        Log.d(TAG,"MyApplication->onCreate");
        mApplication = this;

        mCityDB = openCityDB();
        initCityList();
    }

    public static Application getInstance(){
        return mApplication;
    }

    private CityDB openCityDB(){
        String path = "/data"
                + Environment.getDataDirectory().getAbsolutePath()
                + File.separator + getPackageName()
                + File.separator + "databases1"
                + File.separator
                + CityDB.CITY_DB_NAME;
        File db = new File(path);
        Log.d(TAG, "path: " + path);
        if (!db.exists()) {
            String pathfolder = "/data"
                    + Environment.getDataDirectory().getAbsolutePath()
                    + File.separator + getPackageName()
                    + File.separator + "databases1"
                    + File.separator;
            File dirFirstFolder = new File(pathfolder);
            if (!dirFirstFolder.exists()) {
                dirFirstFolder.mkdirs();
                Log.d(TAG, "mkdirs");
            }
            Log.i(TAG, "db is not exists");
            try {
                InputStream is = getAssets().open("city.db");
                FileOutputStream fos = new FileOutputStream(db);
                int len = -1;
                byte[] buffer = new byte[1024];
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                    fos.flush();
                }
                fos.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
        Log.d(TAG, "openCityDB path: "+path);
        return new CityDB(this, path);
    }

    private boolean prepareCityList() {
        mCityList = mCityDB.getAllCity();
        ArrayList<String> cityCodeList = new ArrayList<String>();
        ArrayList<String> cityNameList = new ArrayList<String>();
        listems = new ArrayList<Map<String, Object>>();
        for (City city : mCityList) {
            Map<String, Object> listem = new HashMap<String, Object>();
            listem.put("city", city.getCity());
            listem.put("province", city.getProvince());
            listems.add(listem);
            cityNameList.add(city.getCity());
            cityCodeList.add(city.getNumber());
        }
        cityCode = new String[cityCodeList.size()];
        cityCode = cityCodeList.toArray(cityCode);
        cityName = new String[cityNameList.size()];
        cityName = cityNameList.toArray(cityName);
        return  true;
    }

    private void initCityList(){
        mCityList = new ArrayList<City>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                prepareCityList();
            }
        }).start();
    }

    public String[] getCityCodeList(){
        return cityCode;
    }
    public String[] getCityNameList(){
        return cityName;
    }

    public List<Map<String,Object>> getCityDetailList(){
        return listems;
    }

}
