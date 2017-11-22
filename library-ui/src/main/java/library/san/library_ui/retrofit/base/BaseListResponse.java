package library.san.library_ui.retrofit.base;

import java.util.List;

/**
 * Created by songgx on 2017/9/26.
 * 定义接口返回数据为list的基类
 */

public class BaseListResponse<T> extends BaseResponse {

    public List<T> result;

    public List<T> getResult() {
        return result;
    }

    public void setResult(List<T> result) {
        this.result = result;
    }
}
