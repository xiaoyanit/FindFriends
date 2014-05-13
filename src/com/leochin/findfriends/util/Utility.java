package com.leochin.findfriends.util;

import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * 
 * @author Mr.Wen <br />
 * @version 1.0 <br />
 * @created 2013-5-6 下午2:45:37 <br />
 * 
 */

public class Utility {

	public static SharedPreferences getSharedPreferences(Context context, String name){

		SharedPreferences sp = context.getSharedPreferences(name,
				Context.MODE_PRIVATE);
		return sp;
	}
	
	public static Editor getEditor(Context context, String name){
		
		return getSharedPreferences(context, name).edit();	
	}
	
	public static void setSharedInt(Context context, String name, String key,
			int value) {

		Editor editor = getEditor(context, name);
		editor.putInt(key, value);
		editor.commit();
	}

	public static void setSharedString(Context context, String name,
			String key, String value) {

		Editor editor = getEditor(context, name);
		editor.putString(key, value);
		editor.commit();
	}

	public static int getSharedInt(Context context, String name, String key) {

		return getSharedPreferences(context, name).getInt(key, 0);
	}

	public static String getSharedString(Context context, String name, String key) {

		return getSharedPreferences(context, name).getString(key, "");
	}
	
	/**
	 * 判断当前是否联网 需要添加权限：<uses-permission
	 * android:name="android.permission.ACCESS_NETWORK_STATE" />
	 * 
	 * @param Activity
	 *            调用网络检测的context
	 * @return true 联网成功，false 联网失败
	 * */

	public static boolean isNetWorkConnected(Context context) {

		ConnectivityManager connect = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connect.getActiveNetworkInfo();

		return (info != null) && (info.isConnectedOrConnecting());
	}
	
	/**
	 * httpPostRequest请求，成功返回数据，失败返回为空的String
	 */
	public static String httpPostRequest(String url , List<NameValuePair> params){
	    
        String resultString = "";
	    
	    try {

	        HttpPost httpRequest = new HttpPost(url); // 使用Apache的网络库
	        
            httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            
            HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);
            Debugs.d("leochin", "url="+url +"--1->"+httpResponse.getStatusLine());
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                resultString = EntityUtils.toString(httpResponse.getEntity()); // 服务器返回的数据
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
	    return resultString;
	}
	
	/**
     * httpPostRequest请求，成功返回数据，失败返回为空的String
     */
    public static void httpPostRequestNoBack(String url , List<NameValuePair> params){
        try {

            HttpPost httpRequest = new HttpPost(url); // 使用Apache的网络库
            httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            HttpResponse rp = new DefaultHttpClient().execute(httpRequest);
            Debugs.d("leochin", "url="+url +"--2->"+rp.getStatusLine());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
