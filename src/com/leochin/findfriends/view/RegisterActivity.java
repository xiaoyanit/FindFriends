package com.leochin.findfriends.view;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.leochin.findfriends.R;
import com.leochin.findfriends.data.Constants;
import com.leochin.findfriends.util.Debugs;
import com.leochin.findfriends.util.Utility;

public class RegisterActivity extends Activity {

    private final static String TAG = "RegisterActivity";
    
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
	
	public final static int HANDLE_ID_REGIST_FAILURE_USER = 4;
	public final static int HANDLE_ID_REGIST_FAILURE_EMAIL = 5;
	public final static int HANDLE_ID_REGIST_SUCCESS = 6;
	

	Handler registerHandler = new Handler() {

		public void handleMessage(Message msg) {
		    
		    if(mProDialog != null){
	              mProDialog.dismiss();
		    }
		    
		    switch(msg.what){
		    case LoginActivity.HANDLE_ID_NET_UNAVAILABLE:
		        Toast.makeText(RegisterActivity.this,
                        R.string.login_net_unavailable, Toast.LENGTH_LONG).show();
		        break;
		    case LoginActivity.HANDLE_ID_NET_UNRESPOND:
		        Toast.makeText(RegisterActivity.this,
                        R.string.login_net_unrespond, Toast.LENGTH_LONG).show();
		        break;
		    case HANDLE_ID_REGIST_FAILURE_USER:
		        Toast.makeText(RegisterActivity.this,
                        "注册出现异常，该用户名已被注册!", Toast.LENGTH_LONG).show();
		        break;
		    case HANDLE_ID_REGIST_FAILURE_EMAIL:  
	            Toast.makeText(RegisterActivity.this,
	                    "注册出现异常，该Email已被注册!", Toast.LENGTH_LONG).show();
		        break;
		    case HANDLE_ID_REGIST_SUCCESS:
		        
	            saveSharePreferences(true, true);
		        /*
			        Intent intent = new Intent(RegisterActivity.this, BasicMapActivity.class);
		            Bundle bundle = new Bundle();
		            bundle.putString("MAP_USERNAME", mUserName);
		            intent.putExtras(bundle);
		            startActivity(intent);
		        */
	            
	            Toast.makeText(RegisterActivity.this, "注册成功,正在跳转到登陆....", 0).show();
//	            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
//	            startActivity(intent);
	            RegisterActivity.this.finish();
		        break;
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

	/**
	 * 
	 * 注册按钮点击
	 * 
	 * */
	private OnClickListener submitListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			String username = mUserNameEdit.getText().toString();
			String email = mEmailEdit.getText().toString();
			String password = mPasswordEdit.getText().toString();
			String passwordConfirm = mPasswordConfirmEdit.getText().toString();

            if(!Utility.isNetWorkConnected(RegisterActivity.this)){
                registerHandler.sendEmptyMessage(LoginActivity.HANDLE_ID_NET_UNAVAILABLE);
                return;
            }

            validateForm(username, email, password, passwordConfirm);
            
			if (mSuggest.length() == 0) {

				mProDialog = ProgressDialog.show(RegisterActivity.this,
						"注册中..", "连接中..请稍后....", true, true);

				Thread registerThread = new Thread(new RegisterRunnable());
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

		    if(hasFocus){
		        return;
		    }
		    
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
			Toast.makeText(this,
					mSuggest.subSequence(0, mSuggest.length() - 1),
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

	private void saveSharePreferences(boolean saveUserName, boolean savePassword) {

		Editor editor = Utility.getEditor(RegisterActivity.this,
				Constants.SHARE_LOGIN_INFO);

		if (saveUserName) {
			editor.putString(Constants.SHARE_LOGIN_USERNAME, mUserNameEdit
					.getText().toString());
		}
		if (savePassword) {
			editor.putString(Constants.SHARE_LOGIN_PASSWORD, mPasswordEdit
					.getText().toString());
		}

		editor.commit();
	}

	class RegisterRunnable implements Runnable {
		@Override
		public void run() {
    		mUserName = mUserNameEdit.getText().toString();
    		mEmail = mEmailEdit.getText().toString();
    		mPassword = mPasswordEdit.getText().toString();
    		
    		validateRegister(mUserName, mEmail, mPassword, Constants.URL_REGIST);
		}
	}

    private void validateRegister(String userName, String email,
            String password, String validateUrl) {

        Debugs.d(TAG,"username:" + userName+" password:" + password);

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("username", userName));
        params.add(new BasicNameValuePair("password", password));
        params.add(new BasicNameValuePair("email", email));
        params.add(new BasicNameValuePair("version", "majin"));

        mResultString  = Utility.httpPostRequest(validateUrl, params);

        Debugs.d(TAG,"mResultString=" + mResultString);
        
        if(mResultString.equals("successful")){
            registerHandler.sendEmptyMessage(HANDLE_ID_REGIST_SUCCESS);
        }else if(mResultString.contains("Username")){
            registerHandler.sendEmptyMessage(HANDLE_ID_REGIST_FAILURE_USER);
        }else if(mResultString.contains("Email")){
            registerHandler.sendEmptyMessage(HANDLE_ID_REGIST_FAILURE_EMAIL);
        }
    }
}
