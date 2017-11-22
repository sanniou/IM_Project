package library.san.library_ui.api;

/**
 * Created by songgx on 2017/9/26.
 * 网络请求接口服务
 */

public abstract class IMRequestManager {

    /**
     * 获取对象的单例模式
     * @return
     */
     public IMObjectRequest getObjectInstance(){
         return null;
     }

     public IMListRequest getListInstance(){
         return null;
     }
}
