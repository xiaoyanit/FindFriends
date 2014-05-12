package com.leochin.findfriends;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

import com.leochin.findfriends.data.Constants;
import com.leochin.findfriends.util.Utility;

///git  test

public class MainActivity extends Activity {

	private String mUserName = null;
	private String mPassword = null;

	private EditText mUserNameEdit = null;
	private EditText mPasswordEdit = null;
	private CheckBox mRememberMe = null;
	private Button mSubmitButton = null;
	private Button mRegisterButton = null;
	private ProgressDialog mProDialog;

	private boolean isNetworkAvailable = false;

	Handler mLoginHandler = new Handler() {

		public void handleMessage(Message msg) {

			isNetworkAvailable = (Boolean) msg.obj;

			if (mProDialog != null) {

				mProDialog.dismiss();
			}

			if (!isNetworkAvailable) {

				Toast.makeText(MainActivity.this,
						"登陆失败:\n1.请检查您网络连接.\n2.请联系我们.!", Toast.LENGTH_SHORT)
						.show();
			} else {

				Toast.makeText(MainActivity.this, "登陆失败,请输入正确的用户名和密码!",
						Toast.LENGTH_SHORT).show();
				clearSharePassword();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mUserNameEdit = (EditText) findViewById(R.id.ID_LOGIN_USERNAME_EDIT);
		mPasswordEdit = (EditText) findViewById(R.id.ID_LOGIN_PASSWORD_EDIT);
		mRememberMe = (CheckBox) findViewById(R.id.ID_LOGIN_REMEMBER_BOX);
		mSubmitButton = (Button) findViewById(R.id.ID_LOGIN_SUBMIT_BUTTON);
		mRegisterButton = (Button) findViewById(R.id.ID_LOGIN_REGISTER_BUTTON);

		mSubmitButton.setOnClickListener(submitListener);
		mRegisterButton.setOnClickListener(registerListener);
		mRememberMe.setOnCheckedChangeListener(rememberMeListener);

		initView();
	}

	/**
	 * 
	 * 在Edit当中显示，上一次输入的账号
	 * 
	 * */
	private void initView() {

		String username = Utility.getSharedString(MainActivity.this,
				Constants.SHARE_LOGIN_USERNAME, "");
		String password = Utility.getSharedString(MainActivity.this,
				Constants.SHARE_LOGIN_PASSWORD, "");

		Log.d(this.toString(), "userName=" + username + " password=" + password);

		/*
		 * 获取SharedPreferences当中的用户信息 存在则将信息显示在EditText中
		 */
		if (!"".equals(username)) {
			mUserNameEdit.setText(username);
		}
		if (!"".equals(password)) {
			mPasswordEdit.setText(password);
			mRememberMe.setChecked(true);
		}

	}

	/**
	 * 登录按钮监听器
	 * 
	 * */
	private OnClickListener submitListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			mProDialog = ProgressDialog.show(MainActivity.this, "连接中..",
					"连接中..请稍后....", true, true);

			Thread loginThread = new Thread(new LoginRunnable());
			loginThread.start();
		}
	};

	/**
	 * 注册按钮监听器
	 * 
	 * 跳转到注册页面
	 * 
	 * */
	private OnClickListener registerListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			Intent intent = new Intent();
			intent.setClass(MainActivity.this, RegisterActivity.class);
			startActivity(intent);
		}
	};

	/**
	 * CheckBox控件监听器
	 * 
	 * 建议： 应当做一个全局变量，获取当前状态时候要将账号和密码写到preference中
	 * 
	 * */
	private OnCheckedChangeListener rememberMeListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {

			if (isRememberMe()) {
				Toast.makeText(MainActivity.this, "如果登录成功,以后账号和密码会自动输入!",
						Toast.LENGTH_SHORT).show();
			}
		}
	};

	/**
	 * 
	 * 内部类 实现在其他线程中的网络登录
	 * 
	 * */
	class LoginRunnable implements Runnable {

		@Override
		public void run() {

			/* 判断网络是否可用 */
			isNetworkAvailable = Utility.isNetWorkConnected(MainActivity.this);

			if (!isNetworkAvailable) {

				/*
				 * 网络不可用则发送Handler给UI Thread
				 * 
				 * 网络不可用，且不向下执行
				 */
				Message message = Message.obtain();
				message.obj = isNetworkAvailable;
				mLoginHandler.sendMessage(message);
				return;
			}

			/* 获取用户名和密码 */
			mUserName = mUserNameEdit.getText().toString();
			mPassword = mPasswordEdit.getText().toString();

			boolean loginState = validateLogin(mUserName, mPassword,
					Constants.LOGIN_URL);

			System.out.println("login state:" + loginState);

			if (loginState) {

				Intent intent = new Intent(MainActivity.this,
						BasicMapActivity.class);

				Bundle bundle = new Bundle();
				bundle.putString("MAP_USERNAME", mUserName);
				intent.putExtras(bundle);

				startActivity(intent);
				mProDialog.dismiss();
			} else {

				Message message = Message.obtain();
				message.obj = isNetworkAvailable;
				mLoginHandler.sendMessage(message);
			}
		}

	}

	/**
	 * 
	 * 连接网络，进行登录
	 * 
	 * */
	private boolean validateLogin(String username, String password, String url) {

		boolean loginState = false;
		String resultString = null;

		HttpPost httpRequest = new HttpPost(url);  // 使用Apache的网络库

		/* 初始化POST参数 */
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("username", username));
		params.add(new BasicNameValuePair("password", password));

		try {

			httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			HttpResponse httpResponse = new DefaultHttpClient()
					.execute(httpRequest);

			if (httpResponse.getStatusLine().getStatusCode() == 200) {

				resultString = EntityUtils.toString(httpResponse.getEntity()); //服务器返回的数据
				System.out.println("strResult: " + resultString);
			}

		} catch (Exception e) {

			e.printStackTrace();
		}

		System.out.println("strResult:" + resultString);

		if(resultString == null){
			return false;
		}
		
		if (resultString
				.equals("<script>window.location.href='index.php'</script>")) {
			loginState = true;
		}

		if (loginState) {

			if (isRememberMe()) {

				saveSharePreferences(true, true);
			} else {

				saveSharePreferences(true, false);
			}

		} else {

			if (!isNetworkAvailable) {
				clearSharePassword();
			}
		}

		if (!isRememberMe()) {
			clearSharePassword();
		}

		return loginState;
	}

	/**
	 * 
	 * 将账号、密码写入sharedPreference
	 * 
	 * */
	private void saveSharePreferences(boolean saveUserName, boolean savePassword) {

		Editor edit = Utility.getEditor(MainActivity.this,
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
	 * 
	 * 将SharedPreferences当中的密码清空
	 * 
	 * */
	private void clearSharePassword() {

		Utility.setSharedString(MainActivity.this, Constants.SHARE_LOGIN_INFO,
				Constants.SHARE_LOGIN_PASSWORD, "");
	}

	/**
	 * 
	 * 获取CheckBox的当前状态
	 * 
	 * @return true 保存, false 不保存
	 * 
	 * */
	private boolean isRememberMe() {

		if (mRememberMe.isChecked()) {
			return true;
		}

		return false;
	}

}
