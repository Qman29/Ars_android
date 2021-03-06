package com.example.qman.myapplication.lyf.drawarea;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.MapGeometry;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Graphic;
import com.esri.core.renderer.SimpleRenderer;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.example.qman.myapplication.R;
import com.example.qman.myapplication.areatab.AreaFragment;
import com.example.qman.myapplication.utils.ActivityUtil;
import com.example.qman.myapplication.utils.FormFile;
import com.example.qman.myapplication.utils.IncreasingId;
import com.example.qman.myapplication.utils.ListViewUtil;
import com.example.qman.myapplication.utils.RequestUtil;
import com.example.qman.myapplication.utils.SocketHttpRequester;
import com.example.qman.myapplication.utils.Util;
import com.example.qman.myapplication.utils.Variables;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

import static android.R.attr.bitmap;
import static android.content.ContentValues.TAG;
import static com.example.qman.myapplication.R.string.username;
import static com.example.qman.myapplication.utils.Util.getViewBitmap;
import static com.example.qman.myapplication.utils.Util.saveMyBitmap;
import static com.example.qman.myapplication.utils.Variables.targetServerURL;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import android.os.Handler;
import android.widget.Toast;

/**
 * Created by lyf on 08/03/2017.
 */

public class SaveDrawArea extends Fragment {
    String mDrawAreaStr;
    MapView mMapView;
    GraphicsLayer DrawAreaGraphicLayer = new GraphicsLayer();

    FloatingActionButton mSave;
    FloatingActionButton mCancel;

    RadioGroup CropKindsRadioGroup;

    RadioButton CropKindsOfWheat;
    RadioButton CropKindsOfRice;
    RadioButton CropKindsOfCorn;

    String mCropKind = "";
    EditText mDrawAreaName;
    String fieldName = "";
    String FileDirectory = "";
    String TAG = "SaveDrawArea";

    String mCodeIdOfArea;

    File upfile;

//    Geometry mgeometry;

    public static SaveDrawArea newInstance(String graphic)
    {
        SaveDrawArea newFragment = new SaveDrawArea();
        Bundle bundle = new Bundle();
        bundle.putString("DrawAreaString",graphic);
        newFragment.setArguments(bundle);
        return newFragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.savedrawarea, container, false);
        mMapView = (MapView)view.findViewById(R.id.mapofsavedrawarea);

        mSave = (FloatingActionButton)view.findViewById(R.id.saveOfSaveDrawArea);
        mCancel = (FloatingActionButton)view.findViewById(R.id.cancelOfSaveDrawArea);

        mDrawAreaName = (EditText)view.findViewById(R.id.fieldNameofDrawArea);

        CropKindsRadioGroup = (RadioGroup)view.findViewById(R.id.CropKindsRadioGroup);

        CropKindsOfWheat  = (RadioButton)view.findViewById(R.id.CropKindsOfWheat);
        CropKindsOfRice  = (RadioButton)view.findViewById(R.id.CropKindsOfRice);
        CropKindsOfCorn  = (RadioButton)view.findViewById(R.id.CropKindsOfCorn);

        CropKindsRadioGroup.setOnCheckedChangeListener(new RadioGroupListener());

        ActivityUtil.setTitle(getActivity(),R.id.toolbar_title,"保存区域");
        ActivityUtil.setOnlyVisibilitys(getActivity(),R.id.toolbar_title, R.id.toolbar_search, R.id.toolbar_add,R.id.toolbar_draw);
        /*Bundle args = getArguments();
        if(args!=null)
        {
            mDrawAreaStr = args.getString("DrawAreaString");
            Log.d("SaveDrawArea",mDrawAreaStr);
        }*/
        mDrawAreaStr = ActivityUtil.getParam(getActivity(),"DrawAreaString");
        Log.d("SaveDrawArea",mDrawAreaStr);

        mMapView.setOnStatusChangedListener(new OnStatusChangedListener() {

            private static final long serialVersionUID = 1L;

            @Override
            public void onStatusChanged(Object source, STATUS status) {
                if (source == mMapView && status == STATUS.INITIALIZED) {
                    try{
                        mMapView.addLayer(DrawAreaGraphicLayer);

                        String[] geometryStr = {mDrawAreaStr};
                        new AsyncLoadGraphic().execute(geometryStr);

//                        JsonFactory factory = new JsonFactory();
//                        JsonParser jsonParser = factory.createJsonParser(mDrawAreaStr);
//                        MapGeometry mapGeometry = GeometryEngine.jsonToGeometry(jsonParser);
//                        Geometry geometry = mapGeometry.getGeometry();
//
//                        SimpleFillSymbol simpleFillSymbol = new SimpleFillSymbol(Color.YELLOW);
//                        simpleFillSymbol.setAlpha(100);
//                        simpleFillSymbol.setOutline(new SimpleLineSymbol(Color.BLACK, 4));
//                        Graphic graphic = new Graphic(geometry, (simpleFillSymbol));
//
//
//                        DrawAreaGraphicLayer.addGraphic(graphic);



//                        Envelope env = new Envelope();
//                        geometry.queryEnvelope(env);
//
//                        Envelope newEnv = new Envelope(env.getCenter(),env.getWidth()*2,env.getHeight()*2);
//
//                        Log.d(TAG,env.toString());
//                        Log.d(TAG,newEnv.toString());
//
//                        mMapView.setExtent(env);
//                        Log.d(TAG,"added");


                    }
//                    catch(IOException ex)
//                    {
//                        ex.printStackTrace();
//                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }

                }
            }
        });




        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                //"/storage/sdcard0/Arsandroid/2017-03-27-14-49-42_test5.png"
                fieldName = mDrawAreaName.getText().toString();//地块名称
                mCodeIdOfArea = "10"+ (int)((Math.random()*9+1)*1000);

