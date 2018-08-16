package face.yang.com.facerecognition.utils;

import com.google.gson.Gson;

public class JsonUtils {
    static {
        gson = new Gson();
    }

    private static Gson gson;

    public static String toJson(Object object){
        String s = gson.toJson(object);
        return s;
    }

    public static <T> T toEntity(String json,Class<T> t){
        return gson.fromJson(json,t);
    }
}
