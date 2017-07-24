package com.example.peter.redfaceplusplus.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.example.peter.redfaceplusplus.R;
import com.example.peter.redfaceplusplus.utils.Contents;
import com.example.peter.redfaceplusplus.utils.NetworkRequestUtils;
import com.loopj.android.http.PersistentCookieStore;

import org.apache.http.client.CookieStore;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LoginActivity extends BaseActivity implements View.OnClickListener {
    private Button btnLogin;
    private EditText edEmail, edPws;
    private ProgressDialog dialog;
    private AlertDialog alertDialog;
    private SharedPreferences preferences;
    private RelativeLayout layout;
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case Contents.LOGIN_RESPONSE:
                    String response = (String) message.getData().getString("response");
                    parseJson(response);
                    break;

                default:
                    break;
            }
            return false;
        }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    /**
     * 初始化控件对象
     */
    @SuppressLint("NewApi")
    private void init() {
        requestUtils = NetworkRequestUtils.getInstance();
        dialog = new ProgressDialog(this);
        preferences = getSharedPreferences(LoginActivity.class.getSimpleName(), MODE_PRIVATE);
        layout = findViewById(R.id.rl_content);
        btnLogin = findViewById(R.id.btn_login);
        edEmail = findViewById(R.id.ed_email);
        edPws = findViewById(R.id.ed_pws);
        btnLogin.setOnClickListener(this);

        if (!preferences.getString("email", "").equals("")) {
            edEmail.setText(preferences.getString("email", ""));
        }
        edEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkUsernameAndPws();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkUsernameAndPws();
            }
        });
    }

    /**
     * 用户登录face++
     */
    private void loginFace() {
        PersistentCookieStore myCookieStore = new PersistentCookieStore(LoginActivity.this);
        requestUtils.setCookieStore(myCookieStore);
        requestUtils.setHeader("User-Agent", "Koala Admin");
        requestUtils.setHeader("Content-Type", "application/json");
        try {
            JSONObject json = new JSONObject();
            json.put("username", edEmail.getText().toString());
            json.put("password", edPws.getText().toString());
            requestUtils.requestPost(this, Contents.FACE_HOST + Contents.LOGIN_URL, new StringEntity(json.toString(), "UTF-8"), "application/json", handler);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
//       params.put("username", "developer@hsbc.com");
//        params.put("username", "hsbc@megvii.com");
//        params.put("password", "123456");
    }

    /**
     * 处理登陆之后服务端返回的结果
     *
     * @param response
     */
    private void parseJson(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.has("code")) {
                int code = jsonObject.getInt("code");
                if (code == 0) {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("email", edEmail.getText().toString());
                    editor.commit();
                    Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                    startActivity(intent);
                    dialog.dismiss();
                    finish();
                } else {
                    dialog.dismiss();
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setIcon(R.mipmap.face_icon);
                    builder.setTitle(Contents.LOGIN_FAILED);
                    builder.setMessage(Contents.FAILED_MESSAGE);
                    builder.setNegativeButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            alertDialog.dismiss();
                        }
                    });
                    alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 检测输入内容是不是邮箱格式
     */
    private void checkUsernameAndPws() {
        //电子邮件
        String check = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
//        String check = "^[a-z0-9A-Z]+@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[cnom]{2,3}$";
        Pattern regex = Pattern.compile(check);
        Matcher matcher = regex.matcher(edEmail.getText().toString());
        boolean isMatched = matcher.matches();
        if (isMatched) {
            btnLogin.setEnabled(true);
            edEmail.setError(null);
        } else {
            edEmail.setError("Please enter the correct email address");
            btnLogin.setEnabled(false);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                dialog.setTitle(R.string.dialogtitle);
                dialog.setIcon(R.mipmap.face_icon);
                dialog.setMessage(Contents.MESSAGE_CONTENT);
                dialog.show();
                loginFace();
                break;
            default:

                break;
        }
    }

    //获取屏幕的宽度
    public static int getScreenWidth(Context context) {
        WindowManager manager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        return display.getWidth();
    }
}
