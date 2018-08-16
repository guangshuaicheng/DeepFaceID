package face.yang.com.facerecognition.ui.activity.user;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.Serializable;

import butterknife.BindView;
import butterknife.OnClick;
import face.yang.com.facerecognition.R;
import face.yang.com.facerecognition.base.BaseActivity;
import face.yang.com.facerecognition.entity.UserEntity;
import face.yang.com.facerecognition.utils.Constants;

public class ManagerItemActivity extends BaseActivity {

    @BindView(R.id.info_line)
    LinearLayout linearLayout;
    @BindView(R.id.head_image)
    ImageView imageView;

    @Override
    public int getLayoutID() {
        return R.layout.activity_manageritem;
    }

    @Override
    public void initView() {

        Bundle extras = getIntent().getExtras();
        UserEntity userInfo = (UserEntity) extras.getSerializable("entity");
        addItem("姓名",userInfo.getName());
        addItem("性别",userInfo.getSex());
        addItem("年龄",userInfo.getAge());
        addItem("身份证号",userInfo.getId());
        addItem("公司",userInfo.getCompany());
        addItem("地址",userInfo.getAddress());
        addItem("类别",convertType(userInfo.getType()));
        addItem("电话",userInfo.getTell());

        Glide.with(this).load(userInfo.getImagePath()).into(imageView);

    }

    @OnClick({R.id.regist_back})
    void click(View v){
        switch (v.getId()){
            case R.id.regist_back:
                finish();
                break;
        }
    }

    private void addItem(String n,String v){
        View inflate = View.inflate(this, R.layout.item_info, null);
        TextView name = inflate.findViewById(R.id.name);
        TextView value = inflate.findViewById(R.id.value);
        name.setText(n);
        value.setText(v);

        linearLayout.addView(inflate);
    }

    private String convertType(String type){
        Constants.UsetType[] values = Constants.UsetType.values();
        for (int i=0;i<values.length;i++){
            if(values[i].getValue().equals(type)){
                return values[i].getName();
            }
        }
        return "";
    }
}
