package face.yang.com.facerecognition.ui.activity.user;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.aip.ImageFrame;
import com.baidu.aip.api.FaceApi;
import com.baidu.aip.entity.ARGBImg;
import com.baidu.aip.entity.Feature;
import com.baidu.aip.entity.User;
import com.baidu.aip.face.FaceCropper;
import com.baidu.aip.face.FaceDetectManager;
import com.baidu.aip.face.FileImageSource;
import com.baidu.aip.manager.FaceDetector;
import com.baidu.aip.manager.FaceSDKManager;
import com.baidu.aip.utils.FeatureUtils;
import com.baidu.aip.utils.FileUitls;
import com.baidu.aip.utils.ImageUtils;
import com.baidu.idl.facesdk.FaceInfo;
import com.baidu.idl.facesdk.FaceTracker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.OnClick;
import face.yang.com.facerecognition.R;
import face.yang.com.facerecognition.base.AppManager;
import face.yang.com.facerecognition.base.BaseActivity;
import face.yang.com.facerecognition.base.FaceApplication;
import face.yang.com.facerecognition.entity.UserEntity;
import face.yang.com.facerecognition.utils.Constants;
import face.yang.com.facerecognition.utils.FaceUtils;
import face.yang.com.facerecognition.utils.JsonUtils;

public class RegistActivity extends BaseActivity {

    public static final int SOURCE_REG = 1;
    private static final int REQUEST_CODE_AUTO_DETECT = 100;
    private static final int REQUEST_CODE_PICK_IMAGE = 1000;
    private String uid=System.currentTimeMillis()+"";
    private Handler handler = new Handler(Looper.getMainLooper());

    @BindView(R.id.regist_head)
    ImageView regist_head;
    @BindView(R.id.regist_scrollview)
    ScrollView regist_scrollview;
    @BindView(R.id.item_Line)
    LinearLayout item_Line;

    // 注册时使用人脸图片路径。
    private String faceImagePath;

    private boolean isupload=false;

    private List<View> itemView=new ArrayList<>();
    private FaceDetectManager detectManager;
    private UserEntity userEntity;
    private ProgressDialog progressDialog;

    @Override
    public int getLayoutID() {
        return R.layout.activity_regist;
    }

    @Override
    public void initView() {
        itemView.add(addItem("姓名*",null ));
        itemView.add(addItem("性别",null ));
        itemView.add(addItem("年龄",null ));
        itemView.add(addItem("身份证号",null ));
        itemView.add(addItem("公司",null ));
        itemView.add(addItem("地址",null ));
        itemView.add(addItem("类别*",null ));
        itemView.add(addItem("电话",null ));

        detectManager = new FaceDetectManager(getApplicationContext());

        FaceUtils.instance().initFace(this);

    }

    private View addItem(String name, TextWatcher textWatcher){

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.leftMargin= FaceUtils.dp2px(this,30);
        layoutParams.rightMargin=FaceUtils.dp2px(this,30);
        layoutParams.topMargin=FaceUtils.dp2px(this,10);

        if(name.equals("类别*")){
            View inflate = View.inflate(this, R.layout.item_regist_type,null);
            TextView nameText = inflate.findViewById(R.id.item_name);
            TextView valueT = inflate.findViewById(R.id.item_value);
            valueT.setText(Constants.UsetType.white.getName());
            nameText.setText(name);
            item_Line.addView(inflate,layoutParams);
            inflate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectSing();
                }
            });
            return inflate;
        }else {
            View inflate = View.inflate(this, R.layout.item_regist,null);
            TextView nameText = inflate.findViewById(R.id.item_name);
            EditText valueText = inflate.findViewById(R.id.item_value);
            valueText.clearFocus();
            hideKeyboard(valueText);

            nameText.setText(name);
            item_Line.addView(inflate,layoutParams);
            return inflate;
        }
    }



    @OnClick({R.id.regist_back,R.id.regist_back2,R.id.regist_ticket,R.id.regist_upload,R.id.regist_confirm})
    void click(View v){
        switch (v.getId()){
            case R.id.regist_back:
            case R.id.regist_back2:
                finish();
                break;
            case R.id.regist_ticket:
                ticketPictue();
                break;
            case R.id.regist_upload:
                uploadImage();
                break;
            case R.id.regist_confirm:
                showLoading();
                confirm();
                break;
        }
    }

    private void uploadImage() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE },
                    100);
            return;
        }
        faceImagePath = null;
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    /**
     * 获取照片
     */
    private void ticketPictue() {

        faceImagePath=null;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest
                .permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA}, 100);
            return;
        }

        Intent intent = new Intent(this, RegistActivity2.class);
