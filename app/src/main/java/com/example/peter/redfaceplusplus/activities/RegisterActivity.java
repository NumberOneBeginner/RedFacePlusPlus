package com.example.peter.redfaceplusplus.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.peter.redfaceplusplus.R;
import com.example.peter.redfaceplusplus.selecttime.TimeSelectorView;
import com.example.peter.redfaceplusplus.utils.Contents;
import com.google.gson.Gson;
import com.loopj.android.http.RequestParams;

import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterActivity extends BaseActivity implements View.OnClickListener {
    /* 相机请求码 */
    private static final int REQUEST_CAMERA = 0;
    private static final int REQUEST_READ = 1;
    private ImageView imageView;//照片展示控件初始化
    private File temp = null;
    private Button btn_picture, btn_photo, btn_cancel, btn_add;
    private TextView btn_start, btn_end;
    private AlertDialog alertDialog, dialog;
    private EditText ed_name;
    private Bitmap head;// 头像Bitmap
    @SuppressLint("SdCardPath")
    private static String path = "/sdcard/myHead/";// sd路径
    private View contentView;
    private TimeSelectorView timeSelectorView;
    private PopupWindow mPopupWindow;
    private String startTime = null;
    private String endTime = null;
    private String imagePath;
    private TimeSelectorView.TimeChangeListener mListener;
    private LinearLayout rl_main;
    private int mTextColorUnChecked = 0xff666666;
    private int mTextColorChecked = 0xff378ad3;
    private long startTimeL, endTimeL;
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case Contents.UPDATA_PHOTO:
                    try {
                        String response = message.getData().getString("photoresponse");
                        JSONObject object = new JSONObject(response);
                        if (object.has("code")) {
                            int code;
                            if (object.get("code") instanceof Integer) {
                                code = object.getInt("code");
                            } else {
                                code = Integer.MIN_VALUE;
                            }
                            if (code == 0) {
                                if (object.has("data")) {
                                    JSONObject objectD = object.getJSONObject("data");
                                    String companyId = objectD.optString("company_id", " ");
                                    int photoId = objectD.optInt("id", Integer.MIN_VALUE);
                                    addUserInfo(photoId);
                                }
                            } else {
                                dialog.dismiss();
                                Toast toast = Toast.makeText(RegisterActivity.this, "upload photo fail", 2);
                                toast.setGravity(Gravity.BOTTOM, 0, getScreenHeight(RegisterActivity.this)/5);
                                toast.show();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case Contents.ADD_USER:
                    try {
                        dialog.dismiss();
                        String response = message.getData().getString("create");
                        JSONObject object = new JSONObject(response);
                        if (object.has("code")) {
                            int code;
                            if (object.get("code") instanceof Integer) {
                                code = object.getInt("code");
                            } else {
                                code = Integer.MIN_VALUE;
                            }
                            if (code == 0) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                                builder.setIcon(R.mipmap.face_icon);
                                builder.setTitle(Contents.CREATE_UESE_TITLE);
                                builder.setMessage(Contents.CREATE_UESE_MESSAGE);
                                builder.setNegativeButton("ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        alertDialog.dismiss();
                                    }
                                });
                                alertDialog = builder.create();
                                alertDialog.show();
//                                Toast.makeText(RegisterActivity.this, "Add user success", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast toast = Toast.makeText(RegisterActivity.this, "Add user failed", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.BOTTOM, 0, getScreenHeight(RegisterActivity.this)/5);
                                toast.show();
                            }
                        } else {
                            Toast toast = Toast.makeText(RegisterActivity.this, "Add user failed", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.BOTTOM, 0, getScreenHeight(RegisterActivity.this)/5);
                            toast.show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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
        setContentView(R.layout.activity_register);
        init();
    }

    private void init() {
        rl_main = findViewById(R.id.rl_main);
        imageView = findViewById(R.id.img_show);
        btn_add = findViewById(R.id.btn_add);
        btn_start = findViewById(R.id.btn_start);
        btn_end = findViewById(R.id.btn_end);
        ed_name = findViewById(R.id.ed_name);
        btn_start.setOnClickListener(this);
        btn_end.setOnClickListener(this);
        btn_add.setOnClickListener(this);
        imageView.setImageResource(R.mipmap.defaultavator);
        imageView.setOnClickListener(this);
    }

    //get user info  and set to map send server
    private void addUserInfo(int photoIds) {
        List<Integer> photoL = new ArrayList<Integer>();
        photoL.add(photoIds);
//        int[] photoArray = {photoId};
        try {
            Map<String, Object> userCreate = new HashMap<>();
            userCreate.put("name", ed_name.getText().toString());
            userCreate.put("subject_type", 1);
            userCreate.put("start_time", startTimeL);
            userCreate.put("end_time", endTimeL);
            userCreate.put("photo_ids", photoL);
            Gson gson = new Gson();
            String jsonParam = gson.toJson(userCreate);
            requestUtils.setHeader("Content-Type", "application/json");
            requestUtils.addUserPost(this, Contents.FACE_HOST + Contents.ADD_USER_URL, new StringEntity(jsonParam, "UTF-8"), "application/json", handler);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private long timeFormat(String time) {
        try {
//            String time = "2017年7月23日13时01分";
            Date date = new SimpleDateFormat("yyyy/MM/dd HH:mm").parse(time);
//            Date date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(time);
            long unixTimestamp = date.getTime() / 1000;//java转换出来的是精确到毫秒
            // 需求是精确到秒所以除以1000
            System.out.println(unixTimestamp);
            return unixTimestamp;
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_show:
                checkPermissionToImage();
                break;
            case R.id.btn_start:
                showPop(this, btn_start);
                break;
            case R.id.btn_end:
                showPop(this, btn_end);
                break;
            case R.id.btn_add:
                if (btn_start.getText() != null && !btn_start.getText().toString().trim().equals("")) {
                    startTime = btn_start.getText().toString().trim();
                }
                if (btn_end.getText() != null && !btn_end.getText().toString().trim().equals("")) {
                    endTime = btn_end.getText().toString().trim();
                }
                if (ed_name.getText() == null || ed_name.getText().toString().trim().equals("")) {
                    Toast toast = Toast.makeText(RegisterActivity.this, "Please edit enter name", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.BOTTOM, 0, getScreenHeight(RegisterActivity.this)/5);
                    toast.show();
                    return;
                }
                if (startTime != null && endTime != null) {
                    try {
                        if (startTime.equals("Please select start time")||endTime.equals("Please select end time")){
                            Toast toast = Toast.makeText(this, "Please choose the right time", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.BOTTOM, 0, getScreenHeight(RegisterActivity.this)/5);
                            toast.show();
                            return;
                        }
                        if (new SimpleDateFormat("yyyy/MM/dd HH:mm").parse(endTime)
                                .after(new SimpleDateFormat("yyyy/MM/dd HH:mm").parse(startTime))) {
                            startTimeL = timeFormat(startTime);
                            endTimeL = timeFormat(endTime);
                            if (startTimeL != -1 && endTimeL != -1) {
                                uploadFaceServerPhoto();
                            } else {
                                Toast toast = Toast.makeText(this, "Please choose the right time", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.BOTTOM, 0, getScreenHeight(RegisterActivity.this)/5);
                                toast.show();
                                return;
                            }
                        } else {
                            Toast toast = Toast.makeText(this, "Please choose the right time", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.BOTTOM, 0, getScreenHeight(RegisterActivity.this)/5);
                            toast.show();
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                } else {
                    Toast toast = Toast.makeText(this, "Please choose the right time", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.BOTTOM, 0, getScreenHeight(RegisterActivity.this)/5);
                    toast.show();
                    return;
                }
                break;
            default:

                break;
        }
    }

    private void checkPermissionToImage() {
        int permission = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            requestCameraPermission();
//                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
//                            REQUEST_CAMERA);
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_READ);
            } else {
                getImage();
            }
        }
    }

    private void showPop(Context context, TextView textView) {
        contentView = LayoutInflater.from(context).inflate(R.layout.popu_timeselector, null, false);
        timeSelectorView = (TimeSelectorView) contentView.findViewById(R.id.timeselector);
        timeSelectorView.setListener(initTimeChangeListener(textView));
        /**初始化PopupWindow*/
        mPopupWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setFocusable(true);// 取得焦点
        //点击推出,要设置backgroundDrawable
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        /**设置PopupWindow弹出和退出时候的动画效果*/
        mPopupWindow.setAnimationStyle(R.style.animotorPdop);
        backgroundAlpha(0.8f);
        //添加pop窗口关闭事件
        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                backgroundAlpha(1.0f);
            }
        });
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.showAtLocation(rl_main, Gravity.BOTTOM, 0, 0);

    }

    /**
     * 设置添加屏幕的背景透明度
     *
     * @param bgAlpha
     */
    public void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        getWindow().setAttributes(lp);
    }

    private TimeSelectorView.TimeChangeListener initTimeChangeListener(final TextView textView) {
        mListener = new TimeSelectorView.TimeChangeListener() {

            @Override
            public void scrollFinish(String time) {
                textView.setText(time);
                textView.setTextColor(mTextColorChecked);
            }

            @Override
            public void onFinish() {
                mPopupWindow.dismiss();
            }

            @Override
            public void onCancle() {
//                textView.setText(mDefault);
                textView.setTextColor(mTextColorUnChecked);
//                mImageView.setVisibility(INVISIBLE);
                mPopupWindow.dismiss();
            }
        };
        return mListener;
    }

    /**
     * upload photo to face plus plus
     */
    private void uploadFaceServerPhoto() {
        try {
            if (imagePath != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                builder.setIcon(R.mipmap.face_icon);
                builder.setTitle("Uploading");
                builder.setMessage("Uploading, please wait ...");
                builder.setNegativeButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.dismiss();
                    }
                });
                dialog = builder.create();
                dialog.show();
                File file = new File(imagePath);
                RequestParams params = new RequestParams();
                params.put("photo", file);
                requestUtils.cleanHead();
                requestUtils.requestPost(Contents.FACE_HOST + Contents.UPDATA_PHOTO_URL, params, handler);
            } else {
                Toast toast=Toast.makeText(this, "Please select upload photo", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.BOTTOM, 0, getScreenHeight(this)/5);
                toast.show();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取屏幕的高度
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context) {
        WindowManager manager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        return display.getHeight();
    }
    /**
     * Kill app application and processes
     *
     * @author charlie
     */
    public void killApplication() {
        //need permission : <uses-permission android:name="android.permission.RESTART_PACKAGES" />
        String packName = this.getPackageName();
        ActivityManager activityMgr = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        activityMgr.restartPackage(packName);
        //need permission : <uses-permission android:name="android.permission.KILL_BACKGROUND_PROCESSES" />
        activityMgr.killBackgroundProcesses(packName);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /**
     * 开始获取图片 可选择是相册 还是相机
     */
    private void getImage() {
        View view = getLayoutInflater().inflate(R.layout.phtot_select, null);
        final Dialog dialog = new Dialog(this, R.style.transparentFrameWindowStyle);
        dialog.setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        Window window = dialog.getWindow();
        // 设置显示动画
        window.setWindowAnimations(R.style.main_menu_animstyle);
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.x = 0;
        wl.y = getWindowManager().getDefaultDisplay().getHeight();
        // 以下这两句是为了保证按钮可以水平满屏
        wl.width = ViewGroup.LayoutParams.MATCH_PARENT;
        wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        // 设置显示位置
        dialog.onWindowAttributesChanged(wl);
        // 设置点击外围解散
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

        btn_picture = (Button) window.findViewById(R.id.btn_picture);
        btn_photo = (Button) window.findViewById(R.id.btn_photo);
        btn_cancel = (Button) window.findViewById(R.id.btn_cancle);

        btn_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(Intent.ACTION_PICK, null);
                intent1.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent1, 1);
                dialog.dismiss();
            }
        });
        btn_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent2.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "head.jpg")));
                startActivityForResult(intent2, 2);// 采用ForResult打开
                dialog.dismiss();
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    cropPhoto(data.getData());// 裁剪图片
                }

                break;
            case 2:
                if (resultCode == RESULT_OK) {
                    temp = new File(Environment.getExternalStorageDirectory() + "/head.jpg");
                    cropPhoto(Uri.fromFile(temp));// 裁剪图片
                }

                break;
            case 3:
                if (data != null) {
                    Bundle extras = data.getExtras();
                    head = extras.getParcelable("data");
                    if (head != null) {
                        /**
                         * 上传服务器代码
                         */
                        setPicToView(head);// 保存在SD卡中
                        imageView.setImageBitmap(toRoundBitmap(head));// 用ImageView显示出来
                    }
                }
                break;
            default:
                break;

        }
    }

    /**
     * 调用系统的裁剪
     *
     * @param uri
     */
    public void cropPhoto(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 160);
        intent.putExtra("outputY", 160);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, 3);
    }

    private void setPicToView(Bitmap mBitmap) {
        String sdStatus = Environment.getExternalStorageState();
        if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
            return;
        }
        FileOutputStream b = null;
        File file = new File(path);
        file.mkdirs();// 创建文件夹
        String fileName = path + "head.jpg";// 图片名字
        imagePath = fileName;
        try {
            b = new FileOutputStream(fileName);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);// 把数据写入文件

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                // 关闭流
                b.flush();
                b.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (imagePath != null) {
            deleteFolder(imagePath);
        }
        if (temp != null) {
            deleteFileS(temp.getAbsolutePath());
        }
        if (requestUtils != null) {
            requestUtils = null;
        }
    }

    /**
     * 把bitmap转成圆形
     */
    public Bitmap toRoundBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int r = 0;
        // 取最短边做边长
        if (width < height) {
            r = width;
        } else {
            r = height;
        }
        // 构建一个bitmap
        Bitmap backgroundBm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        // new一个Canvas，在backgroundBmp上画图
        Canvas canvas = new Canvas(backgroundBm);
        Paint p = new Paint();
        // 设置边缘光滑，去掉锯齿
        p.setAntiAlias(true);
        RectF rect = new RectF(0, 0, r, r);
        // 通过制定的rect画一个圆角矩形，当圆角X轴方向的半径等于Y轴方向的半径时，
        // 且都等于r/2时，画出来的圆角矩形就是圆形
        canvas.drawRoundRect(rect, r / 2, r / 2, p);
        // 设置当两个图形相交时的模式，SRC_IN为取SRC图形相交的部分，多余的将被去掉
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        // canvas将bitmap画在backgroundBmp上
        canvas.drawBitmap(bitmap, null, rect, p);
        return backgroundBm;
    }


    /**
     * 申请相机权限
     */
    private void requestCameraPermission() {
        // 相机权限未被授予，需要申请！
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            // 如果访问了，但是没有被授予权限，则需要告诉用户，使用此权限的好处

            AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
            builder.setIcon(R.mipmap.face_icon);
            builder.setTitle("Access Request");
            builder.setMessage("Please authorize the application of the appropriate permissions, or can not follow up. ");
            builder.setNegativeButton("ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // 这里重新申请权限
                    ActivityCompat.requestPermissions(RegisterActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            REQUEST_CAMERA);
                    alertDialog.dismiss();
                }
            });
            alertDialog = builder.create();
            alertDialog.show();
        } else {
            // 第一次申请，就直接申请
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA) {
            checkPermissionToImage();
        } else if (requestCode == REQUEST_READ) {
            checkPermissionToImage();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * 删除文件夹以及目录下的文件
     *
     * @param filePath 被删除目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public boolean deleteDirectory(String filePath) {
        boolean flag = false;
        //如果filePath不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        File[] files = dirFile.listFiles();
        //遍历删除文件夹下的所有文件(包括子目录)
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                //删除子文件
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag) break;
            } else {
                //删除子目录
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag) break;
            }
        }
        if (!flag) return false;
        //删除当前空目录
        return dirFile.delete();
    }

    /**
     * 根据路径删除指定的目录或文件，无论存在与否
     *
     * @param filePath 要删除的目录或文件
     * @return 删除成功返回 true，否则返回 false。
     */
    public boolean deleteFolder(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        } else {
            if (file.isFile()) {
                // 为文件时调用删除文件方法
                return deleteFileS(filePath);
            } else {
                // 为目录时调用删除目录方法
                return deleteDirectory(filePath);
            }
        }
    }

    /**
     * 删除单个文件
     *
     * @param filePath 被删除文件的文件名
     * @return 文件删除成功返回true，否则返回false
     */
    public boolean deleteFileS(String filePath) {
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            return file.delete();
        }
        return false;
    }
}
