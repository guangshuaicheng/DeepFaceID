package face.yang.com.facerecognition.base;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import butterknife.ButterKnife;
import face.yang.com.facerecognition.ui.activity.OrbbecLivenessDetectActivity;

public abstract class BaseActivity extends AppCompatActivity {

    protected Activity mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        int layoutID = getLayoutID();
        setContentView(layoutID);
        ButterKnife.bind(this);

        initView();
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//
//    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    public abstract int getLayoutID();
    public abstract void initView();

    public void tip(final String tip) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BaseActivity.this, tip, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
