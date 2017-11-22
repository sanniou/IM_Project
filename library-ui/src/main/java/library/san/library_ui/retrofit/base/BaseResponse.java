package library.san.library_ui.retrofit.base;

/**
 * Created by songgx on 2017/9/26.
 * 网络请求基类
 */

public abstract class BaseResponse {

    public int code;

    public String message;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