//        Intent intent = new Intent(this, RgbIrLivenessActivity.class);
        intent.putExtra("source", SOURCE_REG);
        startActivityForResult(intent, REQUEST_CODE_AUTO_DETECT);
    }

    private void confirm() {

        if(isupload||faceImagePath==null){
            tip("请拍照上传!");
            dismissLoading();
            return;
        }

        String name = getValue(0);
        String sex = getValue(1);
        String age = getValue(2);
        String ID = getValue(3);
        String company = getValue(4);
        String address = getValue(5);
        String tell = getValue(7);

        if(TextUtils.isEmpty(name)){
            tip("请输入姓名!");
            dismissLoading();
            return;
        }else if(TextUtils.isEmpty(typeValue)) {
            tip("请选择类型!");
            dismissLoading();
            return;
        }

        uid = System.currentTimeMillis()+"";

        userEntity = new UserEntity();
        userEntity.setName(name);
        userEntity.setSex(sex);
        userEntity.setAddress(address);
        userEntity.setId(ID);
        userEntity.setAge(age);
        userEntity.setTell(tell);
        userEntity.setCompany(company);
        userEntity.setType(typeValue);
        userEntity.setUid(uid);

        String user = JsonUtils.toJson(userEntity);
//        PreferencesUtil.putString(username,user );
//
//        String string = PreferencesUtil.getString(Constants.groupKey, "");
//        GroupEntity groupEntity = new GroupEntity();
//        List<String> strings=null;
//        if(TextUtils.isEmpty(string)){
//            strings= new ArrayList<>();
//        }else {
//            GroupEntity entity = JsonUtils.toEntity(string, GroupEntity.class);
//            strings=entity.getUsername();
//        }
//        strings.add(username);
//        groupEntity.setUsername(strings);
//        PreferencesUtil.putString(Constants.groupKey,JsonUtils.toJson(groupEntity));

        register(faceImagePath,user,typeValue);
    }

    private String getValue(int index){
        EditText nameView = itemView.get(index).findViewById(R.id.item_value);
        String name = nameView.getText().toString().trim();
        return name;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_AUTO_DETECT && data != null) {
            faceImagePath = data.getStringExtra("file_path");

            Bitmap bitmap = BitmapFactory.decodeFile(faceImagePath);
            regist_head.setImageBitmap(bitmap);
            FaceUtils.instance().getFaceSuccess(FaceApplication.application);
        }else if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            String filePath = FaceUtils.instance().getRealPathFromURI(this,uri);
            detect(filePath);
            FaceUtils.instance().getFaceSuccess(FaceApplication.application);
        }
    }


    // 从相册检测。
    private void detect(final String filePath) {

        FileImageSource fileImageSource = new FileImageSource();
        fileImageSource.setFilePath(filePath);
        detectManager.setImageSource(fileImageSource);
        detectManager.setUseDetect(true);
        detectManager.setOnFaceDetectListener(new FaceDetectManager.OnFaceDetectListener() {
            @Override
            public void onDetectFace(int status, FaceInfo[] faces, ImageFrame frame) {
                if (faces != null && status != FaceTracker.ErrCode.NO_FACE_DETECTED.ordinal()
                        && status != FaceTracker.ErrCode.UNKNOW_TYPE.ordinal()) {
                    final Bitmap cropBitmap = FaceCropper.getFace(frame.getArgb(), faces[0], frame.getWidth());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            regist_head.setImageBitmap(cropBitmap);
                        }
                    });

                    // File file = File.createTempFile(UUID.randomUUID().toString() + "", ".jpg");
                    File faceDir = FileUitls.getFaceDirectory();
                    if (faceDir != null) {
                        String imageName = UUID.randomUUID().toString();
                        File file = new File(faceDir, imageName);
                        // 压缩人脸图片至300 * 300，减少网络传输时间
                        ImageUtils.resize(cropBitmap, file, 300, 300);
                        RegistActivity.this.faceImagePath = file.getAbsolutePath();
                    } else  {
                        tip("注册人脸目录未找到");
                    }
                } else {
                    tip("未检测到人脸，可能原因：人脸太小（必须大于最小检测人脸minFaceSize），或者人脸角度太大，人脸不是朝上");
                }
            }
        });
        detectManager.start();
    }


    private void register(final String filePath, final String userInfo, final String type) {

        if (TextUtils.isEmpty(userInfo)) {
            Toast.makeText(this, "userid不能为空", Toast.LENGTH_SHORT).show();
            dismissLoading();
            return;
        }

        /*
         * 用户id（由数字、字母、下划线组成），长度限制128B
         * uid为用户的id,百度对uid不做限制和处理，应该与您的帐号系统中的用户id对应。
         *
         */
//        final String uid = UUID.randomUUID().toString();
        // String uid = 修改为自己用户系统中用户的id;

        if (TextUtils.isEmpty(faceImagePath)) {
            Toast.makeText(this, "人脸文件不存在", Toast.LENGTH_LONG).show();
            dismissLoading();
            return;
        }
        final File file = new File(filePath);
        if (!file.exists()) {
            Toast.makeText(this, "人脸文件不存在", Toast.LENGTH_LONG).show();
            dismissLoading();
            return;
        }


        final User user = new User();
        user.setUserId(uid);
        user.setUserInfo(userInfo);
        user.setGroupId(type);

        Executors.newSingleThreadExecutor().submit(new Runnable() {

            @Override
            public void run() {
                ARGBImg argbImg = FeatureUtils.getARGBImgFromPath(filePath);
                byte[] bytes = new byte[2048];
                int ret = FaceSDKManager.getInstance().getFaceFeature().faceFeature(argbImg, bytes);
                if (ret == FaceDetector.NO_FACE_DETECTED) {
                    tip("人脸太小，或者人脸角度太大，人脸不是朝上");
                } else if (ret != -1) {
                    dismissLoading();
                    Feature feature = new Feature();
                    feature.setGroupId(type);
                    feature.setUserId(uid);
                    feature.setFeature(bytes);
                    feature.setImageName(file.getName());

                    user.getFeatureList().add(feature);
                    File faceFile = new File(faceImagePath);
                    userEntity.setImagePath(faceImagePath);
                    if(Constants.userList!=null&&Constants.userList.contains(userEntity)){
                        Constants.userList.add(userEntity);
                        finish();
                        tip("注册成功");
                        isupload=true;
                    }else
                    if (faceFile.exists()&&FaceApi.getInstance().userAdd(user)) {
                        if(Constants.userList!=null){
                            Constants.userList.add(userEntity);
                        }
//                        AppManager.getInstance().finishActivity(LoginActivity.class);
//                        RegistActivity.this.startActivity(new Intent(RegistActivity.this,LoginActivity.class));
                        tip("注册成功");
                        FaceUtils.instance().registSuccess(FaceApplication.application);
                        isupload=true;
                        finish();
//                        DBManager.getInstance().init(RegistActivity.this);

                    } else {
                        tip("注册失败,请重新上传图片");
                    }

                } else {
                    tip("抽取特征失败");
                }
                userEntity=null;
                dismissLoading();
            }
        });
    }

    private void hideKeyboard(View view){

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

    }

    private String typeValue=Constants.UsetType.white.getValue();

    private void selectSing(){
        typeValue="";
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final Constants.UsetType[] values = Constants.UsetType.values();
        String[] types=new String[]{values[0].getName(),values[1].getName(),values[2].getName(),values[3].getName(),values[4].getName()};
        builder.setSingleChoiceItems(types, 1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                typeValue=values[i].getValue();
                TextView nameView = itemView.get(6).findViewById(R.id.item_value);
                nameView.setText(values[i].getName());
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    private void showLoading(){
        progressDialog = new ProgressDialog(this);
        progressDialog.show();
    }

    private void dismissLoading(){
        if(progressDialog!=null&&progressDialog.isShowing()){
            progressDialog.dismiss();
            progressDialog=null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        startActivity(new Intent(this, OrbbecLivenessDetectActivity.class));
    }
}
