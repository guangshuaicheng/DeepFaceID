package face.yang.com.facerecognition.ui.activity.user;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.baidu.aip.db.DBManager;
import com.baidu.aip.manager.FaceSDKManager;
import com.baidu.aip.utils.FileUitls;
import com.baidu.aip.utils.PreferencesUtil;

import face.yang.com.facerecognition.R;
import face.yang.com.facerecognition.ui.activity.LivenessSettingActivity;

public class SplashActivity extends Activity {

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(FileUitls.checklicense(SplashActivity.this, FaceSDKManager.LICENSE_NAME)){
//                startActivity(new Intent(SplashActivity.this, LoginActivity3.class));
                startActivity(new Intent(SplashActivity.this, face.yang.com.facerecognition.ui.activity.user.OrbbecLivenessDetectActivity.class));
                finish();
            }else {
                handler.sendEmptyMessageDelayed(0,1000);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去除状态栏
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        PreferencesUtil.putBoolean("login",false);

        livnessTypeTip();
        // 使用人脸1：n时使用
        DBManager.getInstance().init(this);
        FaceSDKManager.getInstance().init(this);

        handler.sendEmptyMessageDelayed(0,1000);

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(null);
        handler=null;
    }
}
