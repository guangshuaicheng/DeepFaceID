package face.yang.com.facerecognition;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.aip.api.FaceApi;
import com.baidu.aip.entity.Group;
import com.baidu.aip.manager.FaceSDKManager;
import com.baidu.aip.utils.PreferencesUtil;

import junit.framework.Test;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemClick;
import face.yang.com.facerecognition.base.BaseActivity;
import face.yang.com.facerecognition.ui.activity.LivenessSettingActivity;
import face.yang.com.facerecognition.ui.activity.OrbbecLivenessDetectActivity;
import face.yang.com.facerecognition.ui.activity.RegActivity;
import face.yang.com.facerecognition.ui.activity.RgbVideoIdentityActivity;
import face.yang.com.facerecognition.ui.activity.RgbVideoMatchImageActivity;
import face.yang.com.facerecognition.ui.activity.TestActivity;
import face.yang.com.facerecognition.ui.activity.user.LoginActivity;
import face.yang.com.facerecognition.utils.FaceUtils;

public class MainActivity extends BaseActivity {

    @BindView(R.id.regist_device)
    Button button;
    @BindView(R.id.regist_user)
    Button registButton;

    @Override
    public int getLayoutID() {
        return R.layout.activity_main;
    }

    @Override
    public void initView() {
        FaceUtils.instance().initFace(this);
        livnessTypeTip();
        List<Group> groupList = FaceApi.getInstance().getGroupList(0, 1000);
        if(groupList.size()==0){
            FaceUtils.instance().addGroup("test");
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                FaceSDKManager.getInstance().showActivation();
//                MainActivity.this.startActivity(new Intent(MainActivity.this, OrbbecLivenessDetectActivity.class));
                Intent intent = new Intent(mContext, TestActivity.class);
                intent.putExtra("source", 1);
                startActivityForResult(intent, 2);
            }
        });

        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    Thread.sleep(3000);
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    @OnClick({R.id.regist_user,R.id.check_user})
    void click(View v){
        switch (v.getId()){
            case R.id.regist_user:
//                startActivity(new Intent(this, RegActivity.class));
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                break;
            case R.id.check_user:

//                startActivity(new Intent(this, RgbVideoMatchImageActivity.class));

                Intent intent = new Intent(this, RgbVideoIdentityActivity.class);
                intent.putExtra("group_id", "test");
                startActivity(intent);
                break;
        }
    }

    private void checkSelfPermission(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest
                .permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA}, 100);
            return;
        }
    }

    private void livnessTypeTip () {
        int type = PreferencesUtil.getInt(LivenessSettingActivity.TYPE_LIVENSS, LivenessSettingActivity
                .TYPE_NO_LIVENSS);

        if (type == LivenessSettingActivity.TYPE_NO_LIVENSS) {
            Toast.makeText(this, "当前活体策略：无活体, 请选用普通USB摄像头", Toast.LENGTH_LONG).show();
        } else if (type == LivenessSettingActivity.TYPE_RGB_LIVENSS) {
            Toast.makeText(this, "当前活体策略：单目RGB活体, 请选用普通USB摄像头", Toast.LENGTH_LONG).show();
        } else if (type == LivenessSettingActivity.TYPE_RGB_IR_LIVENSS) {
            Toast.makeText(this, "当前活体策略：双目RGB+IR活体, 请选用RGB+IR摄像头，如：迪威泰双目摄像头",
                    Toast.LENGTH_LONG).show();
        } else if (type == LivenessSettingActivity.TYPE_RGB_DEPTH_LIVENSS) {
            Toast.makeText(this, "当前活体策略：双目RGB+Depth活体，请选用RGB+Depth摄像头，"
                    + "如：如奥比中光mini双目摄像头", Toast.LENGTH_LONG).show();
        }
    }
}
