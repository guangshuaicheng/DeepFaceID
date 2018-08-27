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
import android.hardware.usb.UsbDevice;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.baidu.idl.facesdk.FaceInfo;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import butterknife.OnClick;
import face.yang.com.facerecognition.R;
import face.yang.com.facerecognition.base.BaseActivity;
import face.yang.com.facerecognition.base.FaceApplication;
import face.yang.com.facerecognition.utils.Constants;
import face.yang.com.facerecognition.utils.FaceUtils;
import face.yang.com.facerecognition.view.CommonDialog;

/**
 * 自动检测获取人脸
 */
public class RegistActivity2 extends BaseActivity implements OpenNIHelper.DeviceOpenListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static String TAG = "OpenniLivenessDetect";

    private HomeKeyListener mHomeListener;
    private Activity mContext;
    private ExecutorService es;

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
    private float maxScore = 0;

    private int mWidth = com.orbbec.utils.GlobalDef.RESOLUTION_X;
    private int mHeight = com.orbbec.utils.GlobalDef.RESOLUTION_Y;
    private final int DEPTH_NEED_PERMISSION = 33;
    private Object sync = new Object();
    private boolean exit = false;

    private static final int IDENTITY_IDLE = 2;
    private static final int IDENTITYING = 3;
    private boolean result=false;

    private static final int FEATURE_DATAS_UNREADY = 1;

    private volatile int identityStatus = FEATURE_DATAS_UNREADY;

    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isShowRegist=false;

    private int source;
    private boolean success;
    private boolean isStart;
    private CommonDialog pwdDialog;

    @Override
    public int getLayoutID() {
        return R.layout.activity_regist2;
    }

    @Override
    public void initView() {
        findView();

        FaceUtils.instance().initFace(this);

        mContext = this;

        registerHomeListener();
        es = Executors.newSingleThreadExecutor();

        Intent intent = getIntent();
        if (intent != null) {
            source = intent.getIntExtra("source", -1);
        }
    }

    @OnClick({R.id.button_start})
    void click(final View v){
        if(!FaceSDKManager.getInstance().check()){
            return;
        }

        isStart = true;

    }


    private void findView() {

        mDepthGLView = (OpenGLView) findViewById(R.id.depthGlView);
        mRgbGLView = (OpenGLView) findViewById(R.id.rgbGlView);

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

        if(pwdDialog!=null){
            pwdDialog.dismiss();
            pwdDialog=null;
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
                if(isStart&&!isShowRegist){
                    checkResult(livenessModel);
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
            Bitmap bitmap = FaceCropper.getFace(model.getImageFrame().getArgb(),
                    model.getFaceInfo(), model.getImageFrame().getWidth());

            // 注册来源保存到注册人脸目录
            File faceDir = FileUitls.getFaceDirectory();
            if (faceDir != null) {
                String imageName = UUID.randomUUID().toString();
                File file = new File(faceDir, imageName);
                // 压缩人脸图片至300 * 300，减少网络传输时间
                ImageUtils.resize(bitmap, file, 680, 680);
//                tipInputPassWord(file);
                Intent intent = new Intent();
                intent.putExtra("file_path", file.getAbsolutePath());
                setResult(Activity.RESULT_OK, intent);
                success = true;
                finish();
            } else {
                toast("注册人脸目录未找到");
            }
        }
    }

    private void asyncIdentity(final ImageFrame imageFrame, final FaceInfo[] faceInfos, final String guoupId) {
        if (identityStatus != IDENTITY_IDLE) {
            return;
        }

        es.submit(new Runnable() {
            @Override
            public void run() {
                if (faceInfos == null || faceInfos.length == 0) {
                    return;
                }
                identity(imageFrame, faceInfos[0],guoupId);
            }
        });
    }


    private void identity(ImageFrame imageFrame, FaceInfo faceInfo,String groupId) {


        float raw  = Math.abs(faceInfo.headPose[0]);
        float patch  = Math.abs(faceInfo.headPose[1]);
        float roll  = Math.abs(faceInfo.headPose[2]);
        // 人脸的三个角度大于20不进行识别
        if (raw > 20 || patch > 20 ||  roll > 20) {
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
                        return;
                    } else {
                        maxScore = score;
                        Log.i(TAG,"maxScore"+String.valueOf(maxScore));
                        return;
                    }
                } else {
                    userIdOfMaxScore = userId;
                    maxScore = score;
                }


                user = FaceApi.getInstance().getUserInfo(groupId, userId);
                if (user == null) {
                    return;
                }
            }
        });
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

    private void toast(final String tip) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(RegistActivity2.this, tip, Toast.LENGTH_SHORT).show();
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


    private void tipInputPassWord(final File file){

        isShowRegist = true;

        if(pwdDialog!=null&&pwdDialog.isShowing()){
            pwdDialog.dismiss();
            pwdDialog=null;
        }
        pwdDialog = new CommonDialog(this, R.layout.dialog_regist);
        Window window = pwdDialog.getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.gravity= Gravity.CENTER;
        window.setAttributes(attributes);
        pwdDialog.show();
        final Button confirm = pwdDialog.findViewById(R.id.confirm);
        final ImageView imageview = pwdDialog.findViewById(R.id.imageview);
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.error(R.drawable.ic_icon_head);
        Glide.with(this).load(file).apply(requestOptions).into(imageview);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("file_path", file.getAbsolutePath());
                setResult(Activity.RESULT_OK, intent);
                success = true;
                finish();
            }
        });

        pwdDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                isShowRegist = false;
            }
        });
    }


}
