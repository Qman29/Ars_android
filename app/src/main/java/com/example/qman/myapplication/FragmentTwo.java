package com.example.qman.myapplication;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.bigkoo.pickerview.OptionsPickerView;
import com.example.qman.myapplication.indextab.AddressBean;
import com.example.qman.myapplication.indextab.JsonUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FragmentTwo extends Fragment implements OnClickListener
{
    private String json = "";
    private String updateJson = "";
    private OkHttpClient okHttpClient = new OkHttpClient();
    JSONObject jsonObject = null;//利用json字符串生成json对象
    private Button registerBtn ;
    private TextView tv_address;

    private ArrayList<AddressBean> provinceList = new ArrayList<>();//创建存放省份实体类的集合

    private ArrayList<String> cities ;//创建存放城市名称集合
    private ArrayList<List<String>> citiesList= new ArrayList<>();//创建存放城市名称集合的集合

    private ArrayList<String> areas ;//创建存放区县名称的集合
    private ArrayList<List<String>> areasList ;//创建存放区县名称集合的集合
    private ArrayList<List<List<String>>> areasListsList = new ArrayList<>();//创建存放区县集合的集合的集合
    private OptionsPickerView mPvOptions;

    private String provinceSelected = null;
    private String citiesSelected = null;
    private String areaSelecteds = null;
    private String codeidStr = "";
    private String id = "";
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    private ListView listView;

    private ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String,Object>>();
    private SimpleAdapter adapter = null;
    private ArrayList<HashMap<String, Object>> initSplitData(){
        data.clear();
//        String codeIdJson = "{'codeid':'" + codeidStr + "'}";
//        //把请求的内容字符串转换为json
//        RequestBody body = RequestBody.create(JSON, codeIdJson);
//        Request request = new Request.Builder()
//                .url("http://10.2.3.182:8080/AndroidService/cityInfoService")
//                .post(body)
//                .build();
//        okHttpClient.newCall(request).enqueue(callbackCityInfo);//callback是请求后的回调接口
        return data;
    }

    private ArrayList<HashMap<String, Object>> addData(String province,String city,String area){
        HashMap<String,Object>map = new HashMap<String,Object>();
        map.put("province", province);
        map.put("city", city);
        map.put("area", area);
        data.add(map);
        return data;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.friend_layout, container, false);
        if (getArguments() != null) {
            json = getArguments().getString("param");

        }

        Intent intent= getActivity().getIntent();
//        id = intent.getStringExtra("id");
//        codeidStr = intent.getStringExtra("locno");
        listView = (ListView)view.findViewById(R.id.areaLists);
        //listView.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_expandable_list_item_1,splitData()));

        /* 参数一多，有些人就头晕了。这里解说下，各个参数的意思。
         * 第一个参数 this 代表的是当前上下文，可以理解为你当前所处的activity
         * 第二个参数 getData() 一个包含了数据的List,注意这个List里存放的必须是map对象。simpleAdapter中的限制是这样的List<? extends Map<String, ?>> data
         * 第三个参数 R.layout.user 展示信息的组件
         * 第四个参数 一个string数组，数组内存放的是你存放数据的map里面的key。
         * 第五个参数：一个int数组，数组内存放的是你展示信息组件中，每个数据的具体展示位置，与第四个参数一一对应
         * */
        adapter = new SimpleAdapter(getActivity(), initSplitData(), R.layout.city,
                new String[]{"province","city","area"}, new int[]{R.id.province,R.id.city,R.id.area});
        listView.setAdapter(adapter);

        registerBtn = (Button) view.findViewById(R.id.registerBtn);
        registerBtn.setOnClickListener(this);
        return view ;
    }
    @Override
    public void onClick(View v)
    {
        json += "'codeidStr':'" + codeidStr + "'}";
        //把请求的内容字符串转换为json
        RequestBody body = RequestBody.create(JSON, json);
        Log.v("json",json);
        Request request = new Request.Builder()
                .url("http://10.2.3.182:8080/AndroidService/registerService")
                .post(body)
                .build();
        okHttpClient.newCall(request).enqueue(callback);//callback是请求后的回调接口

        Intent intent = new Intent();
        intent.setClass(getActivity(), MainActivity.class);
        startActivity(intent);//红色部分为要打开的心窗口的类名
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        tv_address = (TextView) getActivity().findViewById(R.id.tv_address);
        //获取json字符串,用来解析以获取集合
        String jsonString = JsonUtils.getJsonString(getActivity(),
                "province_data.json");
        //解析json字符串,向各级集合中添加元素
        parseJson(jsonString);
        mPvOptions = new OptionsPickerView(getActivity());

        //设置三级联动的效果
        mPvOptions.setPicker(provinceList, citiesList, areasListsList, true);

        //设置可以循环滚动,true表示这一栏可以循环,false表示不可以循环
        mPvOptions.setCyclic(true, false, false);

        //设置默认选中的位置
        mPvOptions.setSelectOptions(0, 0, 0);
        mPvOptions.setOnoptionsSelectListener(new OptionsPickerView.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3) {
                //返回的分别是三个级别的选中位置
                String city = provinceList.get(options1).getPickerViewText();
                String address;
                //  如果是直辖市或者特别行政区只设置市和区/县
//                if ("北京市".equals(city) || "上海市".equals(city) || "天津市".equals(city) || "重庆市".equals(city) || "澳门".equals(city) || "香港".equals(city)) {
//                    address = provinceList.get(options1).getPickerViewText()
//                            + " " + areasListsList.get(options1).get(option2).get(options3);
//                    provinceSelected = provinceList.get(options1).getPickerViewText();
//                    citiesSelected = null;
//                    areaSelecteds = areasListsList.get(options1).get(option2).get(options3);;
//                } else {
                address = provinceList.get(options1).getPickerViewText()
                        + " " + citiesList.get(options1).get(option2)
                        + " " + areasListsList.get(options1).get(option2).get(options3);
                provinceSelected = provinceList.get(options1).getPickerViewText();
                citiesSelected = citiesList.get(options1).get(option2);
                areaSelecteds = areasListsList.get(options1).get(option2).get(options3);
//                }

                //查询订购区域代码codeid
                String json = "{'provinceSelected':'" + provinceSelected + "'," + "'citiesSelected':'" + citiesSelected + "',"
                        + "'areaSelecteds':'" + areaSelecteds +
                        "'}";
                //把请求的内容字符串转换为json
                RequestBody body = RequestBody.create(JSON, json);
                Request request = new Request.Builder()
                        .url("http://10.2.3.182:8080/AndroidService/cityService")
                        .post(body)
                        .build();
                okHttpClient.newCall(request).enqueue(callbackCity);//callback是请求后的回调接口
                addData(provinceSelected,citiesSelected,areaSelecteds);
                adapter.notifyDataSetChanged();
                tv_address.setText(address);
            }
        });
        //点击文本框的时候,显示地址选择框
        tv_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPvOptions.show();
            }
        });
    }

    //解析获得的json字符串,放在各个集合中
    private void parseJson(String json){
        try {
            //得到一个数组类型的json对象
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {//对数组进行遍历得到每一个jsonobject对象
                JSONObject provinceObject = (JSONObject) jsonArray.get(i);
                String provinceName = provinceObject.getString("name");//得到省份的名字
                provinceList.add(new AddressBean(provinceName));//向集合里面添加元素
                JSONArray cityArray = provinceObject.optJSONArray("city");
                cities = new ArrayList<>();//创建存放城市名称集合
                areasList = new ArrayList<>();//创建存放区县名称的集合的集合
                for (int j = 0; j < cityArray.length(); j++) {//遍历每个省份集合下的城市列表
                    JSONObject cityObject = (JSONObject) cityArray.get(j);
                    String cityName = cityObject.getString("name");
                    cities.add(cityName);//向集合里面添加元素
                    JSONArray areaArray = cityObject.optJSONArray("area");
                    areas = new ArrayList<>();//创建存放区县名称的集合
                    for (int k = 0; k < areaArray.length(); k++) {
                        String areaName = areaArray.getString(k);
                        areas.add(areaName);
                    }
                    areasList.add(areas);
                }
                citiesList.add(cities);
                areasListsList.add(areasList);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //请求后的回调接口
    private Callback callback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            // setResult(e.getMessage());
        }
        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String str = response.body().string();
            try {
                jsonObject = new JSONObject(str);
                String result =  jsonObject.getString("result");//解析json查询结果
                if(result.equals("success")){
                } else {
                    //setResult("注册失败");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    private Callback callbackCity = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            // setResult(e.getMessage());
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String str = response.body().string();
            try {
                jsonObject = new JSONObject(str);
                String result =  jsonObject.getString("result");//解析json查询结果
                if(result.equals("success")){
                    Log.v("codeid",jsonObject.getString("codeid"));
                    codeidStr += jsonObject.getString("codeid") + "/";
                    Log.v("codeidStr",codeidStr);
                } else {
                    //setResult("注册失败");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    //请求后的回调接口
    private Callback callbackCityInfo = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            // setResult(e.getMessage());
        }
        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String str = response.body().string();
            try {
                jsonObject = new JSONObject(str);
                String result =  jsonObject.getString("result");//解析json查询结果
                if(result.equals("success")){
                    String dataStr = jsonObject.getString("data");
                    JSONArray areaLists = new JSONArray(dataStr);
                    if (areaLists.length()>0) {
                        for (int i=0;i<areaLists.length();i++) {
                            JSONArray aArea = new JSONArray(areaLists.get(i).toString());
                            HashMap<String, Object> listm = new HashMap<String, Object>();
                            listm.put("province", aArea.get(0).toString());
                            listm.put("city", aArea.get(1).toString());
                            listm.put("area", aArea.get(2).toString());
                            data.add(listm);
                        }
                    }
                } else {
                    //setResult("注册失败");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    public static com.example.qman.myapplication.FragmentTwo newInstance(String text) {
        com.example.qman.myapplication.FragmentTwo fragment = new com.example.qman.myapplication.FragmentTwo();
        Bundle args = new Bundle();
        args.putString("param", text);
        fragment.setArguments(args);
        return fragment;
    }
}