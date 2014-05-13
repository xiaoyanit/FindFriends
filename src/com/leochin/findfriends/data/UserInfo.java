package com.leochin.findfriends.data;

import java.util.ArrayList;

import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;

public class UserInfo {
    protected String uid;
    protected String username;
    protected double lat;
    protected double lon;
    protected String pic; 
    protected String msg;
    
    protected String time;
    
    private boolean online;
    
    private ArrayList<String> msgList = new ArrayList<String>();
    
    protected Marker marker;
    
    public String getUid() {
        return uid;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public double getLatitude() {
        return lat;
    }
    public void setLatitude(double latitude) {
        this.lat = latitude;
    }
    public double getLongitude() {
        return lon;
    }
    public void setLongitude(double longitude) {
        this.lon = longitude;
    }
    public Marker getMarker() {
        return marker;
    }
    public void setMarker(Marker marker) {
        this.marker = marker;
    }
    public LatLng getLatLng(){
        return new LatLng(lat, lon);
    }
    public String getPic() {
        return pic;
    }
    public void setPic(String pic) {
        this.pic = pic;
    }
    
    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }   
    
    public void addMsg(String s){
        msgList.add(s);
    }
    
    public String getMsg(){
        return msg;
    }
    
    public int getMsgNum(){
        return msgList.size();
    }
    
    public ArrayList<String> getMsgList(){
        return msgList;
    }
    
    public void  cleanMsgList(){
        msgList.clear();
    }
    
    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public void setUserInfo(UserInfo  info){
        this.uid = info.uid;
        this.username = info.username;
        this.lat = info.lat;
        this.lon = info.lon;
        this.online = info.online;
        this.pic = info.pic;
    }
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		StringBuilder build = new StringBuilder();
		build.append("uid=").append(uid)
		.append(" name=").append(username)
		.append(" lat=").append(lat)
		.append(" lon=").append(lon)
		.append(" online=").append(online)
		.append(" pic=").append(pic);
		
		return  build.toString();
	}
}
