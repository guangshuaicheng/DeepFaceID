package face.yang.com.facerecognition.ui.activity.user;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.usb.UsbDevice;
import android.os.Looper;
import android.support.annotation.NonNull;
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
import com.baidu.aip.face.CameraImageSource;
import com.baidu.aip.face.FaceDetectManager;
import com.baidu.aip.face.PreviewView;
import com.baidu.aip.face.camera.CameraView;
import com.baidu.aip.face.camera.ICameraControl;
import com.baidu.aip.manager.FaceEnvironment;
import com.baidu.aip.manager.FaceLiveness;
import com.baidu.aip.manager.FaceSDKManager;
import com.baidu.aip.utils.ImageUtils;
import com.baidu.aip.utils.PreferencesUtil;
import com.baidu.idl.facesdk.FaceInfo;

import org.openni.Device;
import org.openni.DeviceInfo;
import org.openni.OpenNI;
import org.openni.PixelFormat;
import org.openni.SensorType;
import org.openni.VideoMode;
import org.openni.VideoStream;
import org.openni.android.OpenNIHelper;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import butterknife.BindView;
import butterknife.OnClick;
import face.yang.com.facerecognition.R;
import face.yang.com.facerecognition.base.BaseActivity;
import face.yang.com.facerecognition.entity.UserEntity;
import face.yang.com.facerecognition.ui.activity.LivenessSettingActivity;
import face.yang.com.facerecognition.utils.Constants;
import face.yang.com.facerecognition.utils.FaceUtils;
import face.yang.com.facerecognition.utils.GPIO;
import face.yang.com.facerecognition.utils.JsonUtils;
import face.yang.com.facerecognition.view.CommonDialog;

public class LoginActivity3 extends BaseActivity implements OpenNIHelper.DeviceOpenListener {

    private final static String TAG="LoginActivity";

    @BindView(R.id.head_image)
    ImageView headImage;
    @BindView(R.id.yuan_image)
    ImageView yuanImage;
    @BindView(R.id.title_text)
    TextView titleText;
    @BindView(R.id.tip_text)
    TextView tipText;
    @BindView(R.id.tip_pass)
    TextView tipPass;
//    private ImageView testView;
    private TextView userOfMaxSocre;
    private PreviewView previewView;
    private Object sync = new Object();
//    private TextureView textureView;

    private String pin="263";
    private Device device;
    private boolean exit = false;

    private static final int FEATURE_DATAS_UNREADY = 1;

    private volatile int identityStatus = FEATURE_DATAS_UNREADY;

    private android.os.Handler handler = new android.os.Handler(Looper.getMainLooper());

    private int mWidth = com.orbbec.utils.GlobalDef.RESOLUTION_X;
    private int mHeight = com.orbbec.utils.GlobalDef.RESOLUTION_Y;

    // 用于检测人脸。
    private FaceDetectManager faceDetectManager;
    private static final int IDENTITY_IDLE = 2;
    private static final int IDENTITYING = 3;
    private boolean isCheck=false;
    private boolean result=false;

    private String userIdOfMaxScore = "";
    private float maxScore = 0;
    private User user;
    private CommonDialog commonDialog;
    private CommonDialog pwdDialog;
    private int count=0;
    //上线改为false
    private boolean isShowPassword=false;
    private OpenNIHelper mOpenNIHelper;
    private VideoStream depthStream;
    private VideoStream rgbStream;
    private Thread thread;
    private boolean initOk;
    private boolean isLiveness=false;
    private Bitmap bitmap;
    //    private TextureView textureView;

    @Override
    public int getLayoutID() {
        return R.layout.activity_login;
    }

    @Override
    public void initView() {
        livnessTypeTip();

        findView();
        FaceUtils.instance().initFace(this);
        init();
    }

