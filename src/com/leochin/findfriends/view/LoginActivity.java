package com.leochin.findfriends.view;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.leochin.findfriends.R;
import com.leochin.findfriends.data.Constants;
import com.leochin.findfriends.data.HostUser;
import com.leochin.findfriends.data.UserInfo;
import com.leochin.findfriends.push.MQTTService;
import com.leochin.findfriends.util.Debugs;
import com.leochin.findfriends.util.Utility;

public class LoginActivity extends Activity {

    private final static String TAG = "LoginActivity";
    
    private String mUserName = null;
    private String mPassword = null;

    private EditText mUserNameEdit = null;
    private EditText mPasswordEdit = null;
    private CheckBox mRememberMe = null;
    private ProgressDialog mProDialog;

    public final static int HANDLE_ID_NET_UNAVAILABLE = 0;
    public final static int HANDLE_ID_NET_UNRESPOND = 1;
    public final static int HANDLE_ID_LOGIN_FAILURE = 2;
    public final static int HANDLE_ID_LOGIN_SUCCESS = 3;
    
    private static Handler mHandler;
   
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /* 初始化View设置*/
        initView();
        
        /* 初始化Handler处理*/
        initHandler();
        
        /* 初始化View数据显示*/
        initViewData();
    }

    /**
     * 初始化View设置
     */
    private void initView(){
        mUserNameEdit = (EditText) findViewById(R.id.ID_LOGIN_USERNAME_EDIT);
        mPasswordEdit = (EditText) findViewById(R.id.ID_LOGIN_PASSWORD_EDIT);
        mRememberMe = (CheckBox) findViewById(R.id.ID_LOGIN_REMEMBER_BOX);
        
        /*
         * CheckBox控件监听器 建议： 应当做一个全局变量，
         * 获取当前状态时候要将账号和密码写到preference中
         */
        mRememberMe.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                if (isRememberMe()) {
                    Toast.makeText(LoginActivity.this, "如果登录成功,以后账号和密码会自动输入!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        /* 设置‘登录’事件处理*/
        findViewById(R.id.ID_LOGIN_SUBMIT_BUTTON).setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                mProDialog = ProgressDialog.show(LoginActivity.this, "连接中..",
                        "连接中..请稍后....", true, true);

                Thread loginThread = new Thread(new LoginRunnable());
                loginThread.start();
            }
        });
        
        /* 设置‘注册’事件处理*/
        findViewById(R.id.ID_LOGIN_REGISTER_BUTTON).setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent();
                intent.setClass(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * 初始化Handler处理
     */
    private void initHandler(){
        mHandler = new Handler() {

            public void handleMessage(Message msg) {

                if (mProDialog != null) {
                    mProDialog.dismiss();
                }

                switch (msg.what) {
                case HANDLE_ID_NET_UNAVAILABLE:
                    Toast.makeText(LoginActivity.this, R.string.login_net_unavailable, Toast.LENGTH_SHORT)
                            .show();
                    clearSharePassword();
                    break;
                    
                case HANDLE_ID_NET_UNRESPOND:
                    Toast.makeText(LoginActivity.this, R.string.login_net_unrespond, Toast.LENGTH_SHORT)
                            .show();
                    clearSharePassword();
                    break;

                case HANDLE_ID_LOGIN_FAILURE:
                    Toast.makeText(LoginActivity.this, R.string.login_login_failure, Toast.LENGTH_SHORT).show();
                    clearSharePassword();
                    break;

                case HANDLE_ID_LOGIN_SUCCESS:
                    /* 保存用户信息 */
                    saveHostUserInfo();
                    /* 设置Host用户信息*/
                  boolean flag =   setHostUserInfo((String)msg.obj);
                  if(!flag){
                	  Toast.makeText(LoginActivity.this, "登陆异常!请重新登陆!", 1).show();
                	  break;
                  }
                    /* 跳转到地图 */
                    gotoMapActivity();
                    /* 启动推送服务*/
                    startPushService();
                    break;
                }
            }
        };
    }

    
    /**
     * 获取SharedPreferences当中的用户信息，初始化View数据显示
     */
    private void initViewData() {

        String username = Utility.getSharedString(LoginActivity.this,
                Constants.SHARE_LOGIN_INFO, Constants.SHARE_LOGIN_USERNAME);
        String password = Utility.getSharedString(LoginActivity.this,
                Constants.SHARE_LOGIN_INFO, Constants.SHARE_LOGIN_PASSWORD);

        Debugs.d(TAG, "u =" + username  + " p = " + password);
        
        if (!"".equals(username)) {
            mUserNameEdit.setText(username);
        }
        if (!"".equals(password)) {
            mPasswordEdit.setText(password);
            mRememberMe.setChecked(true);
        }
    }
    

    /**
     * 内部类 实现在其他线程中的网络登录
     */
    class LoginRunnable implements Runnable {

        @Override
        public void run() {

            /* 网络不可用，且不向下执行 */
            if (!Utility.isNetWorkConnected(LoginActivity.this)) {
                mHandler.sendEmptyMessage(HANDLE_ID_NET_UNAVAILABLE);
                return;
            }

            /* 获取用户名和密码 */
            mUserName = mUserNameEdit.getText().toString();
            mPassword = mPasswordEdit.getText().toString();

            /* 登录*/
            validateLogin(mUserName, mPassword, Constants.URL_LOGIN) ;
        }
    }

    /**
     * 连接网络，进行登录
     * @return 登录状态
     */
    private void  validateLogin(String username, String password, String url) {
        
        String resultString = "";
        
        /* 初始化POST参数 */
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("username", username));
        params.add(new BasicNameValuePair("password", password));
        params.add(new BasicNameValuePair("version", "leochin"));
        resultString = Utility.httpPostRequest(url, params);
        
        if(resultString.equals("")){
            mHandler.sendEmptyMessage(HANDLE_ID_NET_UNRESPOND);
        }else if(resultString.contains("error")){
            mHandler.sendEmptyMessage(HANDLE_ID_LOGIN_FAILURE);
        }else {
            mHandler.sendMessage(getMessage(HANDLE_ID_LOGIN_SUCCESS, resultString));
        }
    }
    
    /**
     * 将账号、密码写入sharedPreference
     */
    private void saveSharePreferences(boolean saveUserName, boolean savePassword) {

        Debugs.d(TAG, "save share " +mUserNameEdit
                .getText().toString()  + " " +  mPasswordEdit
                .getText().toString());
        
        Editor edit = Utility.getEditor(LoginActivity.this,
                Constants.SHARE_LOGIN_INFO);

        if (saveUserName) {
            edit.putString(Constants.SHARE_LOGIN_USERNAME, mUserNameEdit
                    .getText().toString());
        }

        if (savePassword) {
            edit.putString(Constants.SHARE_LOGIN_PASSWORD, mPasswordEdit
                    .getText().toString());
        }

        edit.commit();
    }

    /**
     * 将SharedPreferences当中的密码清空
     */
    private void clearSharePassword() {

        Utility.setSharedString(LoginActivity.this, Constants.SHARE_LOGIN_INFO,
                Constants.SHARE_LOGIN_PASSWORD, "");
    }

    /**
     * 获取CheckBox的当前状态
     */
    private boolean isRememberMe() {

        if (mRememberMe.isChecked()) {
            return true;
        }

        return false;
    }

    /**
     * 获取一个Message对象
     */
    private Message getMessage(int id, String content) {
        Message message = Message.obtain();
        message.what = id;
        message.obj = content;
        return message;
    }

    /**
     * 保存主机用户信息
     */ 
    private void saveHostUserInfo() {
        if (isRememberMe()) {
            saveSharePreferences(true, true);
        } else {
            saveSharePreferences(true, false);
        }
    }

    /**
     * 登录成功，跳转到地图Activity
     */
    private void gotoMapActivity() {
        Intent intent = new Intent(LoginActivity.this, BasicMapActivity.class);

        Bundle bundle = new Bundle();
        bundle.putString("MAP_USERNAME", mUserName);
        intent.putExtras(bundle);

        startActivity(intent);
        this.finish();
    }
    
    private boolean setHostUserInfo(String data){
        
        Debugs.d(TAG, "data="+ data);
        /* 解析Json数据，为对象*/
        Gson gson = new Gson();
        UserInfo user = gson.fromJson(data, new TypeToken<UserInfo>(){}.getType());
        
        /* 设置HostUser的信息*/
        HostUser hostUser = HostUser.getHostUserInstance();
        hostUser.setUserInfo(user);
        Debugs.d("leochin","登陆成功:"+hostUser.getUid());
        hostUser.setOnline(true);
        
        Debugs.d(TAG, "online" + hostUser.isOnline());
        
        if(hostUser.getUid()==null){
    		return false;
        }
        else{
        	return true;
        }

    }
    
    private void startPushService(){
        Debugs.d(TAG, "startPushService .... ");
        Intent svc = new Intent(this, MQTTService.class);
        
        svc.putExtra(MQTTService.INTENT_KEY_SERVICE_ADD, 
                Constants.PUSH_SERVICE_IPADD);
        svc.putExtra(MQTTService.INTENT_KEY_TOPIC, 
                HostUser.getHostUserInstance().getUsername());
        
        startService(svc);
    }
}
