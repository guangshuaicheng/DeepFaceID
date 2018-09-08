/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package face.yang.com.facerecognition.ui.activity.user;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.aip.ImageFrame;
import com.baidu.aip.api.FaceApi;
import com.baidu.aip.callback.ILivenessCallBack;
import com.baidu.aip.entity.IdentifyRet;
import com.baidu.aip.entity.LivenessModel;
import com.baidu.aip.entity.User;
import com.baidu.aip.face.FaceCropper;
import com.baidu.aip.manager.FaceEnvironment;
import com.baidu.aip.manager.FaceLiveness;
import com.baidu.aip.manager.FaceSDKManager;
import com.baidu.aip.utils.FileUitls;
import com.baidu.aip.utils.ImageUtils;
import com.baidu.aip.utils.PreferencesUtil;
import com.baidu.idl.facesdk.FaceInfo;
import com.baidu.idl.facesdk.FaceTracker;
import com.orbbec.obDepth2.HomeKeyListener;
import com.orbbec.view.OpenGLView;

import org.openni.Device;
import org.openni.DeviceInfo;
import org.openni.OpenNI;
import org.openni.PixelFormat;
import org.openni.SensorType;
import org.openni.VideoMode;
import org.openni.VideoStream;
import org.openni.android.OpenNIHelper;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import butterknife.BindView;
import butterknife.OnClick;
import face.yang.com.facerecognition.R;
import face.yang.com.facerecognition.base.BaseActivity;
import face.yang.com.facerecognition.base.FaceApplication;
import face.yang.com.facerecognition.entity.UserEntity;
import face.yang.com.facerecognition.utils.Constants;
import face.yang.com.facerecognition.utils.FaceUtils;
import face.yang.com.facerecognition.utils.GPIO;
import face.yang.com.facerecognition.utils.JsonUtils;
import face.yang.com.facerecognition.view.CommonDialog;
import face.yang.com.facerecognition.view.RectView;

/**
 * 自动检测获取人脸
 */
public class OrbbecLivenessDetectActivity extends BaseActivity implements OpenNIHelper.DeviceOpenListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static String TAG = "OpenniLivenessDetect";

    @BindView(R.id.yuan_image)
    ImageView yuanImage;
    @BindView(R.id.title_text)
    TextView titleText;
    @BindView(R.id.tip_text)
    TextView tipText;
    @BindView(R.id.tip_pass)
    TextView tipPass;

    private HomeKeyListener mHomeListener;
    private Activity mContext;
    private final int CREATE_OPENNI = 0x001;
    private boolean m_InitOk = false;
    private boolean m_pause = false;
    private ExecutorService es;
    private Future future;
    private boolean success = false;
    private int source;

    private boolean initOk = false;
    private Device device;
    private Thread thread;
    private OpenNIHelper mOpenNIHelper;
    private VideoStream depthStream;
    private VideoStream rgbStream;

    private OpenGLView mDepthGLView;
    private OpenGLView mRgbGLView;
    private User user;
    private String userIdOfMaxScore = "";
    private float maxScore = 65.1f;

    private int mWidth = com.orbbec.utils.GlobalDef.RESOLUTION_X;
    private int mHeight = com.orbbec.utils.GlobalDef.RESOLUTION_Y;
    private final int DEPTH_NEED_PERMISSION = 33;
    private Object sync = new Object();
    private boolean exit = false;
    private CommonDialog commonDialog;