                Bitmap bitmap=Util.getViewBitmap(mMapView);

                FileDirectory = Util.saveMyBitmap(mCodeIdOfArea,bitmap);//保存缩略图，并返回文件路径

                Log.d(TAG,FileDirectory);

                bitmap.recycle();
                // 开启一个子线程，进行网络操作，等待有返回结果，使用handler通知UI
                upfile = new File(FileDirectory);
                new Thread(runnable).start();
                //更新数据库表
                //new AreaCodeInfoServiceThreadTask().execute();






            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                FragmentTransaction ft = getFragmentManager().beginTransaction();
//                ft.remove(this);
//                ft.show(Fragment1);
//                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//                ft.commit();



            }
        });

        return view;
    }

    class AsyncLoadGraphic extends AsyncTask<String,Void,Geometry>{
        @Override
        protected Geometry doInBackground(String... geometryStr)
        {

            if (geometryStr == null || geometryStr.length < 1)
            {
                return null;
            }



            try{

                //Log.d(TAG,geometryStr[0]);
                JsonFactory factory = new JsonFactory();
                JsonParser jsonParser = factory.createJsonParser(geometryStr[0]);
                MapGeometry mapGeometry = GeometryEngine.jsonToGeometry(jsonParser);
                Geometry geometry = mapGeometry.getGeometry();

                SimpleFillSymbol simpleFillSymbol = new SimpleFillSymbol(Color.YELLOW);
                simpleFillSymbol.setAlpha(100);
                simpleFillSymbol.setOutline(new SimpleLineSymbol(Color.BLACK, 4));
                Graphic graphic = new Graphic(geometry, (simpleFillSymbol));


                DrawAreaGraphicLayer.addGraphic(graphic);

                //Log.d(TAG,"added");

                return geometry;





            }
            catch(IOException ex)
            {
                ex.printStackTrace();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Geometry geometry)
        {
            Envelope env = new Envelope();
            geometry.queryEnvelope(env);

            //Envelope newEnv = new Envelope(env.getCenter(),env.getWidth()*2,env.getHeight()*2);

//            Log.d(TAG,env.toString());
//            Log.d(TAG,newEnv.toString());

            mMapView.setExtent(env);
        }

    }
    class RadioGroupListener implements RadioGroup.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId==CropKindsOfWheat.getId()){
                System.out.println("CropKindsOfWheat!");
                mCropKind = CropKindsOfWheat.getText().toString();

            }else if (checkedId==CropKindsOfRice.getId()){
                System.out.println("CropKindsOfWheat!");
                mCropKind = CropKindsOfRice.getText().toString();
            }else if(checkedId==CropKindsOfCorn.getId())
            {
                mCropKind = CropKindsOfCorn.getText().toString();
                System.out.println("CropKindsOfCorn!");
            }else {
                mCropKind = "";
            }

        }
    }


    class AreaCodeInfoServiceThreadTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            JSONObject ajsonObject = new JSONObject();
            try {
                //String codeIdTemp = "10"+ (int)((Math.random()*9+1)*1000);

                String fileURL = Variables.serviceIP+"upload/image/"+ActivityUtil.getParam(getActivity(),"id")+"/"+upfile.getName();

                ajsonObject.put("userid",ActivityUtil.getParam(getActivity(),"id"));
                ajsonObject.put("codeid",mCodeIdOfArea);
                //ajsonObject.put("sdpath",FileDirectory);
                ajsonObject.put("sdpath",fileURL);
                ajsonObject.put("geometry",mDrawAreaStr);//mDrawAreaStr
                ajsonObject.put("ordername",fieldName);

                ajsonObject.put("cropkinds",mCropKind);

                RequestUtil.request(ajsonObject.toString(),"AndroidService/areaCodeInfoService");//新增订购区域信息
                String newLocno = ActivityUtil.getParam(getActivity(),"locno")+mCodeIdOfArea+"/";//拼接新的codeid字符串
                ajsonObject.put("codeidStr",newLocno);
                ActivityUtil.changeParam(getActivity(),"locno",newLocno);
                ajsonObject.put("id",ActivityUtil.getParam(getActivity(),"id"));
                RequestUtil.request(ajsonObject.toString(),"AndroidService/updateUserCodeIdService");//更新用户表中的区域codeid字段
                ListViewUtil.addData(fieldName,fileURL,"000",mCodeIdOfArea,"000");//第二个参数为缩略图显示地址
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return "";
        }
        //onPostExecute方法用于在执行完后台任务后更新UI,显示结果
        @Override
        protected void onPostExecute(String s) {
            //删除设备上的中间文件
            if (upfile.delete())
            {
                Log.i(TAG, upfile.getName()+" local delete");
            }

            ActivityUtil.switchToFragment(getActivity(),new AreaFragment(),R.id.id_content);



        }
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }
//    private Bitmap getViewBitmap(MapView v) {
//
//        v.clearFocus();
//        v.setPressed(false);
//
//        boolean willNotCache = v.willNotCacheDrawing();
//        v.setWillNotCacheDrawing(false);
//        int color = v.getDrawingCacheBackgroundColor();
//        v.setDrawingCacheBackgroundColor(0);
//        if (color != 0) {
//            v.destroyDrawingCache();
//        }
//        v.buildDrawingCache();
//        Bitmap cacheBitmap = null;
//        while (cacheBitmap == null) {
//            cacheBitmap = v.getDrawingMapCache(0, 0, v.getWidth(),
//                    v.getHeight());
//        }
//        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);
//        v.destroyDrawingCache();
//        v.setWillNotCacheDrawing(willNotCache);
//        v.setDrawingCacheBackgroundColor(color);
//        return bitmap;
//    }
//    public String saveMyBitmap(String bitName,Bitmap mBitmap){
//
//        //String FileName=this.getInnerSDCardPath() + "/" + bitName + ".png";
//        //获取当前系统时间
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
//        Date curDate =new Date(System.currentTimeMillis());
//        String DateStr = formatter.format(curDate);
//        Log.d("saveMyBitmap",DateStr);
//
//        String FileName = null;
//        try{
//
//            String FileDir = Util.CreateFileDir("Arsandroid");
//            FileName = FileDir+ "/" + DateStr+"_"+bitName + ".png";
//
//            Log.d("saveMyBitmap",FileName);
//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }
//
//
//        //ShowMessage(FileName);
//        File f = new File(FileName);
//        try {
//            f.createNewFile();
//        } catch (IOException e) {
//        // TODO Auto-generated catch block
//            Log.e("在保存"+FileName+"图片时出错：" + e.toString(),"在保存"+FileName+"图片时出错：" + e.toString());
//        }
//        FileOutputStream fOut = null;
//        try {
//            fOut = new FileOutputStream(f);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
//        try {
//            fOut.flush();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        try {
//            fOut.close();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return FileName;
//    }

