package cn.edu.pku.cyao.miniweather;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by cyao on 16-9-21.
 */

public class MainActivity extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_info);
    }
}
