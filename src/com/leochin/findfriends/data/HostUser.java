
package com.leochin.findfriends.data;

public class HostUser extends UserInfo{
    
    private static HostUser mHostUser ;
    
    private boolean isOnline;
    
    public static final HostUser getHostUserInstance(){
        if(mHostUser == null){
            mHostUser = new HostUser();
        }
        return mHostUser;
    }
    
    public void setUserInfo(UserInfo  info){
        this.uid = info.uid;
        this.username = info.username;
        this.lat = info.lat;
        this.lon = info.lon;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }   
}
/*
public class HostUser extends UserInfo{
    
    private static HostUser mHostUser ;
    
    public static final HostUser getHostUserInstance(){
        if(mHostUser == null){
            mHostUser = new HostUser();
        }
        return mHostUser;
    }

}*/