//    public String getSDPath(){
//        File sdDir = null;
//        boolean sdCardExist = Environment.getExternalStorageState()
//                .equals(android.os.Environment.MEDIA_MOUNTED); //判断sd卡是否存在
//        if (sdCardExist)
//        {
//            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
//        }
//        return sdDir.toString();
//    }
//    //创建文件夹及文件
//    public String CreateFileDir(String fileDir) throws IOException {
//
//        String fileDirectory = getSDPath()+"/"+fileDir;//文件夹路径
//        File filedir = new File(fileDirectory);
//        if (!filedir.exists()) {
//            try {
//                //按照指定的路径创建文件夹
//                filedir.mkdirs();
//                return fileDirectory;
//            } catch (Exception e) {
//                // TODO: handle exception
//            }
//        }
//        return fileDirectory;
//    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String val = data.getString("value");
            Log.i("mylog", "请求结果为-->" + val);
            // TODO
            // UI界面的更新等相关操作
        }
    };

    Runnable runnable=new Runnable() {

        @Override
        public void run() {
            Log.i(TAG, "runnable run");
            uploadFile(upfile);
            //handler.postDelayed(runnable, 5000);
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("value", "请求结果");
            msg.setData(data);
            handler.sendMessage(msg);
        }

    };
    /**
     * 上传图片到服务器
     *
     * @param imageFile 包含路径
     */
    public void uploadFile(File imageFile) {
        Log.i(TAG, "upload start");
        try {
            String requestUrl = Variables.serviceIP+"upload/UploadAction.do";
            //请求普通信息
            Map<String, String> params = new HashMap<String, String>();
            params.put("userid", ActivityUtil.getParam(getActivity(),"id"));
            params.put("username", ActivityUtil.getParam(getActivity(),"username"));
//            params.put("age", "21");
            params.put("fileName", imageFile.getName());

            Log.i(TAG, imageFile.getName());

            //上传文件
            FormFile formfile = new FormFile(imageFile.getName(), imageFile, "image", "application/octet-stream");

            SocketHttpRequester.post(requestUrl, params, formfile);
            Log.i(TAG, "upload success");
        } catch (Exception e) {
            Log.i(TAG, "upload error");
            e.printStackTrace();
        }
        finally {
//            更新数据库表
            new AreaCodeInfoServiceThreadTask().execute();

        }
        Log.i(TAG, "upload end");
    }
}
