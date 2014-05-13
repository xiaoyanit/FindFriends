/**
 * 
 */
package com.leochin.findfriends.util;

import java.text.DecimalFormat;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.search.core.LatLonPoint;
import com.leochin.findfriends.data.ChString;

/*******
 * @project AMapV2Demos
 * @email chuan.yu@autonavi.com
 * @time 2013-3-26下午7:03:47
 *******/
public class AMapUtil {
	
	/**
	 * 对AMap对象判断是否为null
	 */
	public static boolean checkReady(Context context, AMap aMap) {
		if (aMap == null) {
			Debugs.toast(context, "地图不可用!");
			return false;
		}
		return true;
	}

	public static Spanned stringToSpan(String src) {
		return src == null ? null : Html.fromHtml(src.replace("\n", "<br />"));
	}

	public static String colorFont(String src, String color) {
		StringBuffer strBuf = new StringBuffer();

		strBuf.append("<font color=").append(color).append(">").append(src)
				.append("</font>");
		return strBuf.toString();
	}

	public static String makeHtmlNewLine() {
		return "<br />";
	}

	public static String makeHtmlSpace(int number) {
		final String space = "&nbsp;";
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < number; i++) {
			result.append(space);
		}
		return result.toString();
	}

	public static String getFriendlyLength(int lenMeter) {
		if (lenMeter > 10000) // 10 km
		{
			int dis = lenMeter / 1000;
			return dis + ChString.Kilometer;
		}

		if (lenMeter > 1000) {
			float dis = (float) lenMeter / 1000;
			DecimalFormat fnum = new DecimalFormat("##0.0");
			String dstr = fnum.format(dis);
			return dstr + ChString.Kilometer;
		}

		if (lenMeter > 100) {
			int dis = lenMeter / 50 * 50;
			return dis + ChString.Meter;
		}

		int dis = lenMeter / 10 * 10;
		if (dis == 0) {
			dis = 10;
		}

		return dis + ChString.Meter;
	}
	public static boolean IsEmptyOrNullString(String s) {
		return (s == null) || (s.trim().length() == 0);
	}
	
	public static LatLonPoint convertToLatLonPoint(LatLng latlon){
		return new LatLonPoint(latlon.latitude,latlon.longitude);
	}

	public static final String HtmlBlack = "#000000";
	public static final String HtmlGray = "#808080";
}