    private void findView() {
//        testView = (ImageView) findViewById(R.id.test_view1);
        userOfMaxSocre = (TextView) findViewById(R.id.user_of_max_score_tv1);
        previewView = (PreviewView) findViewById(R.id.preview_view1);
//        matchAvatorIv = (ImageView) findViewById(R.id.match_avator_iv1);
//        matchUserTv = (TextView) findViewById(R.id.match_user_tv1);
//        scoreTv = (TextView) findViewById(R.id.score_tv1);
//        facesetsCountTv = (TextView) findViewById(R.id.facesets_count_tv1);
//        detectDurationTv = (TextView) findViewById(R.id.detect_duration_tv1);
//        rgbLivenssDurationTv = (TextView) findViewById(R.id.rgb_liveness_duration_tv1);
//        rgbLivenessScoreTv = (TextView) findViewById(R.id.rgb_liveness_score_tv1);
//        featureDurationTv = (TextView) findViewById(R.id.feature_duration_tv1);

//        textureView = (TextureView) findViewById(R.id.texture_view);

        ViewGroup.LayoutParams layoutParams = headImage.getLayoutParams();
        layoutParams.width=FaceUtils.instance().getDisPlayWidth(this)*2/3;
        layoutParams.height=FaceUtils.instance().getDisPlayWidth(this)*2/3;

        mOpenNIHelper = new OpenNIHelper(this);
        mOpenNIHelper.requestDeviceOpen(this);
    }

    @OnClick({R.id.regist_button,R.id.system_manager})
    void click(final View v){
        if(!FaceSDKManager.getInstance().check()){
            return;
        }

        if(PreferencesUtil.getBoolean("login",false)){
            switch (v.getId()){
                case R.id.regist_button:
                    startActivity(new Intent(LoginActivity3.this,RegistActivity.class));
                    break;
                case R.id.system_manager:
                    startActivity(new Intent(LoginActivity3.this,ManagerActivity.class));
                    break;
            }
        }else{
            tipInputPassWord(new ClickListener() {
                @Override
                public void click() {
                    PreferencesUtil.putBoolean("login",true);
                    switch (v.getId()){
                        case R.id.regist_button:
                            startActivity(new Intent(LoginActivity3.this,RegistActivity.class));
                            break;
                        case R.id.system_manager:
//                Intent intent = new Intent(this, RgbVideoIdentityActivity.class);
//                intent.putExtra("group_id", "test");
//                startActivity(intent);

                            startActivity(new Intent(LoginActivity3.this,ManagerActivity.class));
                            break;
                    }
                }
            });
        }
    }

    private void livnessTypeTip () {
        int type = PreferencesUtil.getInt(LivenessSettingActivity.TYPE_LIVENSS, LivenessSettingActivity
                .TYPE_NO_LIVENSS);

        if (type == LivenessSettingActivity.TYPE_NO_LIVENSS) {
            Log.i("loginactivity","当前活体策略：无活体, 请选用普通USB摄像头");
        } else if (type == LivenessSettingActivity.TYPE_RGB_LIVENSS) {
            Log.i("loginactivity","当前活体策略：单目RGB活体, 请选用普通USB摄像头");
        } else if (type == LivenessSettingActivity.TYPE_RGB_IR_LIVENSS) {
            Log.i("loginactivity","当前活体策略：双目RGB+IR活体, 请选用RGB+IR摄像头，如：迪威泰双目摄像头");
        } else if (type == LivenessSettingActivity.TYPE_RGB_DEPTH_LIVENSS) {
            Log.i("loginactivity","当前活体策略：双目RGB+Depth活体，请选用RGB+Depth摄像头，"
                    + "如：如奥比中光mini双目摄像头");
        }
    }



