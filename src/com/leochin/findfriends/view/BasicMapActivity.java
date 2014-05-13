/**
 * 
 * FileName BasicMapActivity.java  <br />
 * @author Mr.Wen <br />
 * @version 1.0   <br />
 * @created 2013-5-6 上午10:37:08 <br />
 * 
 */
package com.leochin.findfriends.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.CancelableCallback;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.LocationSource.OnLocationChangedListener;
import com.amap.api.maps.SupportMapFragment;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.leochin.findfriends.R;
import com.leochin.findfriends.data.Constants;
import com.leochin.findfriends.data.HostUser;
import com.leochin.findfriends.data.UserInfo;
import com.leochin.findfriends.push.MQTTService;
import com.leochin.findfriends.util.AMapUtil;
import com.leochin.findfriends.util.Debugs;
import com.leochin.findfriends.util.Utility;

/**
 * 
 * @author Mr.Wen <br />
 * @version 1.0 <br />
 * @created 2013-5-6 上午10:37:08 <br />
 */
public class BasicMapActivity extends FragmentActivity {

    private final static String TAG = "BasicMapActivity";

    private AMap mAMap = null;
    private LocationManagerProxy mAMapLocationManager;
    private AMapLocationListener mLocationListener;//地图的位置监听
    private OnLocationChangedListener mListener; // 用来标记自己的定位
    private LocationSource  mLocationSource ;//地图定位 

    private boolean firstLocation; //第一次位置标记
    private boolean updateFlag;//用于停止位置更新的标志位   
    
    private BroadcastReceiver mBrocadRecvState;//推送服务器的状态监听
    private BroadcastReceiver mBrocadRecvMsg;  //推送服务器的消息监听

    private Map<String, UserInfo> mUserInfoMap = new HashMap<String, UserInfo>();

    private EditText mMsgEditText;          //消息输入框
    private View mMsgView;					//下面的整个View
    private View mSendMsgView;		    	//发送消息按钮
    private boolean mBroadMsgFlag = false;
    
    private ListView  mMsgListView;           //listView显示消息
    private ArrayAdapter<String> mMsgAdapter; //adapter
    private List<String> mMsgList = new ArrayList<String>();//消息的集合
    private TextView mMsgTitle;                           //消息的标题
    
    private Marker curSelectMarker;                       //当前选中的Marker  
    
    @Override
    protected void onCreate(Bundle arg0) {
        // TODO Auto-generated method stub
        super.onCreate(arg0);
        setContentView(R.layout.activity_map);

        /* 初始化Amap对象 */
        //这里的地图是一个Fragment
        mAMap = ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map)).getMap();

        //对AMap对象判断是否为null
        if (AMapUtil.checkReady(this, mAMap)) {
            setUpMap();
        }
        
        /* 初始化推送服务接受器 */
        initPushServiceReceiver();
        
        /* 初始化消息显示、发送win*/
        initMsgWin();
        
        /* 定位自己的位置*/
        findViewById(R.id.ID_MAP_MY_LACATION).setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                toMyLoacation();
            }
        });
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        updateFlag = true;
        firstLocation = true;
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onStop();
        updateFlag = false;
        
        logout();
        
        /* 停止定位*/
        mLocationSource.deactivate();
        
        /* 取消服务注册*/
        unregisterReceiver(mBrocadRecvState);
        unregisterReceiver(mBrocadRecvMsg);
    }
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        startActivity(new Intent(this, SettingActivity.class));
        return true;
    }
