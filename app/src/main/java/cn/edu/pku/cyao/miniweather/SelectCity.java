package cn.edu.pku.cyao.miniweather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;

import cn.edu.pku.cyao.app.MyApplication;

/**
 * Created by cyao on 16-10-18.
 */

public class SelectCity extends Activity implements View.OnClickListener{

    private ImageView mBackBtn;
    private String currentCityCode;
    private String currentCityName;
    private TextView cityTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);

        mBackBtn = (ImageView) findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);
        Log.d("selectCity", "onCreate: selectCity");
        cityTitle = (TextView) findViewById(R.id.title_name);
        initListView();
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.title_back:
                Intent i = new Intent();
                i.putExtra("cityCode", currentCityCode);
                setResult(RESULT_OK,i);
                finish();
                break;
            default:
                break;
        }
    }

    private void initListView(){
        final MyApplication app = (MyApplication)getApplicationContext();
        ArrayList<Map<String, Object>> listems = (ArrayList<Map<String, Object>>)app.getCityDetailList();
        final ListView cityListView = (ListView) findViewById(R.id.city_list_view);
        SimpleAdapter simplead = new SimpleAdapter(SelectCity.this,listems,R.layout.city_item,
                new String[]{"city","province"},new int[]{R.id.city_name,R.id.province_name});
        cityListView.setAdapter(simplead);
        cityListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentCityCode = app.getCityCodeList()[position];
                currentCityName = app.getCityNameList()[position];
                Toast.makeText(SelectCity.this,"你选择了： "+currentCityName,Toast.LENGTH_SHORT).show();
                cityTitle.setText("当前城市："+currentCityName);
            }
        });
    }
}
