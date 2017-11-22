package library.san.library_ui.notify;

import android.content.Intent;
import android.os.Bundle;

import com.alibaba.android.arouter.facade.annotation.Route;

import library.san.library_ui.base.BaseActivity;

/**
 * Created by songgx on 2017/8/29.
 */
@Route(path = NotifyActivity.ROUTE_PATH)
public class NotifyActivity extends BaseActivity {
    public static final String ROUTE_PATH ="/main/notify";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();


    }

    private void init() {
        Intent intent = getIntent();
        if (intent != null) {
         //TODO 实现业务逻辑的推送

        }
    }


}
