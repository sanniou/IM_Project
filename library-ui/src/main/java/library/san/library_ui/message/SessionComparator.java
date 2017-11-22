package library.san.library_ui.message;

import library.san.library_ui.entity.SessionItem;

import java.util.Comparator;
import java.util.Date;

/**
 * Created by songgx on 2017/1/22.
 */

public class SessionComparator implements Comparator<Object> {
    @Override
    public int compare(Object lhs, Object rhs) {
        SessionItem map1 = (SessionItem) lhs;
        SessionItem map2 = (SessionItem) rhs;
        Date date1 = new Date(map1.getDate());
        Date date2 = new Date(map2.getDate());
        return date2.compareTo(date1);
    }
}
