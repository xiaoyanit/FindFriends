package com.sunplusedu.findfriends;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.sunplusedu.findfriends.util.Constants;

public class RegisterActivity extends Activity {

	private String mUserName = null;
	private String mEmail = null;
	private String mPassword = null;
	private String mResultString = null;

	private EditText mUserNameEdit = null;
	private EditText mEmailEdit = null;
	private EditText mPasswordEdit = null;
	private EditText mPasswordConfirmEdit = null;
	private Button mSubmitButton = null;
	private Button mReinputButton = null;
	
	private StringBuilder mSuggest = null;
	private ProgressDialog mProDialog = null;
	
	private final String SHARE_LOGIN_TAG = "MAP_SHARE_LOGIN_TAG";
	private String SHARE_LOGIN_USERNAME = "MAP_LOGIN_USERNAME";
	private String SHARE_LOGIN_PASSWORD = "MAP_LOGIN_PASSWORD";
	
	private boolean isNetworkAvailable = false;
	
	Handler registerHandler = new Handler() {
		
		public void handleMessage(Message msg) {
			
			isNetworkAvailable = msg.getData().getBoolean("isNetError");
			
			if (mProDialog != null) {
				
				mProDialog.dismiss();
			} else if (isNetworkAvailable) {
				
				Toast.makeText(RegisterActivity.this, "注册失败:\n1.请检查您网络连接.\n2.请联系我们.!",
						Toast.LENGTH_SHORT).show();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		setContentView(R.layout.activity_register);
		
		mUserNameEdit = (EditText) findViewById(R.id.ID_REGISTER_USERNAME_EDIT);
		mEmailEdit = (EditText) findViewById(R.id.ID_REGISTER_EMAIL_EDIT);
		mPasswordEdit = (EditText) findViewById(R.id.ID_REGISTER_PASSWORD_EDIT);
		mPasswordConfirmEdit = (EditText) findViewById(R.id.ID_REGISTER_PASSWORDCONFIRM_EDIT);
		mSubmitButton = (Button) findViewById(R.id.ID_REGISTER_SUBMIT_BUTTON);
		mReinputButton = (Button) findViewById(R.id.ID_REGISTER_REINPUT_BUTTON);
		
		mSubmitButton.setOnClickListener(submitListener);
		mReinputButton.setOnClickListener(clearListener);
		mEmailEdit.setOnFocusChangeListener(emailChange);
	}
	
	private OnClickListener submitListener = new OnClickListener() {
	
		@Override
		public void onClick(View v) {
			
			String userName = mUserNameEdit.getText().toString();
			String email = mEmailEdit.getText().toString();
			String password = mPasswordEdit.getText().toString();
			String passwordConfirm = mPasswordConfirmEdit.getText().toString();
			
			validateForm(userName, email, password, passwordConfirm);
			
			if (mSuggest.length() == 0) {
				
				mProDialog = ProgressDialog.show(RegisterActivity.this, "注册中..",
						"连接中..请稍后....", true, true);
				
				Thread registerThread = new Thread(new RegisterHandler());
				registerThread.start();
			}
		}
	};

	private OnClickListener clearListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			clearForm();
		}
	};
	
	private OnFocusChangeListener emailChange = new OnFocusChangeListener() {
		
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			// TODO Auto-generated method stub
			
			String strPattern = "^[a-zA-Z][\\w\\.-]*[a-zA-Z0-9]@[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]$";
			String email = mEmailEdit.getText().toString();
			Pattern p = Pattern.compile(strPattern);
			Matcher m = p.matcher(email);
			
			if (!m.matches()) {
				Toast.makeText(RegisterActivity.this, "邮箱格式错误，请重输！",
						Toast.LENGTH_SHORT).show();
			}
			
		}
	};
	
	/**
	 * 
	 * 检查帐号相关信息的格式是否正确
	 * 
	 * */
	private void validateForm(String userName, String email, String password,
			String password2) {
		
		mSuggest = new StringBuilder();
		
		if (userName.length() < 1) {
			mSuggest.append(getText(R.string.suggest_userName) + "\n");
		}
		
		if (email.length() < 1) {
			mSuggest.append(getText(R.string.suggest_email) + "\n");
		}
		
		if (password.length() < 1 || password2.length() < 1) {
			mSuggest.append(getText(R.string.suggest_passwordNotEmpty) + "\n");
		}
		
		if (!password.equals(password2)) {
			mSuggest.append(getText(R.string.suggest_passwordNotSame));
		}
		
		if (mSuggest.length() > 0) {
			Toast.makeText(this, mSuggest.subSequence(0, mSuggest.length() - 1),
					Toast.LENGTH_SHORT).show();
		}
	}

	private void clearForm() {
		mUserNameEdit.setText("");
		mEmailEdit.setText("");
		mPasswordEdit.setText("");
		mPasswordConfirmEdit.setText("");
		
		mUserNameEdit.requestFocus();
	}


	private boolean validateLocalRegister(String userName, String email,
			String password, String validateUrl) {
		
		System.out.println("username:" + userName);
		System.out.println("password:" + password);

		boolean registerState = false;

		HttpPost httpRequest = new HttpPost(validateUrl);

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("username", userName));
		params.add(new BasicNameValuePair("password", password));
		params.add(new BasicNameValuePair("email", email));

		try {

			httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));

			HttpResponse httpResponse = new DefaultHttpClient()
					.execute(httpRequest);

			if (httpResponse.getStatusLine().getStatusCode() == 200) {

				mResultString = EntityUtils.toString(httpResponse.getEntity());

				System.out.println("strResult: " + mResultString);
			}
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}

		if (mResultString
				.equals("<script>window.location.href='index.php'</script>")) {
			
			registerState = true;
		}
		if (registerState) {

			saveSharePreferences(true, true);
		}
		return registerState;
	}

	private void saveSharePreferences(boolean saveUserName, boolean savePassword) {
		
		SharedPreferences share = getSharedPreferences(SHARE_LOGIN_TAG, 0);
		Editor editor = share.edit();
		
		if (saveUserName) {
			
			editor.putString(SHARE_LOGIN_USERNAME,
							mUserNameEdit.getText().toString());
		}
		
		if (savePassword) {
			
			editor.putString(SHARE_LOGIN_PASSWORD,
							mPasswordEdit.getText().toString());
		}
		
		editor.commit();
	}

	class RegisterHandler implements Runnable {
		@Override
		public void run() {
			
			mUserName = mUserNameEdit.getText().toString();
			mEmail = mEmailEdit.getText().toString();
			mPassword = mPasswordEdit.getText().toString();
			
			String validateURL = Constants.USER_URL;
			boolean registerState = validateLocalRegister(mUserName, mEmail,
					mPassword, validateURL);
			
			Log.d(this.toString(), "validateRegister");

			System.out.println("register state:" + registerState);
		
			if (registerState) {
				
				Intent intent = new Intent();
				intent.setClass(RegisterActivity.this, BasicMapActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("MAP_USERNAME", mUserName);
				intent.putExtras(bundle);
				startActivity(intent);
				mProDialog.dismiss();
			} else {

				Message message = new Message();
				Bundle bundle = new Bundle();
				bundle.putBoolean("isNetError", isNetworkAvailable);
				message.setData(bundle);
				registerHandler.sendMessage(message);
			}
		}
	}
}