    private void init() {

        faceDetectManager = new FaceDetectManager(getApplicationContext());
        // 从系统相机获取图片帧。
        final CameraImageSource cameraImageSource = new CameraImageSource(this);
        // 图片越小检测速度越快，闸机场景640 * 480 可以满足需求。实际预览值可能和该值不同。和相机所支持的预览尺寸有关。
        // 可以通过 camera.getParameters().getSupportedPreviewSizes()查看支持列表。
        // cameraImageSource.getCameraControl().setPreferredPreviewSize(1280, 720);
        cameraImageSource.getCameraControl().setPreferredPreviewSize(640, 480);

        // 设置最小人脸，该值越小，检测距离越远，该值越大，检测性能越好。范围为80-200
        FaceSDKManager.getInstance().getFaceDetector().setMinFaceSize(100);
        // FaceSDKManager.getInstance().getFaceDetector().setNumberOfThreads(4);
        // 设置预览
        cameraImageSource.setPreviewView(previewView);
        // 设置图片源
        faceDetectManager.setImageSource(cameraImageSource);
        // 设置人脸过滤角度，角度越小，人脸越正，比对时分数越高
        faceDetectManager.getFaceFilter().setAngle(20);

//        textureView.setOpaque(false);
//        // 不需要屏幕自动变黑。
//        textureView.setKeepScreenOn(true);

        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        if (isPortrait) {
            previewView.setScaleType(PreviewView.ScaleType.FIT_WIDTH);
            // 相机坚屏模式
            cameraImageSource.getCameraControl().setDisplayOrientation(CameraView.ORIENTATION_PORTRAIT);
        } else {
            previewView.setScaleType(PreviewView.ScaleType.FIT_HEIGHT);
            // 相机横屏模式
            cameraImageSource.getCameraControl().setDisplayOrientation(CameraView.ORIENTATION_HORIZONTAL);
        }

        setCameraType(cameraImageSource);
    }

    private void setCameraType(CameraImageSource cameraImageSource) {


        if(FaceUtils.isUSB){
            // 选择使用usb摄像头
             cameraImageSource.getCameraControl().setCameraFacing(ICameraControl.CAMERA_USB);
            // 如果不设置，人脸框会镜像，显示不准
            previewView.getTextureView().setScaleX(-1);
        }else {
            // 选择使用前置摄像头
            cameraImageSource.getCameraControl().setCameraFacing(ICameraControl.CAMERA_FACING_FRONT);
        }


        // 选择使用后置摄像头
//        cameraImageSource.getCameraControl().setCameraFacing(ICameraControl.CAMERA_FACING_BACK);
//        previewView.getTextureView().setScaleX(-1);
    }