*/

    /**
     * 返回按钮的处理
     */
    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
            quitDialog();
    }
    
    /**
     * 初始化推送服务接受器
     */
    private void initPushServiceReceiver(){
        /* 初始化推送广播状态接收器*/
        mBrocadRecvState = new BroadcastReceiver(){

            @Override
            public void onReceive(Context context, Intent intent) {
                // TODO Auto-generated method stub
                Debugs.d(TAG, "Recv push service reciver : state");
                dealPushServiceState(intent);
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction (MQTTService.MQTT_STATUS_INTENT);
        registerReceiver(mBrocadRecvState, filter);
        
        
        /* 初始化推送广播消息接收器*/
        mBrocadRecvMsg = new BroadcastReceiver(){

            @Override
            public void onReceive(Context context, Intent intent) {
                // TODO Auto-generated method stub
                Debugs.d(TAG, "Recv push service reciver : msg");
                dealPushServiceMsg(intent);
            }
        };
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction (MQTTService.MQTT_MSG_RECEIVED_INTENT);
        registerReceiver(mBrocadRecvMsg, filter2);
    }
    
    /**
     * 初始化UI
     */
    private void initMsgWin(){
        
        CheckBox checkBox = (CheckBox)findViewById(R.id.ID_MAP_BROADCAST_CHECKBOX);
        checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                mBroadMsgFlag =  isChecked;
            }
        });
        
        findViewById(R.id.ID_MAP_BUTTON_SEND).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                sendMsg();
            }
        });
        
        mMsgEditText = (EditText) findViewById(R.id.ID_MAP_EDITTEXT_SENDMSG);
        
        mMsgView = findViewById(R.id.ID_MAP_SHOW_MSG);
        mMsgView.setVisibility(View.INVISIBLE);
        mSendMsgView = findViewById(R.id.ID_MAP_SENDMSG);
        mSendMsgView.setVisibility(View.INVISIBLE);
        
        mMsgTitle = (TextView) findViewById(R.id.ID_MAP_MSG_TITLE);
        
        mMsgListView = (ListView) findViewById(R.id.ID_MAP_LISTVIEW_MSG);
        

        mMsgAdapter= new ArrayAdapter<String>(this,R.layout.msg_list_item,mMsgList);
        mMsgListView.setAdapter(mMsgAdapter);
       
    }

    
    /**
     * 处理推送服务状态改变消息
     */
    private void dealPushServiceState(Intent intent){
        Debugs.d(TAG, intent.getStringExtra(MQTTService.MQTT_STATUS_MSG));
    }
    
    /**
     * 处理推送服务
     */
    private void dealPushServiceMsg(Intent intent){
        String msg = intent.getStringExtra(MQTTService.MQTT_MSG_RECEIVED_MSG);
        Log.d("msg", "msg="+msg);
        
        if(!msg.contains("uid")){
            return;
        }
        
        Gson gson = new Gson();
        UserInfo temp = gson.fromJson(msg, new TypeToken<UserInfo>(){}.getType());
        
        UserInfo user = mUserInfoMap.get(temp.getUid());
        
        if(user == null){
            user = new UserInfo();
            user.setUserInfo(temp);
            mUserInfoMap.put(user.getUid(), user);
        }
        
        if(msg.contains("msg")){
            user.addMsg(temp.getMsg()+"..."+temp.getTime());
            Debugs.d(TAG, "msg list="+user.getMsgList());
            if(!TextUtils.isEmpty(user.getUsername())&&!TextUtils.isEmpty(user.getUid())){
            	updateUserMarker(user);
            }
            
        }else if(msg.contains("online")){
            user.setOnline(temp.isOnline());
            if(user.getMarker() != null){
                user.getMarker().remove();
            }
            user.setMarker(null);
            
        }else {
            user.setOnline(true);
            user.setLatitude(temp.getLatitude());
            user.setLongitude(temp.getLongitude());
            if(user.getMarker()==null){
                updateUserMarker(user);
            }else{
                user.getMarker().setPosition(user.getLatLng());
            }
        }
    }
    
    /**
     * 设置Map 
     */
    private void setUpMap() {

        /* 设置定位 */
        mLocationSource = new LocationSource() {

            @Override
            public void activate(OnLocationChangedListener listener) {
                // TODO Auto-generated method stub

                mListener = listener;

                /* 获取MapLocationManager对象 */
                if (mAMapLocationManager == null) {
                    mAMapLocationManager = LocationManagerProxy
                            .getInstance(BasicMapActivity.this);
                }

                // 网络定位
                mAMapLocationManager.requestLocationUpdates(
                        LocationProviderProxy.AMapNetwork, 10, 5000,
                        mLocationListener);
            }

            @Override
            public void deactivate() {
                // TODO Auto-generated method stub
                mLocationListener = null;
                mListener = null;
                if (mAMapLocationManager != null) {
                    mAMapLocationManager.removeUpdates(mLocationListener);
                    mAMapLocationManager.destory();
                }
                mAMapLocationManager = null;
            }
        };
        
        mAMap.setLocationSource(mLocationSource);

        /* 设置定位处理 */
        mLocationListener = new AMapLocationListener() {
            @Override
            public void onLocationChanged(Location arg0) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProviderDisabled(String arg0) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProviderEnabled(String arg0) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onLocationChanged(AMapLocation arg0) {
                // TODO Auto-generated method stub

                /* 显示自己的定位点 */
                // if (mListener != null) {
                // mListener.onLocationChanged(arg0);
                // }
                //Debugs.d(TAG, arg0.toString());

                /* 如果位置发生大变化、则设置HostUser经纬度，并发送 */
                HostUser hostInfo = HostUser.getHostUserInstance();

                hostInfo.setLatitude(arg0.getLatitude());//设置 纬度
                hostInfo.setLongitude(arg0.getLongitude());//设置 经度

                if (updateFlag) {
                    /* 发送自己的定位信息 */
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            if(firstLocation){
                                /* 向服务器发送自己的位置*/
                                sendLocation();
                                /* 显示自己的定位位置*/
                                toMyLoacation();
                                firstLocation = false;
                            }else{
                                updateLocation();
                            }
                        }
                     }).start();
                }
            }
        };

        mAMap.setOnMarkerClickListener(new OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(Marker arg0) {
                // TODO Auto-generated method stub
                curSelectMarker = arg0;
                Debugs.d(TAG, "onMarkerClick =  " + (curSelectMarker==null) );
                showUserMsg((UserInfo)arg0.getObject());
                
                return false;
            }
        });
        

        mAMap.setOnMapClickListener(new OnMapClickListener() {
            
            @Override
            public void onMapClick(LatLng arg0) {
                // TODO Auto-generated method stub
                curSelectMarker = null;
                mMsgView.setVisibility(View.INVISIBLE);
                mSendMsgView.setVisibility(View.INVISIBLE);
            }
        }) ;

        mAMap.setMyLocationEnabled(true);

        mAMap.getUiSettings().setZoomControlsEnabled(false);// 设置系统默认缩放按钮可见
        mAMap.getUiSettings().setMyLocationButtonEnabled(false);
    }
    
    /**
     * 显示该用户的消息
     * @param info
     */
    private void showUserMsg(UserInfo info){
        //动画消息条弹出
        mSendMsgView.setVisibility(View.VISIBLE);
        mMsgEditText.setText("");
        
        /*  显示消息*/
        if(info.getMsgNum() > 0){
            mMsgTitle.setText("来自："+info.getUsername());
            mMsgView.setVisibility(View.VISIBLE);
            mMsgList.clear();
            mMsgList.addAll(info.getMsgList());
            mMsgAdapter.notifyDataSetChanged();
            
            /* 清楚已经显示的消息，更新显示*/
            info.cleanMsgList();
            updateUserMarker(info);
        }
    }
    
    private void sendMsg(){
        
        String did;
        
        if(curSelectMarker == null || mBroadMsgFlag){
            did = "-1";
        }else{
            UserInfo userInfo = (UserInfo) curSelectMarker.getObject();
            did = userInfo.getUid() + "";
        }
        
        Debugs.d(TAG, "did =" + did );
        
        HostUser hostInfo = HostUser.getHostUserInstance();
        
        final List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("sid", hostInfo.getUid() + ""));
        params.add(new BasicNameValuePair("did",  did));
        params.add(new BasicNameValuePair("msg", mMsgEditText.getText().toString()));
        
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                // TODO Auto-generated method stub
                Utility.httpPostRequestNoBack(Constants.URL_SEND_MSG, params);
            }
        }).start();  
        
        
        mMsgView.setVisibility(View.INVISIBLE);
        mSendMsgView.setVisibility(View.INVISIBLE);
    }

    /**
     * 地图Activity,消息处理Handler
     */
    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub

            switch (msg.what) {
            case 1:
                saveUserInfoList((String) msg.obj);
                break;
            }

            super.handleMessage(msg);
        }
    };
    
    /**
     * 登录
     */
    private void sendLocation(){
        HostUser hostInfo = HostUser.getHostUserInstance();
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        params.add(new BasicNameValuePair("uid", hostInfo.getUid() + ""));
        params.add(new BasicNameValuePair("lat", Double.toString(hostInfo
                .getLatitude())));
        params.add(new BasicNameValuePair("lon", Double.toString(hostInfo
                .getLongitude())));
        params.add(new BasicNameValuePair("online",
                hostInfo.isOnline() ? "true" : "false"));
        
        Debugs.d(TAG, "isOnline = " + hostInfo.isOnline());

        String ret = Utility.httpPostRequest(Constants.URL_LOCATION, params);
        Debugs.d(TAG, "ret=" + ret);
        /**
         * 登陆成功后返回所有在线用户的Json,然后解析Json保存UserInfo，地图标记
         * 
         */
        Message msgMessage = new Message();
        msgMessage.what = 1;
        msgMessage.obj = ret;
        mHandler.sendMessage(msgMessage);
        
    }
    
    /**
     * 向服务器发送自己的定位信息
     */
    private void updateLocation() {

        HostUser hostInfo = HostUser.getHostUserInstance();
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        params.add(new BasicNameValuePair("uid", hostInfo.getUid() + ""));
        params.add(new BasicNameValuePair("lat", Double.toString(hostInfo
                .getLatitude())));
        params.add(new BasicNameValuePair("lon", Double.toString(hostInfo
                .getLongitude())));
        params.add(new BasicNameValuePair("online",
                hostInfo.isOnline() ? "true" : "false"));

        Log.d(TAG, "params=" + params.toString());
        
        Utility.httpPostRequestNoBack(Constants.URL_UPDATE_LOCATION, params);
    }
    
    
    /**
     * 退出
     */
    private void logout(){
        
        HostUser hostUser = HostUser.getHostUserInstance();
        hostUser.setOnline(false);
        
        /* 初始化POST参数 */
        final List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("uid", hostUser.getUid()));
        params.add(new BasicNameValuePair("online", hostUser.isOnline() ? "true" : "false"));
        
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                // TODO Auto-generated method stub
                Utility.httpPostRequestNoBack(Constants.URL_LOGOUT, params);
            }
        }).start();  
    }
    
    /**
     * 上线后，根据返回的Json数据，添加在线用户列表
     * @param data
     */
    private void saveUserInfoList(String data) {
        Gson gson = new Gson();

        ArrayList<UserInfo> list = gson.fromJson(data,
                new TypeToken<ArrayList<UserInfo>>() {
                }.getType());
        
        if (list == null) {
            Debugs.e(TAG, "Json 解析出错");
            return;
        }
        
        for (UserInfo info : list) {
            mUserInfoMap.put(info.getUid(), info);
        }
        
        // 更新所有用户地图Marker的显示
        addAllUserMarker();
    }
   
    /**
     * 为所有用户，添加Marker，并显示
     */
    private void addAllUserMarker() {

        if (AMapUtil.checkReady(this, mAMap)) {
            mAMap.clear();
            
            Iterator<String> keys = mUserInfoMap.keySet().iterator();
            
            while(keys.hasNext()){
                UserInfo  info = mUserInfoMap.get(keys.next()); 
                updateUserMarker(info);
            }
        }
    }
    
    /**
     * 更新用户Marker显示内容
     * @param info
     */
    private void updateUserMarker(UserInfo info){

        /* 如果已经添加Marker，则从地图中删除*/
        if(info.getMarker() != null){
            info.getMarker().remove();
        }
        
        /* 根据用户信息添加新Marker*/
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions
                .position(info.getLatLng())
                .icon(getUserBitmapDescriptor(info));
        
        Marker marker = mAMap.addMarker(markerOptions);
        
        //将info作为marker的附加值，方便根据marker获取对象。
        marker.setObject(info);
        
        info.setMarker(marker);
    }
    
    /**
     * 显示Host的定位位置
     */
    private void  toMyLoacation(){
        
        HostUser hoseUser = HostUser.getHostUserInstance();
        
        CameraPosition hostPos = new CameraPosition.Builder()
        .target(hoseUser.getLatLng()).zoom(16).bearing(0).build();
        
        changeCamera(CameraUpdateFactory.newCameraPosition(hostPos));
    }
    
    /**
     * 根据动画按钮状态，调用函数animateCamera或moveCamera来改变可视区域
     */
    private void changeCamera(CameraUpdate update) {
        mAMap.animateCamera(update, 1000, new CancelableCallback() {
            
            @Override
            public void onFinish() {
                // TODO Auto-generated method stub
            }
            
            @Override
            public void onCancel() {
                // TODO Auto-generated method stub
            }
        });
    }
    
    
    /**
     * 根据用户信息，生成Marker的显示Bitmap
     * @param info
     * @return
     */
    private Paint  mPaint = new Paint();
    
    private BitmapDescriptor getUserBitmapDescriptor(UserInfo info) {
        int w = 82;
        int h = 104;
        Log.d("majin", "info.getUsername()=="+info.toString());
        Resources  res = this.getBaseContext().getResources(); 

        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvasTemp = new Canvas(bmp);        
        
        int backID = info.getMsgNum() > 0 ? R.drawable.marker_back_2 : R.drawable.marker_back_1;
        Bitmap backBitmap = BitmapFactory.decodeResource(res , backID); 
        backBitmap = Bitmap.createScaledBitmap(backBitmap, w, h, false);
        
        Bitmap haedBitmap;
        if(info.getPic() == null){
            haedBitmap  = BitmapFactory.decodeResource(this.getBaseContext().getResources(), R.drawable.default_pic);    
        }else{
            haedBitmap =BitmapFactory.decodeFile(info.getPic());
        }
        haedBitmap = Bitmap.createScaledBitmap(haedBitmap, w-4, w-4, false);
        
        canvasTemp.drawBitmap(haedBitmap, 2, 2, mPaint);
        canvasTemp.drawBitmap(backBitmap, 0, 0, mPaint);

        //mPaint.setColor(Color.LTGRAY);
        mPaint.setTextSize(15);
        mPaint.setColor(Color.WHITE);
        
        Debugs.d(TAG,"info ="+info.toString());
        Log.d("majin", "info.getUsername()=="+info.getUsername());
        canvasTemp.drawText(info.getUsername(), 5, 78, mPaint);
        if(info.getMsgNum() > 0){
            canvasTemp.drawText(Integer.toString(info.getMsgNum()), w-12, 15, mPaint);
        }

        return BitmapDescriptorFactory.fromBitmap(bmp);
    }
    
    /** 
     * 退出Dialog
     */
    private void quitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("确定要退出?")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BasicMapActivity.this.finish();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
        }).show();
    }
}