//    private String pin="263";
    private String pin="89";

    private static final int IDENTITY_IDLE = 2;
    private static final int IDENTITYING = 3;
    private boolean isCheck=false;
    private boolean result=false;
    private int count=0;

    private static final int FEATURE_DATAS_UNREADY = 1;

    private volatile int identityStatus = FEATURE_DATAS_UNREADY;

    private android.os.Handler handler = new android.os.Handler(Looper.getMainLooper());
    private CommonDialog pwdDialog;
    private boolean isShowPassword=false;
    private boolean isP=false;
    private RectView rectView;

    @Override
    public int getLayoutID() {
        return R.layout.activity_orbbec_liveness_detect;
    }

    @Override
    public void initView() {
        findView();

        FaceUtils.instance().initFace(this);

        mContext = this;

        registerHomeListener();
        es = Executors.newSingleThreadExecutor();
    }

    @OnClick({R.id.regist_button,R.id.system_manager})
    void click(final View v){
        if(!FaceSDKManager.getInstance().check()){
            return;
        }

//        switch (v.getId()){
//                case R.id.regist_button:
//                    startActivity(new Intent(OrbbecLivenessDetectActivity.this,RegistActivity.class));
//                    finish();
//                    break;
//                case R.id.system_manager:
//                    startActivity(new Intent(OrbbecLivenessDetectActivity.this,ManagerActivity.class));
//                    finish();
//                    break;
//            }

        if(PreferencesUtil.getBoolean("login",false)){
            switch (v.getId()){
                case R.id.regist_button:
                    startActivity(new Intent(OrbbecLivenessDetectActivity.this,RegistActivity.class));
                    finish();
                    break;
                case R.id.system_manager:
                    startActivity(new Intent(OrbbecLivenessDetectActivity.this,ManagerActivity.class));
                    finish();
                    break;
            }
        }else{
            tipInputPassWord(new LoginActivity2.ClickListener() {
                @Override
                public void click() {
                    PreferencesUtil.putBoolean("login",true);
                    switch (v.getId()){
                        case R.id.regist_button:
                            startActivity(new Intent(OrbbecLivenessDetectActivity.this,RegistActivity.class));
                            finish();
                            break;
                        case R.id.system_manager:
                            startActivity(new Intent(OrbbecLivenessDetectActivity.this,ManagerActivity.class));
                            finish();
                            break;
                    }
                }
            });
        }
    }


    private void findView() {

        mDepthGLView = (OpenGLView) findViewById(R.id.depthGlView);
        mRgbGLView = (OpenGLView) findViewById(R.id.rgbGlView);
        rectView = findViewById(R.id.rectview);

        ViewGroup.LayoutParams layoutParams = mRgbGLView.getLayoutParams();
        layoutParams.width=FaceUtils.instance().getDisPlayWidth(this)*2/3;
        layoutParams.height=FaceUtils.instance().getDisPlayWidth(this)*2/3;

        mOpenNIHelper = new OpenNIHelper(this);
        mOpenNIHelper.requestDeviceOpen(this);
    }

    private void init(UsbDevice device) {
        OpenNI.setLogAndroidOutput(false);
        OpenNI.setLogMinSeverity(0);
        OpenNI.initialize();

        List<DeviceInfo> opennilist = OpenNI.enumerateDevices();
        if (opennilist.size() <= 0) {
            Toast.makeText(this, " openni enumerateDevices 0 devices", Toast.LENGTH_LONG).show();
            return;
        }

        this.device = null;
        //Find device ID
        for (int i = 0; i < opennilist.size(); i++) {
            if (opennilist.get(i).getUsbProductId() == device.getProductId()) {
                this.device = Device.open();
                break;
            }
        }

        if (this.device == null) {
            Toast.makeText(this, " openni open devices failed: " + device.getDeviceName(),
                    Toast.LENGTH_LONG).show();
            return;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkSelfPermission();
        loadFeature2Memery();

        GPIO.statusPin(pin);
        GPIO.settingPin(pin);
        GPIO.closePin(pin);
    }

    @Override
    protected void onPause() {
        super.onPause();


    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        GPIO.closeSU();
        dismissDialog();

        FaceUtils.instance().releaseSound();
        if (initOk) {
            exit = true;
            if (thread != null) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (depthStream != null) {
                depthStream.stop();
            }
            if (rgbStream != null) {
                rgbStream.stop();
            }

            if (device != null) {
                device.close();
            }
        }
        if (mOpenNIHelper != null) {
            mOpenNIHelper.shutdown();
        }
    }

    @Override
    public void onDeviceOpened(UsbDevice device) {
        init(device);

        depthStream = VideoStream.create(this.device, SensorType.DEPTH);
        List<VideoMode> mVideoModes = depthStream.getSensorInfo().getSupportedVideoModes();

        for (VideoMode mode : mVideoModes) {
            int X = mode.getResolutionX();
            int Y = mode.getResolutionY();
            int fps = mode.getFps();

            if (X == mWidth && Y == mHeight && mode.getPixelFormat() == PixelFormat.DEPTH_1_MM) {
                depthStream.setVideoMode(mode);
                Log.v(TAG, " setmode");
            }

        }
        rgbStream = VideoStream.create(this.device, SensorType.COLOR);
        List<VideoMode> mColorVideoModes = rgbStream.getSensorInfo().getSupportedVideoModes();

        for (VideoMode mode : mColorVideoModes) {
            int X = mode.getResolutionX();
            int Y = mode.getResolutionY();
            int fps = mode.getFps();

            Log.d(TAG, " support resolution: " + X + " x " + Y + " fps: " + fps + ", (" + mode.getPixelFormat() + ")");
            if (X == mWidth && Y == mHeight && mode.getPixelFormat() == PixelFormat.RGB888) {
                rgbStream.setVideoMode(mode);
                Log.v(TAG, " setmode");
            }
        }

        startThread();
        FaceLiveness.getInstance().setLivenessCallBack(new ILivenessCallBack() {
            @Override
            public void onCallback(LivenessModel livenessModel) {
                paint.setColor(Color.TRANSPARENT);
                rectView.drawRect(rectF,paint);
                if(isP){
                    checkResult(livenessModel);

                    //绘制方框
                    showFrame(livenessModel.getImageFrame(),livenessModel.getFaceInfo());
                }
            }

            @Override
            public void onTip(int code, final String msg) {
                Log.i("wtf", "onCallback" + msg);

            }
        });
    }

    @Override
    public void onDeviceOpenFailed(String msg) {
        showAlertAndExit("Open Device failed: " + msg);
    }

    void startThread() {
        initOk = true;
        thread = new Thread() {

            @Override
            public void run() {

                List<VideoStream> streams = new ArrayList<VideoStream>();

                streams.add(depthStream);
                streams.add(rgbStream);

                depthStream.start();
                rgbStream.start();

                while (!exit) {

                    try {
                        OpenNI.waitForAnyStream(streams, 2000);

                    } catch (TimeoutException e) {
                        e.printStackTrace();
                        continue;
                    }

                    synchronized (sync) {

                        mDepthGLView.update(depthStream, com.orbbec.utils.GlobalDef.TYPE_DEPTH);
                        mRgbGLView.update(rgbStream, com.orbbec.utils.GlobalDef.TYPE_COLOR);

                        ByteBuffer depthByteBuf = depthStream.readFrame().getData();
                        ByteBuffer colorByteBuf = rgbStream.readFrame().getData();
                        int depthLen = depthByteBuf.remaining();
                        int rgbLen = colorByteBuf.remaining();

                        byte[] depthByte = new byte[depthLen];
                        byte[] rgbByte = new byte[rgbLen];

                        depthByteBuf.get(depthByte);
                        colorByteBuf.get(rgbByte);

                        final Bitmap bitmap = ImageUtils.RGB2Bitmap(rgbByte, mWidth, mHeight);

                        FaceLiveness.getInstance().setRgbBitmap(bitmap);
                        FaceLiveness.getInstance().setDepthData(depthByte);
                        FaceLiveness.getInstance().livenessCheck(mWidth, mHeight, 0X0101);
                    }
                }
            }
        };

        thread.start();
    }

    private void showAlertAndExit(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == DEPTH_NEED_PERMISSION) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permission Grant");
                Toast.makeText(mContext, "Permission Grant", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "Permission Denied");
                Toast.makeText(mContext, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void checkResult(LivenessModel model) {

        if (model == null) {
            return;
        }

        int type = model.getLiveType();
        boolean livenessSuccess = false;
        // 同一时刻都通过才认为活体通过，开发者也可以根据自己的需求修改策略
        if ((type & FaceLiveness.MASK_RGB) == FaceLiveness.MASK_RGB) {
            livenessSuccess = (model.getRgbLivenessScore() > FaceEnvironment.LIVENESS_RGB_THRESHOLD) ? true : false;
        }
//        if ((type & FaceLiveness.MASK_IR) == FaceLiveness.MASK_IR) {
//            boolean irScore = (model.getIrLivenessScore() > FaceEnvironment.LIVENESS_IR_THRESHOLD) ? true : false;
//            if (!irScore) {
//                livenessSuccess = false;
//            } else {
//                livenessSuccess &= irScore;
//            }
//        }
//        if ((type & FaceLiveness.MASK_DEPTH) == FaceLiveness.MASK_DEPTH) {
//            boolean depthScore = (model.getDepthLivenessScore() > FaceEnvironment.LIVENESS_DEPTH_THRESHOLD) ? true :
//                    false;
//            if (!depthScore) {
//                livenessSuccess = false;
//            } else {
//                livenessSuccess &= depthScore;
//            }
//        }

        if (livenessSuccess) {
            //TODO

            if(isShowPassword){
                Log.i(TAG,"密码框已弹出");
                return;
            }
            FaceInfo faceInfo = model.getFaceInfo();
            if(faceInfo!=null){
                isCheck=true;
                Constants.UsetType[] values = Constants.UsetType.values();
                for (int i=1;i<values.length;i++){
                    if(result){
                        return;
                    }
                    asyncIdentity(model.getImageFrame(), new FaceInfo[]{faceInfo},values[i].getValue());
                }
            }
        }
    }

    private void asyncIdentity(final ImageFrame imageFrame, final FaceInfo[] faceInfos, final String guoupId) {
        if (identityStatus != IDENTITY_IDLE) {
            unPass();
            return;
        }

        es.submit(new Runnable() {
            @Override
            public void run() {
                if (faceInfos == null || faceInfos.length == 0) {
                    unPass();
                    return;
                }
                identity(imageFrame, faceInfos[0],guoupId);
            }
        });
    }

    private void unPass() {
        isCheck=false;
    }

    private void identity(ImageFrame imageFrame, FaceInfo faceInfo,String groupId) {


        float raw  = Math.abs(faceInfo.headPose[0]);
        float patch  = Math.abs(faceInfo.headPose[1]);
        float roll  = Math.abs(faceInfo.headPose[2]);
        // 人脸的三个角度大于20不进行识别
        if (raw > 20 || patch > 20 ||  roll > 20) {
            unPass();
            return;
        }

        identityStatus = IDENTITYING;

        int[] argb = imageFrame.getArgb();
        int rows = imageFrame.getHeight();
        int cols = imageFrame.getWidth();
        int[] landmarks = faceInfo.landmarks;
        IdentifyRet identifyRet = FaceApi.getInstance().identity(argb, rows, cols, landmarks, groupId);


        displayUserOfMaxScore(identifyRet.getUserId(), identifyRet.getScore(),groupId);
        identityStatus = IDENTITY_IDLE;
    }

    private void displayUserOfMaxScore(final String userId, final float score,final String groupId) {

        handler.post(new Runnable() {
            @Override
            public void run() {

                if (userIdOfMaxScore.equals(userId) ) {
                    if (score < maxScore) {
                        unPass();
                        FaceUtils.instance().unregist(FaceApplication.application);
                        tipPass.setText("请先注册人脸识别");
                        return;
                    }
                } else {
                    userIdOfMaxScore = userId;
                }

                if (score < maxScore) {
                    FaceUtils.instance().unregist(FaceApplication.application);
                    tipPass.setText("请先注册人脸识别");
                    return;
                }

                user = FaceApi.getInstance().getUserInfo(groupId, userId);
                if (user == null) {
                    unPass();
                    tipRegist();
                    FaceUtils.instance().unregist(FaceApplication.application);
                    return;
                }
                Log.i(TAG,"用户的信息:"+ user.getUserInfo());
                isCheck=false;
                result=true;

                UserEntity userEntity = JsonUtils.toEntity(user.getUserInfo(), UserEntity.class);

                String groupId1 = user.getGroupId();
                String type = convertName(groupId1);

                tipPass(String.format("\t\t尊敬的%s:%s \r\n" +
                        "欢迎您的光临！",type,userEntity.getName()));
                tipPass.setText("3D活体检测通过");
                FaceUtils.instance().playerSuccess(OrbbecLivenessDetectActivity.this);
            }
        });
    }

    private void tipPass(String text){

        if(isFinishing()){
            return;
        }

        if(isShowPassword||isFinishing()){
            return;
        }

        if(commonDialog!=null&&commonDialog.isShowing()){
            commonDialog.dismiss();
            commonDialog=null;
        }
        commonDialog = new CommonDialog(this, R.layout.dialog_tippass);
        Window window = commonDialog.getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.gravity= Gravity.CENTER;
        window.setAttributes(attributes);
        commonDialog.show();
        TextView dialogText = commonDialog.findViewById(R.id.dialog_context);
        Button button = commonDialog.findViewById(R.id.dialog_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissDialog();
                FaceUtils.instance().releaseSound();
            }
        });
        dialogText.setText(text);

//        GPIO.statusPin(pin);
//        GPIO.settingPin(pin);
        GPIO.openPin(pin);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                GPIO.closePin(pin);
//                GPIO.closeSU();
            }
        },1000);

    }

    private String convertName(String name){
        Constants.UsetType[] values = Constants.UsetType.values();
        for (int i=0;i<values.length;i++){
            if(values[i].getValue().equals(name)){
                return values[i].getName();
            }
        }
        return "VIP";
    }

    private void registerHomeListener() {
        mHomeListener = new HomeKeyListener(this);
        mHomeListener
                .setOnHomePressedListener(new HomeKeyListener.OnHomePressedListener() {

                    @Override
                    public void onHomePressed() {
                        finish();
                    }

                    @Override
                    public void onHomeLongPressed() {
                    }
                });
        mHomeListener.startWatch();
    }

    private void tipRegist() {
        if(!result){
            count++;
            if(count>10){
                count=0;
                tip("您好，请先注册登记");
                tipPass.setText("3D活体检测未通过");
            }
        }
    }

    private void toast(final String tip) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(OrbbecLivenessDetectActivity.this, tip, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkSelfPermission(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest
                .permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA}, 100);
        }
    }

    private void loadFeature2Memery() {
        if (identityStatus != FEATURE_DATAS_UNREADY) {
            return;
        }
        es.submit(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                // android.os.Process.setThreadPriority (-4);
                Constants.UsetType[] values = Constants.UsetType.values();
                int count=0;
                isP=false;
                for (int i=1;i<values.length;i++){
                    FaceApi.getInstance().loadFacesFromDB(values[i].getValue());
                    count+=FaceApi.getInstance().getGroup2Facesets().get(values[i].getValue()).size();
                }
                Log.i(TAG,"人脸数据加载完成，即将开始1：N");
                Log.i(TAG,"底库人脸个数：" + count);
                identityStatus = IDENTITY_IDLE;
                if(count>0){
                    isP = true;
                }
            }
        });
    }

    private  void dismissDialog(){
        Initialization();
        tipPass.setText("");
        if(commonDialog!=null){
            commonDialog.dismiss();
            commonDialog=null;
        }
    }

    private  void dismissDialogPwd(){
        Initialization();
        if(pwdDialog!=null){
            pwdDialog.dismiss();
            pwdDialog=null;
        }
    }

    private void Initialization(){
        isCheck=false;
        result=false;
        user=null;
    }

    private void tipInputPassWord( final LoginActivity2.ClickListener listener){

        isShowPassword = true;

        if(pwdDialog!=null&&pwdDialog.isShowing()){
            pwdDialog.dismiss();
            pwdDialog=null;
        }
        pwdDialog = new CommonDialog(this, R.layout.dialog_password);
        Window window = pwdDialog.getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.gravity= Gravity.CENTER;
        window.setAttributes(attributes);
        pwdDialog.show();
        final EditText passwordV = pwdDialog.findViewById(R.id.password);
        final EditText usernameV = pwdDialog.findViewById(R.id.username);
        Button loginV = pwdDialog.findViewById(R.id.login);
        loginV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String password = passwordV.getText().toString().trim();
                String username = usernameV.getText().toString().trim();
                if(TextUtils.isEmpty(password)){
                    tip("请输入密码");
                    FaceUtils.instance().tipInputPassWord(FaceApplication.application);
                }else if(TextUtils.isEmpty(username)){
                    tip("请输入用户名");
                    FaceUtils.instance().tipinputname(FaceApplication.application);
                }else {
                    if("admin".equals(username)&&"admin".equals(password)){
                        listener.click();
                        dismissDialogPwd();
                    }else {
                        tip("用户名或密码错误");
                        FaceUtils.instance().pwsErr(FaceApplication.application);
                    }
                }
            }
        });

        pwdDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                isShowPassword = false;
            }
        });
    }

    private Paint paint = new Paint();

    {
        paint.setColor(Color.YELLOW);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize(30);
        paint.setStrokeWidth(5f);
    }

    RectF rectF = new RectF();

    /**
     * 绘制人脸框。
     *
     */
    private void showFrame(ImageFrame imageFrame, FaceInfo faceInfo) {
//        Canvas canvas = rectView.getCanvas();
//        if (canvas == null) {
//            return;
//        }
        if (faceInfo == null) {
            // 清空canvas
//            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//            textureView.unlockCanvasAndPost(canvas);
            return;
        }
//        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);


        rectF.set(getFaceRect(faceInfo, imageFrame));

        // 检测图片的坐标和显示的坐标不一样，需要转换。
//        previewView.mapFromOriginalRect(rectF);

        float yaw  = Math.abs(faceInfo.headPose[0]);
        float patch  = Math.abs(faceInfo.headPose[1]);
        float roll  = Math.abs(faceInfo.headPose[2]);
        if (yaw > 20 || patch > 20 || roll > 20) {
            // 不符合要求，绘制黄框
            paint.setColor(Color.YELLOW);

            String text = "请正视屏幕";
            float width = paint.measureText(text) + 50;
            float x = rectF.centerX() - width / 2;
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.FILL);
//            canvas.drawText(text, x + 25, rectF.top - 20, paint);
            paint.setColor(Color.YELLOW);

        } else {
            // 符合检测要求，绘制绿框
            paint.setColor(Color.GREEN);
        }
        paint.setStyle(Paint.Style.STROKE);
        // 绘制框
