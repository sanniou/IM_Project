package library.san.library_ui.chat;

import library.san.library_ui.entity.ChatMessage;

import java.util.Comparator;
import java.util.Date;

/**
 * Created by songgx on 2017/2/9.
 * 聊天页面消息排列顺序
 */

class ChatMsgCompare implements Comparator<Object> {
    /**
     * 按时间排序
     */
    @Override
    public int compare(Object lhs, Object rhs) {
        ChatMessage map1 = (ChatMessage) lhs;
        ChatMessage map2 = (ChatMessage) rhs;
        Date date1 = new Date(map1.getDate());
        Date date2 = new Date(map2.getDate());
        return date1.compareTo(date2);
    }
}

