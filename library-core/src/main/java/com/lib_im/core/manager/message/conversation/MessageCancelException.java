package com.lib_im.core.manager.message.conversation;

public class MessageCancelException extends Exception {

    MessageCancelException() {
        super("消息被撤回");
    }
}
