package face.yang.com.facerecognition.serialport;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;

public class SerialPortUtils {

    private final static String TAG="SerialPortUtils";

    /**
     * 串口名称
     */
    private String PATH = "/dev/ttyS1";
    /**
     * 波特率
     */
    private int baudrate = 115200;

    private static SerialPortUtils instance;
    private PortPrinterBase portPrinterBase;
    public OutputStream mOutputStream;

    public static SerialPortUtils instance(){
        if(instance==null){
            instance = new SerialPortUtils();
        }
        return instance;
    }

    public void open(Context context){
        LockerSerialportUtil.init(context, PATH, baudrate, new LockerPortInterface() {

            @Override
            public void onLockerDataReceived(byte[] buffer, int size, String path) {
                String result = new String(buffer,0,size);
                Log.e(TAG,"onLockerDataReceived===="+result);
            }

            @Override
            public void onLockerOutputStream(OutputStream outputStream) {
                mOutputStream = outputStream;
                portPrinterBase = new PortPrinterBase(outputStream,"1");
            }
        });
    }

    public void clost(){
        LockerSerialportUtil.getInstance().closeSerialPort();
    }

    public void send(String cmd){
        try {
            mOutputStream.write(cmd.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendParams(byte[] bytes){
        if(mOutputStream == null){
            return;
        }
        // TODO 发送指令
        try {
            mOutputStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendParams(int ...params){
        if(mOutputStream == null){
            return;
        }
        // TODO 发送指令
        try {
            StringBuffer stringBuffer = new StringBuffer();
            for (int param : params){
                stringBuffer.append(param+",");
                mOutputStream.write(param);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
