package face.yang.com.facerecognition.utils;

import android.util.Log;

import com.stericson.RootTools.RootTools;

import java.io.DataOutputStream;
import java.io.IOException;

public class GPIO {

    private static DataOutputStream dataOutputStream;

    public static void openSU(){
        Process su = null;
        try {
//                su = Runtime.getRuntime().exec("/system/xbin/su");
            su = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(su.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("gpio","打开gpio文件失败");
        }
    }

    public static void closeSU(){
        if(!RootTools.isAccessGiven()){
            Log.i("gpio","没有root权限");
            return;
        }
        if(dataOutputStream!=null){
            try {
                dataOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //打开gpio引脚，即status_led连接的引脚
    public static void statusPin(String index){
        if(!RootTools.isAccessGiven()){
            Log.i("gpio","没有root权限");
            return;
        }
        try {
            if(dataOutputStream==null){
                openSU();
            }
            dataOutputStream.writeBytes("echo "+index+" > /sys/class/gpio/export"+"\n");
            dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //设置引脚功能为输出
    public static void settingPin(String index){
        if(!RootTools.isAccessGiven()){
            Log.i("gpio","没有root权限");
            return;
        }
        try {
            if(dataOutputStream==null){
                openSU();
            }
            dataOutputStream.writeBytes("echo out > /sys/class/gpio/gpio"+index+"/direction"+"\n");
            dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //
    public static void openPin(String index){
        if(!RootTools.isAccessGiven()){
            Log.i("gpio","没有root权限");
            return;
        }
        try {
            if(dataOutputStream==null){
                openSU();
            }
            dataOutputStream.writeBytes("echo 1 > /sys/class/gpio/gpio"+index+"/value"+"\n");
            dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //
    public static void closePin(String index){
        if(!RootTools.isAccessGiven()){
            Log.i("gpio","没有root权限");
            return;
        }
        try {
            if(dataOutputStream==null){
                openSU();
            }
            dataOutputStream.writeBytes("echo 0 > /sys/class/gpio/gpio"+index+"/value"+"\n");
            dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
