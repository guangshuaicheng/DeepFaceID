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
            Log.e("gpio","打开1");
            su = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(su.getOutputStream());
            Log.e("gpio","打开2");
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
            dataOutputStream.writeBytes("chmod 777 /sys/class/gpio/gpio"+index+"/value\n");
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
            Log.i("gpio","gpio设置");
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
            Log.i("gpio","gpio打开");
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
            Log.i("gpio","gpio关闭");
            dataOutputStream.writeBytes("echo 0 > /sys/class/gpio/gpio"+index+"/value"+"\n");
            dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
