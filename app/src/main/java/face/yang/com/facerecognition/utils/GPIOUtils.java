//package face.yang.com.facerecognition.utils;
//
//import com.google.android.things.pio.Gpio;
//import com.google.android.things.pio.PeripheralManager;
//
//import java.io.IOException;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//
//public class GPIOUtils {
//
//    public static Map<String,Gpio> gpioMap=new LinkedHashMap<>();
//
//    static {
//
//        PeripheralManager manager = PeripheralManager.getInstance();
//        List<String> gpioList = manager.getGpioList();
//
//        for (String name:gpioList){
//            try {
//                Gpio ledPin = manager.openGpio(name);
//                ledPin.setEdgeTriggerType(Gpio.EDGE_NONE);
//                ledPin.setActiveType(Gpio.ACTIVE_HIGH);
//                ledPin.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
//                gpioMap.put(name,ledPin);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//    }
//
//    public static void control(boolean open){
//        for (Map.Entry<String,Gpio> entry:gpioMap.entrySet()){
//            try {
//                entry.getValue().setValue(open);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    public static void close(){
//        try {
//            for (Map.Entry<String,Gpio> entry:gpioMap.entrySet()){
//                entry.getValue().close();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        gpioMap.clear();
//    }
//}
