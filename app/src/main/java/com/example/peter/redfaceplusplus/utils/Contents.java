package com.example.peter.redfaceplusplus.utils;


import org.apache.http.client.CookieStore;

/**
 * Created by peter on 2017/7/20.
 */

public class Contents {
//    public static final String FACE_HOST ="http://172.16.1.10";
    public static final String FACE_HOST ="https://v2.koalacam.net";
    public static final String LOGIN_URL = "/auth/login";
    public static final String UPDATA_PHOTO_URL = "/subject/photo";
    public static final String ADD_USER_URL = "/subject";
    public static final int LOGIN_RESPONSE = 1;
    public static final int UPDATA_PHOTO = 2;
    public static final int ADD_USER = 3;
    public static CookieStore COOKIE_STORE;
    public static final String MESSAGE_CONTENT = "Is landing, please later ......";
    public static final String LOGIN_FAILED = "Login Failed";
    public static final String FAILED_MESSAGE = "Landing failed, please confirm your username and password, try again.";
    public static final String CREATE_UESE_TITLE = "Added successfully";
    public static final String CREATE_UESE_MESSAGE = "You have added success, please direct access to facial information to enter.";


}
