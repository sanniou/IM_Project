package library.san.library_ui.message;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.lib_im.pro.R;
import library.san.library_ui.entity.SessionItem;
import com.lib_im.pro.im.listener.IMSessionListener;
import com.lib_im.pro.im.listener.RefreshDataListener;
import com.lib_im.pro.im.manager.message.SessionManager;
import library.san.library_ui.utils.DividerDrawable;

import java.util.List;

import library.san.library_ui.base.BaseActivity;
import library.san.library_ui.widget.recyler.BaseRecyclerAdapter;
import library.san.library_ui.widget.recyler.ItemDecorations;
import library.san.library_ui.widget.recyler.LViewHolder;

/**
 * Created by songgx on 2016/12/12.
 * 消息模块---展示数据view即fragment
 */
@Route(path = SessionActivity.ROUTE_PATH)
public class SessionActivity extends BaseActivity implements
        BaseRecyclerAdapter.OnItemClickListener,
        IMSessionListener<SessionItem>, BaseRecyclerAdapter.OnItemLongClickListener,
        View.OnClickListener ,RefreshDataListener {
    public static final String ROUTE_PATH = "/lib_im/session";
    private RecyclerView recyclerView;
    private BaseRecyclerAdapter<SessionItem> adapter;
    private SessionPresenter mSessionPresenter;
    private SessionViewHandle sessionViewHandle;
    private SessionManager<SessionItem> sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_session);
        initView();
        initData();
    }

    public void showSession(final List<SessionItem> list) {
        DividerDrawable dividerDrawable = new DividerDrawable(
                getResources().getDimension(R.dimen.divider_height));
        recyclerView.addItemDecoration(ItemDecorations.vertical(
                SessionActivity.this)
                                                      .type(0, dividerDrawable)
                                                      .create());
        if (adapter == null) {
            adapter = new BaseRecyclerAdapter<SessionItem>(this, list, R.layout.item_session) {
                @Override
                public void onBindHolder(LViewHolder holder, int position) {
                    sessionViewHandle.setView(list, position, holder);
                }
            };
            recyclerView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
        adapter.setOnItemClickListener(this);
        adapter.setOnItemLongClickListener(this);

    }

    /**
     * @descript 初始化界面控件
     */
    private void initView() {
        recyclerView = (RecyclerView)findViewById(R.id.im_session_listView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        ImageView backImage= (ImageView) findViewById(R.id.im_session_title_back_image);
        backImage.setOnClickListener(this);
    }

    public void initData() {
        sessionViewHandle = new SessionViewHandle(this);
        sessionManager = LiteChat.chatClient.getSessionManager();
        sessionManager.addSessionListener(this);
        mSessionPresenter = new SessionPresenter(this);
        List<SessionItem> list = mSessionPresenter.getSession();
        showSession(list);
    }



    @Override
    public void onItemClick(ViewGroup parent, View v, int position) {
        mSessionPresenter.itemOnClick(position, this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sessionManager.removeSessionListener(this);
    }

    /**
     * @param sessionItem
     * @descript 更新session回调方法
     */
    @Override
    public void onUpdateSession(SessionItem sessionItem) {
        mSessionPresenter.updateSession(sessionItem);
    }

    /**
     * @param sessionItem
     * @descript 删除session回调方法
     */
    @Override
    public void onDeleteSession(SessionItem sessionItem) {
        runOnUiThread(new Thread(){
            @Override
            public void run() {
                super.run();
                adapter.notifyDataSetChanged();
            }
        });

    }

    @Override
    public void onItemLongClick(ViewGroup parent, View v, int position) {
        sessionViewHandle.showFontSizeDialog(v, position, adapter, this, mSessionPresenter);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.im_session_title_back_image) {
            finish();
        }
    }

    @Override
    public void onRefresh() {
        runOnUiThread(new Thread(){
            @Override
            public void run() {
                super.run();
                adapter.notifyDataSetChanged();
            }
        });
    }
}
