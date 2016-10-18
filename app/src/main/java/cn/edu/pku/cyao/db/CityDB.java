package cn.edu.pku.cyao.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.edu.pku.cyao.bean.City;

/**
 * Created by cyao on 16-10-18.
 */

public class CityDB {
    public static final String CITY_DB_NAME = "city2.db";
    private static final String CITY_TABLE_NAME = "city";
    private SQLiteDatabase db;

    public CityDB(Context context,String path) {
        db = context.openOrCreateDatabase(CITY_DB_NAME, Context.MODE_PRIVATE, null);
    }

    public List<City> getAllCity(){
        List<City> list = new ArrayList<>();
        Cursor c = db.rawQuery("SELECT * from "+CITY_TABLE_NAME,null);
        while (c.moveToNext()){
            String province = c.getString(c.getColumnIndex("province"));
            String city = c.getString(c.getColumnIndex("city"));
            String number = c.getString(c.getColumnIndex("number"));
            String allPY = c.getString(c.getColumnIndex("allPY"));
            String allFirstPY = c.getString(c.getColumnIndex("allFirstPY"));
            String firstPY = c.getString(c.getColumnIndex("firstPY"));
            City item = new City(province, city, number, firstPY, allPY, allFirstPY);
            list.add(item);
        }
        return list;
    }
}
