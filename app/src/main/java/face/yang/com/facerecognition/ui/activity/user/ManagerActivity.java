package face.yang.com.facerecognition.ui.activity.user;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.baidu.aip.api.FaceApi;
import com.baidu.aip.entity.Feature;
import com.baidu.aip.entity.User;
import com.baidu.aip.utils.FileUitls;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import face.yang.com.facerecognition.R;
import face.yang.com.facerecognition.adapter.ManagerAdapter;
import face.yang.com.facerecognition.base.BaseActivity;
import face.yang.com.facerecognition.entity.UserEntity;
import face.yang.com.facerecognition.utils.Constants;
import face.yang.com.facerecognition.utils.JsonUtils;

public class ManagerActivity extends BaseActivity {

    @BindView(R.id.listview)
    ListView listView;
    @BindView(R.id.edit)
    TextView editText;

    private List<UserEntity> groupList = new ArrayList<>();

    private Handler handler=new Handler(Looper.getMainLooper());
    private ManagerAdapter managerAdapter;

    @Override
    public int getLayoutID() {
        return R.layout.activity_manager;
    }

    @Override
    public void initView() {
        if(Constants.userList==null){
            getUser();
        }else {
            managerAdapter = new ManagerAdapter(ManagerActivity.this, Constants.userList);
            listView.setAdapter(managerAdapter);
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Intent intent = new Intent(ManagerActivity.this, ManagerItemActivity.class);
                Bundle bu = new Bundle();
                bu.putSerializable("entity",Constants.userList.get(i));
                intent.putExtras(bu);
                ManagerActivity.this.startActivity(intent);
            }
        });
    }

    @OnClick({R.id.regist_back,R.id.edit,R.id.regist})
    void click(View v){
        switch (v.getId()){
            case R.id.regist_back:
                finish();
                break;
            case R.id.regist:
                startActivityForResult(new Intent(this,RegistActivity.class),0);
                break;
            case R.id.edit:
                if(managerAdapter!=null){
                    if(!managerAdapter.isDelete()){
                        editText.setText("取消");
                    }else {
                        editText.setText("编辑");
                    }
                    managerAdapter.setDelete(!managerAdapter.isDelete());
                    managerAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

    /**
     * 耗时
     */
    private void getUser(){
        Constants.UsetType[] values = Constants.UsetType.values();
        groupList.clear();
        UserEntity userEntity=null;
        for (int i=0;i<values.length;i++){
            List<User> userList = FaceApi.getInstance().getUserList(values[i].getValue());
            for (User u:userList){
                userEntity = JsonUtils.toEntity(u.getUserInfo(), UserEntity.class);

                User user = FaceApi.getInstance().getUserInfo(u.getGroupId(), u.getUserId());
                List<Feature> featureList = user.getFeatureList();
                if (featureList != null && featureList.size() > 0) {
                    // featureTv.setText(new String(featureList.get(0).getFeature()));
                    File faceDir = FileUitls.getFaceDirectory();
                    if (faceDir != null && faceDir.exists()) {
                        File file = new File(faceDir, featureList.get(0).getImageName());
                        if (file != null && file.exists()) {
                            userEntity.setImagePath(file.getAbsolutePath());
                        }
                    }
                }

                if(userEntity!=null){
                    groupList.add(userEntity);
                }
            }
        }

        handler.post(new Runnable() {
            @Override
            public void run() {
                Constants.userList=groupList;
                managerAdapter = new ManagerAdapter(ManagerActivity.this, Constants.userList);
                listView.setAdapter(managerAdapter);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(null);
        handler=null;

        startActivity(new Intent(this, OrbbecLivenessDetectActivity.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==0){
            if(managerAdapter!=null){
                managerAdapter.notifyDataSetChanged();
            }else {
                getUser();
            }
        }
    }

}