//        canvas.drawRect(rectF, paint);
        rectView.drawRect(rectF,paint);
    }

    class MapValueComparator implements Comparator<Map.Entry<String, Float>> {

        @Override
        public int compare(java.util.Map.Entry<String, Float> me1, java.util.Map.Entry<String, Float> me2) {

            return me1.getValue().compareTo(me2.getValue());
        }
    }

    /**
     * 获取人脸框区域。
     *
     * @return 人脸框区域
     */
    // TODO padding?
    public Rect getFaceRect(FaceInfo faceInfo, ImageFrame frame) {
        Rect rect = new Rect();
        int[] points = new int[8];
        faceInfo.getRectPoints(points);

        int left = points[2];
        int top = points[3];
        int right = points[6];
        int bottom = points[7];

        //            int width = (right - left) * 4 / 3;
        //            int height = (bottom - top) * 4 / 3;
        //
        //            left = getInfo().mCenter_x - width / 2;
        //            top = getInfo().mCenter_y - height / 2;
        //
        //            rect.top = top;
        //            rect.left = left;
        //            rect.right = left + width;
        //            rect.bottom = top + height;

        //            int width = (right - left) * 4 / 3;
        //            int height = (bottom - top) * 5 / 3;
        int width = (right - left);
        int height = (bottom - top);

        //            left = getInfo().mCenter_x - width / 2;
        //            top = getInfo().mCenter_y - height * 2 / 3;
        left = (int) (faceInfo.mCenter_x - width / 2);
        top = (int) (faceInfo.mCenter_y - height  / 2);


        rect.top = top < 0 ? 0 : top;
        rect.left = left < 0 ? 0 : left;
        rect.right = (left + width) > frame.getWidth() ? frame.getWidth() : (left + width) ;
        rect.bottom = (top + height) > frame.getHeight() ? frame.getHeight() : (top + height);

        rect.right=rect.right+20;
        rect.bottom=rect.bottom+40;
        return rect;
    }

}
