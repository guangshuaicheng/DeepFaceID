package face.yang.com.facerecognition.base;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.multidex.MultiDex;

import com.baidu.aip.db.DBManager;
import com.baidu.aip.manager.FaceEnvironment;
import com.baidu.aip.manager.FaceSDKManager;
import com.baidu.aip.utils.PreferencesUtil;
import com.baidu.idl.facesdk.FaceTracker;
import com.baidu.idl.license.AndroidLicenser;
import com.tencent.bugly.crashreport.CrashReport;

import java.lang.reflect.Field;

public class FaceApplication extends Application {

    public static FaceApplication application;

    @Override
    public void onCreate() {
        super.onCreate();
        application=this;
//        setState();
        initData();
        initRegister();

        CrashReport.initCrashReport(getApplicationContext(), "32aa43c97c", true);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    private void initData() {
        new Thread(){
            @Override
            public void run() {
                super.run();
                setName("FaceApplication");
                PreferencesUtil.initPrefs(application);
            }
        }.start();
    }

    private void initRegister() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                AppManager.getInstance().addActivity(activity);
            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                AppManager.getInstance().finishActivity(activity);
            }
        });
    }

    private void setState(){
        try {
            Class<?> aClass = Class.forName("com.baidu.idl.license.AndroidLicenser");
            Field mAuthorityStatus = aClass.getDeclaredField("deviceID");
            mAuthorityStatus.setAccessible(true);
            mAuthorityStatus.set(aClass.newInstance(), "1D00A429B48FE790F670B00626AC9BD1");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }


}