    private void addListener() {
        // 设置回调，回调人脸检测结果。
        faceDetectManager.setOnFaceDetectListener(new FaceDetectManager.OnFaceDetectListener() {
            @Override
            public void onDetectFace(int retCode, FaceInfo[] infos, ImageFrame frame) {

                if(isShowPassword){
                    Log.i(TAG,"密码框已弹出");
                    return;
                }


                bitmap = Bitmap.createBitmap(frame.getArgb(), frame.getWidth(), frame.getHeight(), Bitmap.Config
                        .ARGB_8888);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        headImage.setImageBitmap(bitmap);
                    }
                });

//                if(!isLiveness&&FaceUtils.isUSB){
//                    return;
//                }
//                if(infos==null){
//                    Log.i("loginactivity","没有人脸");
//                }else if(retCode == FaceTracker.ErrCode.OK.ordinal() && infos != null&&!isCheck){
//                    Log.i("loginactivity",retCode+"***************"+infos.toString());
//                    isCheck=true;
////                    Intent intent = new Intent(LoginActivity.this, RgbVideoIdentityActivity.class);
////                    intent.putExtra("group_id", "test");
////                    startActivity(intent);
//
//                    //TODO 适配景深
//                    Constants.UsetType[] values = Constants.UsetType.values();
//                    for (int i=1;i<values.length;i++){
//                        if(result){
//                            return;
//                        }
//                        asyncIdentity(frame, infos,values[i].getValue());
//                    }
//                }

            }

        });

    }


    @Override
    protected void onResume() {
        super.onResume();
        checkSelfPermission();
        addListener();
        loadFeature2Memery();

        GPIO.statusPin(pin);
        GPIO.settingPin(pin);
        GPIO.closePin(pin);
    }

    @Override
    protected void onStart() {
        super.onStart();

        try{
            // 开始检测
            faceDetectManager.start();
            faceDetectManager.setUseDetect(true);
        }catch (Exception e){
            Log.i(TAG,e.getMessage());
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        try{
            // 结束检测。
            faceDetectManager.stop();
        }catch (Exception e){
            Log.i(TAG,e.getMessage());
        }

        if (initOk) {
            exit = true;
            if (thread != null) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            long time1 = System.currentTimeMillis();
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
    protected void onDestroy() {
        super.onDestroy();
        try{
            // 结束检测。
            faceDetectManager.stop();
            GPIO.closeSU();
        }catch (Exception e){
            Log.i(TAG,e.getMessage());
        }
        dismissDialog();
    }

    private void checkSelfPermission(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest
                .permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA}, 100);
        }
    }

    private ExecutorService es = Executors.newSingleThreadExecutor();
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
                for (int i=1;i<values.length;i++){
                    FaceApi.getInstance().loadFacesFromDB(values[i].getValue());
                    count+=FaceApi.getInstance().getGroup2Facesets().get(values[i].getValue()).size();
                }
                Log.i(TAG,"人脸数据加载完成，即将开始1：N");
                Log.i(TAG,"底库人脸个数：" + count);
                identityStatus = IDENTITY_IDLE;
            }
        });
    }

    private void asyncIdentity(final ImageFrame imageFrame, final FaceInfo[] faceInfos,final String guoupId) {
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
//                int liveType = PreferencesUtil.getInt(LivenessSettingActivity.TYPE_LIVENSS, LivenessSettingActivity
//                        .TYPE_NO_LIVENSS);
//                if (liveType ==  LivenessSettingActivity.TYPE_NO_LIVENSS) {
//                    identity(imageFrame, faceInfos[0],guoupId);
//                } else if (liveType ==  LivenessSettingActivity.TYPE_RGB_LIVENSS) {
//
//                    if (rgbLiveness(imageFrame, faceInfos[0]) > 0.9) {
//                        identity(imageFrame, faceInfos[0],guoupId);
//                    } else {
//                        tip("rgb活体分数过低");
//                        isCheck=false;
//                    }
//                }
                if (rgbLiveness(imageFrame, faceInfos[0]) > 0.9) {
                    identity(imageFrame, faceInfos[0],guoupId);
                } else {
//                    tip("rgb活体分数过低");
                    unPass();
                }
            }
        });
    }

    /**
     * 检测活体
     * @param imageFrame
     * @param faceInfo
     * @return
     */
    private float rgbLiveness(ImageFrame imageFrame, FaceInfo faceInfo) {

        final float rgbScore = FaceLiveness.getInstance().rgbLiveness(imageFrame.getArgb(), imageFrame
                .getWidth(), imageFrame.getHeight(), faceInfo.landmarks);

        return rgbScore;
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
                        return;
                    } else {
                        maxScore = score;
                        userOfMaxSocre.setText("userId：" + userId + "\nscore：" + score);
                        Log.i(TAG,"maxScore"+String.valueOf(maxScore));
                        tipRegist();
                        unPass();
                        return;
                    }
                } else {
                    userIdOfMaxScore = userId;
                    maxScore = score;
                }


                user = FaceApi.getInstance().getUserInfo(groupId, userId);
                if (user == null) {
                    unPass();
                    tipRegist();
                    return;
                }
                Log.i(TAG,"用户的信息:"+ user.getUserInfo());
