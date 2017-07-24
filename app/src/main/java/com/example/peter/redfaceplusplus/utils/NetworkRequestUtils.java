package com.example.peter.redfaceplusplus.utils;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.ClientContext;


/**
 * Created by peter on 2017/7/20.
 */

public class NetworkRequestUtils {
    private static NetworkRequestUtils requestObject;
    private AsyncHttpClient httpClient;

    /**
     * 构造方法--创建链接对象
     */
    private NetworkRequestUtils() {
        httpClient = new AsyncHttpClient();
        httpClient.setConnectTimeout(5000);
    }

    public void setHeader(String header, String value) {
        if (httpClient != null) {
            httpClient.addHeader(header, value);
        }
    }
    public void cleanHead(){
httpClient.removeAllHeaders();
    }

    public void setCookieStore(CookieStore cookieStore) {
        httpClient.setCookieStore(cookieStore);
    }

    public static NetworkRequestUtils getInstance() {
        synchronized (NetworkRequestUtils.class) {
            if (requestObject == null) {
                requestObject = new NetworkRequestUtils();
            }
            return requestObject;
        }
    }

    public void requestPost(String url, RequestParams params, final Handler handler) {
        httpClient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
//                CookieStore responseCookieStore = (CookieStore) httpClient.getHttpContext().getAttribute(ClientContext.COOKIE_STORE);
                Message message = handler.obtainMessage();
                if (bytes != null) {
                    Bundle bundle = new Bundle();
                    String respon = new String(bytes);
                    bundle.putString("photoresponse", respon);
                    message.setData(bundle);
//                    message.obj = responseCookieStore;
                    message.what = Contents.UPDATA_PHOTO;
                    message.sendToTarget();
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                if (bytes != null) {
                    Log.e("ddd", "mag: +" + bytes.toString());
                }
            }
        });
    }

    public void requestPost(Context context, String url, HttpEntity entity, String contentType, final Handler handler) {
        httpClient.post(context, url, entity, contentType, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                CookieStore responseCookieStore = (CookieStore) httpClient.getHttpContext().getAttribute(ClientContext.COOKIE_STORE);
                setCookieStore(responseCookieStore);
                Message message = handler.obtainMessage();
                if (bytes != null) {
                    String response = new String(bytes);
//                    Log.e("ddd", "mag: +" + response);
                    Bundle bundle = new Bundle();
                    bundle.putString("response", response);
                    message.setData(bundle);
//                    message.obj=responseCookieStore;
                    message.what = Contents.LOGIN_RESPONSE;
                    message.sendToTarget();
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                if (bytes != null) {
                    Log.e("ddd", "mag: +" + bytes.toString());
                }
            }
        });
    }
    public void addUserPost(Context context, String url, HttpEntity entity, String contentType, final Handler handler) {
        httpClient.post(context, url, entity, contentType, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
//                CookieStore responseCookieStore = (CookieStore) httpClient.getHttpContext().getAttribute(ClientContext.COOKIE_STORE);
//                setCookieStore(responseCookieStore);
                Message message = handler.obtainMessage();
                if (bytes != null) {
                    String response = new String(bytes);
                    Bundle bundle = new Bundle();
                    bundle.putString("create", response);
                    message.setData(bundle);
                    message.what = Contents.ADD_USER;
                    message.sendToTarget();
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                if (bytes != null) {
                    Log.e("ddd", "mag: +" + bytes.toString());
                }
            }
        });
    }

}
