package com.leochin.findfriends.data;

import com.amap.api.maps.model.LatLng;

public class Constants {

	public static final int POISEARCH = 1000;

	public static final int ERROR = 1001;
	public static final int FIRST_LOCATION = 1002;

	public static final int ROUTE_START_SEARCH = 2000;
	public static final int ROUTE_END_SEARCH = 2001;
	public static final int ROUTE_SEARCH_RESULT = 2002;
	public static final int ROUTE_SEARCH_ERROR = 2004;

	public static final int REOCODER_RESULT = 3000;
	public static final int DIALOG_LAYER = 4000;
	public static final int POISEARCH_NEXT = 5000;

	public static final int BUSLINE_RESULT = 6000;
	public static final int BUSLINE_DETAIL_RESULT = 6001;
	public static final int BUSLINE_ERROR_RESULT = 6002;

	public static final LatLng BEIJING = new LatLng(39.90403, 116.407525);// 北京市经纬度
	public static final LatLng ZHONGGUANCUN = new LatLng(39.983456, 116.3154950);// 北京市中关村经纬度
	public static final LatLng SHANGHAI = new LatLng(31.239879, 121.499674);// 上海市经纬度
	public static final LatLng FANGHENG = new LatLng(39.991014, 116.482763);// 方恒国际中心经纬度
	public static final LatLng CHENGDU = new LatLng(29.339879, 104.384855);// 成都市经纬度
	public static final LatLng XIAN = new LatLng(34.341568, 108.940174);// 西安市经纬度
	public static final LatLng ZHENGZHOU = new LatLng(34.7466, 113.625367);// 郑州市经纬度
	public static final LatLng HUHEHAOTE = new LatLng(40.842299, 111.7491380);// 呼和浩特市经纬度
	public static final LatLng HAERBIN = new LatLng(45.803774, 126.534967);// 哈尔滨市经纬度
	public static final LatLng XINING = new LatLng(36.617144, 101.778228);// 西宁市经纬度

    //public static final String PUSH_SERVICE_IPADD = "172.20.223.120";
    public static final String PUSH_SERVICE_IPADD = "http://192.168.1.103/lbs/";
	
	public static final String URL_LOGIN = PUSH_SERVICE_IPADD+"login.php";
	public static final String URL_LOGOUT = PUSH_SERVICE_IPADD+"hideuser.php";
	public static final String URL_REGIST = PUSH_SERVICE_IPADD+"registeruser.php";
	public static final String URL_LOCATION = PUSH_SERVICE_IPADD+"location.php";
	public static final String URL_UPDATE_LOCATION = PUSH_SERVICE_IPADD+"updatelocation.php";
	public static final String URL_SEND_MSG = PUSH_SERVICE_IPADD+"sendmessage.php";
	public static final String ACTION_URL = "http://"+PUSH_SERVICE_IPADD+"action.php";
	
	public static final String SHARE_LOGIN_INFO = "findfriends.pre";
	public static final String SHARE_LOGIN_USERNAME = "map_login_username";
	public static final String SHARE_LOGIN_PASSWORD = "map_login_password";

}
