package library.zrhx.ui.chat;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.zrhx.base.base.BaseFragment;
import com.zrhx.base.utils.ToastUtils;
import com.zrhx.base.widget.RoundButton;
import com.zrhx.base.widget.recyclerview.BaseRecyclerAdapter;
import com.zrhx.base.widget.recyclerview.LViewHolder;

import java.util.List;

import library.zrhx.imsample.R;

@Route(path = "/chat/chatmain")
public class ChatFragment extends BaseFragment implements Chatcontract.ChatView {

    public static final String KEY_FOR_ID = "KEY_FOR_ID";
    private LinearLayout mChatLayout;
    private ImageView mChatActionVoice;
    private RoundButton mChatActionSend;
    private ImageView mChatFunction;
    private EditText mChatInput;
    private RecyclerView mChatList;
    private String mJid;
    private BaseRecyclerAdapter<String> mAdapter;
    private List<String> mData;
    private ChatPresenter mPresenter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup,
                             @Nullable Bundle bundle) {
        View view = layoutInflater.inflate(R.layout.fragment_chat, viewGroup, false);
        initView(view);
        initData();
        return view;
    }

    private void initData() {
        Bundle bundle = getArguments();
        if (bundle == null) {
            getActivity().finish();
            return;
        }
        mJid = bundle.getString(KEY_FOR_ID);
        mPresenter = new ChatPresenter(mJid, this, this);
    }

    private void initView(View view) {
        mChatLayout = view.findViewById(R.id.chat_layout);
        mChatActionVoice = view.findViewById(R.id.chat_action_voice);
        mChatActionSend = view.findViewById(R.id.chat_action_send);
        mChatFunction = view.findViewById(R.id.chat_function);
        mChatInput = view.findViewById(R.id.chat_input);
        mChatList = view.findViewById(R.id.chat_list);
        mAdapter = new BaseRecyclerAdapter<String>(R.layout.item_chat) {
            @Override
            public void onBindHolder(LViewHolder lViewHolder, String s) {
                lViewHolder.setText(R.id.chet_message, s);
            }
        };
        mData = mAdapter.getData();
        mChatList.setAdapter(mAdapter);
        mChatActionSend.setOnClickListener(v -> {
            String trim = mChatInput.getText().toString().trim();
            mPresenter.sendMessage(trim);
        });
    }

    /************************* View *************************/
    @Override
    public void addMessage(String message) {
        mData.add(message);
        mAdapter.notifyItemInserted(mData.size() - 1);
        mChatList.smoothScrollToPosition(mAdapter.getItemCount() - 1);
    }

    @Override
    public void sendMessageSuccess(String message) {

    }

    @Override
    public void joinRoomFailed(Throwable throwable) {
        ToastUtils.showShort("初始化群组失败");
    }

    @Override
    public void sendMessageFiled(Throwable throwable) {

    }
}
