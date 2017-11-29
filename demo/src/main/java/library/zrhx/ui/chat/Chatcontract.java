package library.zrhx.ui.chat;

public interface Chatcontract {

    interface ChatView {

        void addMessage(String message);

        void sendMessageSuccess(String message);

        void joinRoomFailed(Throwable throwable);

        void sendMessageFiled(Throwable throwable);
    }
}
