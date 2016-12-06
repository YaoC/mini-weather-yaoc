package cn.edu.pku.cyao.miniweather;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import cn.edu.pku.cyao.app.MyApplication;

/**
 * Created by cyao on 16-10-18.
 */

public class SelectCity extends Activity implements View.OnClickListener{

    private int resultStatus;

    private ImageView mBackBtn;
    private String currentCityCode;
    private String currentCityName;
    private TextView cityTitle;
    private ArrayList<Map<String, Object>> totalListItem;
    private List listItems;
    private EditText citySerachEditText;
    private Handler textHandler;
    private Runnable textChanged;
    private MyApplication app;
    private SimpleAdapter simplead;

    private String[] cityNames;
    private String[] cityCodes;

    private List<String> currentCityNames;
    private List<String> currentCityCodes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);
        mBackBtn = (ImageView) findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);
        Log.d("selectCity", "onCreate: selectCity");
        app = (MyApplication)getApplicationContext();
        totalListItem = (ArrayList<Map<String, Object>>)app.getCityDetailList();
        cityNames = app.getCityNameList();
        cityCodes = app.getCityCodeList();
        listItems = new ArrayList();
        cityTitle = (TextView) findViewById(R.id.title_name);
        textHandler = new Handler();
        citySerachEditText = (EditText) findViewById(R.id.search_city);
        textChanged = new Runnable() {
            @Override
            public void run() {
                String text = citySerachEditText.getText().toString();
                listItems.clear();
                currentCityCodes.clear();
                currentCityNames.clear();
                if (text.length() == 0) {
                    currentCityCodes.addAll(Arrays.asList(cityCodes));
                    currentCityNames.addAll(Arrays.asList(cityNames));
                    listItems.addAll(totalListItem);
                }else{
                    getItemSub(text);
                }
                simplead.notifyDataSetChanged();
            }
        };
        citySerachEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                textHandler.post(textChanged);
            }
        });
        currentCityName = getIntent().getStringExtra("currentCity");
        currentCityCode = getIntent().getStringExtra("currentCode");
        cityTitle.setText("当前城市："+currentCityName);
        resultStatus = RESULT_CANCELED;
        initListView();
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.title_back:
                Intent i = new Intent();
                i.putExtra("cityCode", currentCityCode);
                setResult(resultStatus,i);
                finish();
                break;
            default:
                break;
        }
    }

    private void initListView(){
        listItems.addAll(totalListItem);
        currentCityCodes = new ArrayList<String>();
        currentCityNames = new ArrayList<String>();
        currentCityCodes.addAll(Arrays.asList(cityCodes));
        currentCityNames.addAll(Arrays.asList(cityNames));
        final ListView cityListView = (ListView) findViewById(R.id.city_list_view);
        simplead = new SimpleAdapter(SelectCity.this, listItems,R.layout.city_item,
            new String[]{"city","province"},new int[]{R.id.city_name,R.id.province_name});
        cityListView.setAdapter(simplead);
        cityListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            // 隐藏软键盘
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
            String selectedCityCode = currentCityCodes.get(position);
            if(!currentCityCode.equals(selectedCityCode)){
                currentCityCode = selectedCityCode;
                currentCityName = currentCityNames.get(position);
                Toast.makeText(SelectCity.this,"你选择了： "+currentCityName,Toast.LENGTH_SHORT).show();
                cityTitle.setText("当前城市："+currentCityName);
                resultStatus = RESULT_OK;
            }

            }
        });
    }

    private void getItemSub(String sub){
        int length = totalListItem.size();
        for (int i = 0; i < length; ++i) {
            Map<String, Object> item = totalListItem.get(i);
            if (item.values().toString().contains(sub)) {
                currentCityCodes.add(cityCodes[i]);
                currentCityNames.add(cityNames[i]);
                listItems.add(item);
            }
        }
    }
}
