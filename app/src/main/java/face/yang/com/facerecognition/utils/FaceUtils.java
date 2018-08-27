package face.yang.com.facerecognition.utils;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.WindowManager;

import com.baidu.aip.api.FaceApi;
import com.baidu.aip.db.DBManager;
import com.baidu.aip.entity.Group;
import com.baidu.aip.manager.FaceEnvironment;
import com.baidu.aip.manager.FaceSDKManager;

import face.yang.com.facerecognition.R;

public class FaceUtils {

    public static boolean isUSB=false;

    public static FaceUtils faceUtils;

    public static FaceUtils instance(){
        if(faceUtils==null){
            faceUtils=new FaceUtils();
        }
        return faceUtils;
    }


    private static final String TAG = "FaceUtils";

    public void initFace(Context context){


        FaceEnvironment faceEnvironment = new FaceEnvironment();
        faceEnvironment.setBlurrinessThreshold(0.7f);
        faceEnvironment.setIlluminationThreshold(50f);
        faceEnvironment.setNotFaceThreshold(0.8f);
        faceEnvironment.setOcclulationThreshold(0.3f);
        faceEnvironment.setMinFaceSize(90);

        faceEnvironment.setPitch(FaceEnvironment.VALUE_HEAD_PITCH);
        faceEnvironment.setRoll(FaceEnvironment.VALUE_HEAD_ROLL);
        faceEnvironment.setYaw(FaceEnvironment.VALUE_HEAD_YAW);

        FaceSDKManager.getInstance().getFaceDetector().setFaceEnvironment(faceEnvironment);

        // 使用人脸1：n时使用
        DBManager.getInstance().init(context);
        FaceSDKManager.getInstance().init(context);
        FaceSDKManager.getInstance().setSdkInitListener(new FaceSDKManager.SdkInitListener() {
            @Override
            public void initStart() {
                Log.i(TAG,"开始初始化");
            }

            @Override
            public void initSuccess() {
                Log.i(TAG,"sdk init success2");
            }

            @Override
            public void initFail(int errorCode, String msg) {
                Log.i(TAG,"sdk init fail:" + msg+"***"+errorCode);
            }
        });
    }

    /**
     * 检查sdk状态
     * @return
     */
    public int checkStatus(){
        int statu=2;
        if (FaceSDKManager.getInstance().initStatus() == FaceSDKManager.SDK_UNACTIVATION) {
            Log.i(TAG,"SDK还未激活，请先激活");
            //FaceSDKManager.getInstance().showActivation();
            statu=1;
        } else if (FaceSDKManager.getInstance().initStatus() == FaceSDKManager.SDK_UNINIT) {
            Log.i(TAG,"SDK还未初始化完成，请先初始化");
            statu=2;
        } else if (FaceSDKManager.getInstance().initStatus() == FaceSDKManager.SDK_INITING) {
            Log.i(TAG,"SDK正在初始化，请稍后再试");
            statu=3;
        }
        return statu;
    }

    /**
     * 添加用户组
     * groupId、字母、下划线中的一个或者多个组合
     */
    public boolean addGroup(String groupId){

//        Pattern pattern = Pattern.compile("^[0-9a-zA-Z_-]{1,}$");
//        Matcher matcher = pattern.matcher(groupId);
//        boolean matches = matcher.matches();

        Group group = new Group();
        group.setGroupId(groupId);
        boolean ret = FaceApi.getInstance().groupAdd(group);
        if(ret){
            Log.i(TAG,"添加用户组成功呢"+groupId);
        }else {
            Log.i(TAG,"添加用户组失败"+groupId);
        }
        return ret;
    }

    /**
     * 人脸注册
     */
    public void userRegister(){

    }

    public static int dp2px(Context context, float dpVal) {

        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,

                dpVal, context.getResources().getDisplayMetrics());

    }

    public String getRealPathFromURI(Context context,Uri contentURI) {
        String result = null;
        Cursor cursor = null;
        try {
            cursor =context.getContentResolver().query(contentURI, null, null, null, null);
            if (cursor == null) { // Source is Dropbox or other similar local file path
                result = contentURI.getPath();
            } else {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                result = cursor.getString(idx);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return result;
    }


    public int getDisPlayWidth(Context context){
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;         // 屏幕宽度（像素）
        int height = dm.heightPixels;
        if(width<height){
            return width;
        }else {
            return height;
        }
    }


    public void playerSuccess(Context context){
        player(context,R.raw.success);
    }

    public void registSuccess(Context context){
        player(context,R.raw.regist_success);
    }

    public void getFaceSuccess(Context context){
        player(context,R.raw.getfacesuccess);
    }

    public void unregist(Context context){
//        player(context,R.raw.unregist);
    }

    public void tipInputPassWord(Context context){
        player(context,R.raw.inputpassword);
    }

    public void tipinputname(Context context){
        player(context,R.raw.inpoutname);
    }

    public void pwsErr(Context context){
        player(context,R.raw.err);
    }

    private void player(Context context,int res) {
        MediaPlayer mediaPlayer = MediaPlayer.create(context, res);
        mediaPlayer.setVolume(1.0F, 1.0F);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });
        mediaPlayer.start();
    }


}
