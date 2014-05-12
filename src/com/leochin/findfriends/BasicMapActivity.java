/**
 * 
 * FileName BasicMapActivity.java  <br />
 * @author Mr.Wen <br />
 * @version 1.0   <br />
 * @created 2013-5-6 上午10:37:08 <br />
 * 
 */
package com.leochin.findfriends;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.amap.api.maps.AMap;
import com.amap.api.maps.SupportMapFragment;
import com.leochin.findfriends.util.AMapUtil;

/**
 * 
 * @author Mr.Wen <br />
 * @version 1.0   <br />
 * @created 2013-5-6 上午10:37:08 <br />
 * 
 */
public class BasicMapActivity extends FragmentActivity{
	
	private AMap mAMap = null;

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.basic_map);
		
		init();
	}
	
	/**
	 * 初始化AMap对象
	 */
	private void init() {
		
		if (mAMap == null) {
			/* 获取Amap对象 */
			mAMap = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
			
			if (AMapUtil.checkReady(this, mAMap)) {
				setUpMap();
			}
		}
	}

	private void setUpMap() {

		//mAMap.getUiSettings().setZoomControlsEnabled(true);// 设置系统默认缩放按钮可见
	}

	
}
