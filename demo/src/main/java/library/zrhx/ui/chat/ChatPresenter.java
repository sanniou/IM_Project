package library.zrhx.ui.chat;

import android.arch.lifecycle.GenericLifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;

import com.lib_im.profession.IMChatClient;
import com.lib_im.profession.message.IMGroupConversation;

import io.reactivex.subjects.PublishSubject;

public class ChatPresenter {

    private LifecycleOwner mLifecycleOwner;
    private Chatcontract.ChatView mChatView;
    private IMGroupConversation mGroupConversation;
    private PublishSubject<String> a = PublishSubject.create();

    public ChatPresenter(String jid, Chatcontract.ChatView chatView,
                         LifecycleOwner lifecycleOwner) {
        mChatView = chatView;
        mLifecycleOwner = lifecycleOwner;
        mLifecycleOwner.getLifecycle()
                       .addObserver((GenericLifecycleObserver) (source, event) -> {
                           switch (event) {
                               case ON_CREATE:
                                   break;
                               case ON_START:
                                   break;
                               case ON_RESUME:
                                   break;
                               case ON_PAUSE:
                                   break;
                               case ON_STOP:
                                   break;
                               case ON_DESTROY:
                                   a.onNext("");
                                   break;
                               case ON_ANY:
                                   break;
                               default:
                           }
                       });

        IMChatClient.getInstance()
                    .getChatManager()
                    .observeMessage(jid)
                    .takeUntil(a)
                    .subscribe(mChatView::addMessage, Throwable::printStackTrace);

        IMChatClient.getInstance().getChatManager()
                    .getGroupConversation(jid, "aznsss")
                    .subscribe(imGroupConversation -> mGroupConversation = imGroupConversation,
                            throwable -> {
                                throwable.printStackTrace();
                                mChatView.joinRoomFailed(throwable);
                            });
    }

    public void sendMessage(String message) {
        mGroupConversation.send(message)
                          .takeUntil(a)
                          .subscribe(s -> mChatView.sendMessageSuccess(message),
                                  throwable -> mChatView.sendMessageFiled(throwable));
    }
}