//                List<Feature> featureList = user.getFeatureList();
//                if (featureList != null && featureList.size() > 0) {
//                    // featureTv.setText(new String(featureList.get(0).getFeature()));
//                    File faceDir = FileUitls.getFaceDirectory();
//                    if (faceDir != null && faceDir.exists()) {
//                        File file = new File(faceDir, featureList.get(0).getImageName());
//                        if (file != null && file.exists()) {
//                            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
//                            matchAvatorIv.setImageBitmap(bitmap);
//                        }
//                    }
//                }
                isCheck=false;
                result=true;

                UserEntity userEntity = JsonUtils.toEntity(user.getUserInfo(), UserEntity.class);

                String groupId1 = user.getGroupId();
                String type = convertName(groupId1);

                tipPass(String.format("\t\t尊敬的%s:%s \r\n" +
                        "欢迎您的光临！",type,userEntity.getName()));
                tipPass.setText("3D活体检测通过");
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        dismissDialog();
//                    }
//                },5000);
            }
        });
    }

    private void unPass() {
        isCheck=false;
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

    private void Initialization(){
        isCheck=false;
        result=false;
        user=null;
    }

    private void tipPass(String text){

        if(isShowPassword){
            return;
        }

        if(commonDialog!=null&&commonDialog.isShowing()){
            return;
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

    private void tipInputPassWord( final ClickListener listener){

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
                }else if(TextUtils.isEmpty(username)){
                    tip("请输入用户名");
                }else {
                    if("admin".equals(username)&&"admin".equals(password)){
                        listener.click();
                        dismissDialogPwd();
                    }else {
                        tip("用户名或密码错误");
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


    private String convertName(String name){
        Constants.UsetType[] values = Constants.UsetType.values();
        for (int i=0;i<values.length;i++){
            if(values[i].getValue().equals(name)){
                return values[i].getName();
            }
        }
        return "VIP";
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        FaceUtils.instance().initFace(this);
        try{
            // 开始检测
            faceDetectManager.start();
            faceDetectManager.setUseDetect(true);
        }catch (Exception e){
            Log.i(TAG,e.getMessage());
        }
    }

    @Override
    public void onDeviceOpened(UsbDevice usbDevice) {
        init(usbDevice);

        depthStream = VideoStream.create(this.device, SensorType.DEPTH);
        List<VideoMode> mVideoModes = depthStream.getSensorInfo().getSupportedVideoModes();

        for (VideoMode mode : mVideoModes) {
            int X = mode.getResolutionX();
            int Y = mode.getResolutionY();
            int fps = mode.getFps();

            if (X == mWidth && Y == mHeight && mode.getPixelFormat() == PixelFormat.DEPTH_1_MM) {
                depthStream.setVideoMode(mode);
            }

        }
        rgbStream = VideoStream.create(this.device, SensorType.COLOR);
        List<VideoMode> mColorVideoModes = rgbStream.getSensorInfo().getSupportedVideoModes();

        for (VideoMode mode : mColorVideoModes) {
            int X = mode.getResolutionX();
            int Y = mode.getResolutionY();
            int fps = mode.getFps();

            if (X == mWidth && Y == mHeight && mode.getPixelFormat() == PixelFormat.RGB888) {
                rgbStream.setVideoMode(mode);
            }
        }

        startThread();
        FaceLiveness.getInstance().setLivenessCallBack(new ILivenessCallBack() {
            @Override
            public void onCallback(LivenessModel livenessModel) {
                checkResult(livenessModel);
            }

            @Override
            public void onTip(int code, final String msg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tip(msg);
                    }
                });
            }

            @Override
            public void onCanvasRectCallback(LivenessModel livenessModel) {

            }
        });

    }

    @Override
    public void onDeviceOpenFailed(String s) {
        tip("3D活体检测设备打开失败");
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
        if ((type & FaceLiveness.MASK_IR) == FaceLiveness.MASK_IR) {
            boolean irScore = (model.getIrLivenessScore() > FaceEnvironment.LIVENESS_IR_THRESHOLD) ? true : false;
            if (!irScore) {
                livenessSuccess = false;
            } else {
                livenessSuccess &= irScore;
            }
        }
        if ((type & FaceLiveness.MASK_DEPTH) == FaceLiveness.MASK_DEPTH) {
            boolean depthScore = (model.getDepthLivenessScore() > FaceEnvironment.LIVENESS_DEPTH_THRESHOLD) ? true :
                    false;
            if (!depthScore) {
                livenessSuccess = false;
            } else {
                livenessSuccess &= depthScore;
            }
        }

        if (livenessSuccess) {
            isLiveness = true;

            if(isShowPassword){
                Log.i(TAG,"密码框已弹出");
                return;
            }
            FaceInfo faceInfo = model.getFaceInfo();
            if(faceInfo==null){
                Log.i("loginactivity","没有人脸");
            }else if( faceInfo != null&&!isCheck){
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

    private void init(UsbDevice device) {
        OpenNI.setLogAndroidOutput(false);
        OpenNI.setLogMinSeverity(0);
        OpenNI.initialize();

        List<DeviceInfo> opennilist = OpenNI.enumerateDevices();
        if (opennilist.size() <= 0) {
            Toast.makeText(this, " 没有depth检测设备", Toast.LENGTH_LONG).show();
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
            Toast.makeText(this, " 设备打开失败: " + device.getDeviceName(),
                    Toast.LENGTH_LONG).show();
            return;
        }
    }

    void startThread() {
        initOk = true;
        thread = new Thread() {

            @Override
            public void run() {

                List<VideoStream> streams = new ArrayList<VideoStream>();

                streams.add(depthStream);

                depthStream.start();

                while (!exit) {

                    try {
                        OpenNI.waitForAnyStream(streams, 2000);

                    } catch (TimeoutException e) {
                        e.printStackTrace();
                        continue;
                    }

                    synchronized (sync) {
                        ByteBuffer depthByteBuf = depthStream.readFrame().getData();
                        int depthLen = depthByteBuf.remaining();

                        byte[] depthByte = new byte[depthLen];

                        depthByteBuf.get(depthByte);

//                        final Bitmap bitmap = ImageUtils.RGB2Bitmap(rgbByte, mWidth, mHeight);

                        FaceLiveness.getInstance().setRgbBitmap(bitmap);
                        FaceLiveness.getInstance().setDepthData(depthByte);
                        FaceLiveness.getInstance().livenessCheck(mWidth, mHeight, 0X0101);
                    }
                }
            }
        };

        thread.start();
    }

    private Paint paint = new Paint();

    {
        paint.setColor(Color.YELLOW);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize(30);
    }

    RectF rectF = new RectF();

    /**
     * 绘制人脸框。
     *
     */
//    private void showFrame(ImageFrame imageFrame, FaceInfo[] faceInfos) {
//        Canvas canvas = textureView.lockCanvas();
//        if (canvas == null) {
//            return;
//        }
//        if (faceInfos == null || faceInfos.length == 0) {
//            // 清空canvas
//            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//            textureView.unlockCanvasAndPost(canvas);
//            return;
//        }
//        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//
//        FaceInfo faceInfo = faceInfos[0];
//
//
//        rectF.set(getFaceRect(faceInfo, imageFrame));
//
//        // 检测图片的坐标和显示的坐标不一样，需要转换。
//        previewView.mapFromOriginalRect(rectF);
//
//        float yaw  = Math.abs(faceInfo.headPose[0]);
//        float patch  = Math.abs(faceInfo.headPose[1]);
//        float roll  = Math.abs(faceInfo.headPose[2]);
//        if (yaw > 20 || patch > 20 || roll > 20) {
//            // 不符合要求，绘制黄框
//            paint.setColor(Color.YELLOW);
//
//            String text = "请正视屏幕";
//            float width = paint.measureText(text) + 50;
//            float x = rectF.centerX() - width / 2;
//            paint.setColor(Color.RED);
//            paint.setStyle(Paint.Style.FILL);
//            canvas.drawText(text, x + 25, rectF.top - 20, paint);
//            paint.setColor(Color.YELLOW);
//
//        } else {
//            // 符合检测要求，绘制绿框
//            paint.setColor(Color.GREEN);
//        }
//        paint.setStyle(Paint.Style.STROKE);
//        // 绘制框
//        canvas.drawRect(rectF, paint);
//        textureView.unlockCanvasAndPost(canvas);
//    }

    interface ClickListener{
        void click();
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

        return rect;
    }
}
