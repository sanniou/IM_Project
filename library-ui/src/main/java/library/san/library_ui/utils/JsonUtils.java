package library.san.library_ui.utils;

import com.google.gson.Gson;

import java.lang.reflect.Type;

public class JsonUtils {

    private static JsonUtils instance;
    private Gson gson;

    private JsonUtils() {
        gson = new Gson();
    }

    public static JsonUtils getInstance() {
        if (instance == null) {
            instance = new JsonUtils();
        }
        return instance;
    }

    public static String toJson(Object object) {
        return getInstance().toJson2(object);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        return getInstance().fromJson2(json, classOfT);
    }

    public static <T> T fromJson(String json, Type type) {
        return getInstance().fromJson2(json, type);
    }

    private String toJson2(Object object) {
        return gson.toJson(object);
    }

    private <T> T fromJson2(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }

    private <T> T fromJson2(String json, Type type) {
        return new Gson().fromJson(json, type);
    }

}