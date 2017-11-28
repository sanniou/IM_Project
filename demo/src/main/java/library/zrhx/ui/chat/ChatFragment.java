package library.zrhx.ui.chat;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zrhx.base.base.BaseFragment;

import library.zrhx.imsample.R;

public class ChatFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup,
                             @Nullable Bundle bundle) {
        return View.inflate(getContext(), R.layout.fragment_chat, viewGroup);
    }
}
