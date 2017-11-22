package library.san.library_ui.db;
import android.content.Context;

import com.google.gson.Gson;
import library.san.library_ui.utils.LogUtils;

import java.util.List;

/**
 * 数据库KEY-VALUES
 *
 * @author songgx
 */
public class TableCache implements Cache {

    private String TAG = "TableCache";
    private BaseDao<TableKeyValue> mBaseDao;

    /**
     * 初始化key,value缓存数据表
     */
    public TableCache(Context context) {
        mBaseDao = new BaseDao<>(DataBaseHelper.getInstance(context), TableKeyValue.class);
    }
    @Override
    public void saveString(String k, String v) {
        LogUtils.i(TAG, "saveString()----" + v);
        TableKeyValue _kv = new TableKeyValue();
        _kv.setKey(k);
        _kv.setValue(v);
        List<TableKeyValue> _list = mBaseDao.queryByColumn("_KEY", k);
        if (_list != null) {
            if (!_list.isEmpty()) {
                for (TableKeyValue _k : _list) {
                    if (_k != null) {
                        _k.setValue(v);
                        mBaseDao.update(_k);
                    } else {
                        mBaseDao.add(_kv);
                    }
                }
            } else {
                mBaseDao.add(_kv);
            }
        }
    }
    @Override
    public String readString(String k) {
        LogUtils.i(TAG, "readString()----");
        String value = null;
        List<TableKeyValue> _list = mBaseDao.queryByColumn("_KEY", k);
        if (_list != null) {
            for (TableKeyValue _k : _list) {
                if (_k != null) {
                    value = _k.getValue();
                }
            }
        }
        return value;
    }
    @Override
    public void saveObject(String key, Object value) {
        if (value != null) {
            saveString(key, new Gson().toJson(value));
        }
    }
    @Override
    public <T> T readObject(String key, Class cls) {
        String value = readString(key);
        return (T) new Gson().fromJson(value, cls);
    }
    @Override
    public void deleteValue(String key) {
        LogUtils.i(TAG, "deleteValue()----");
        try {
            mBaseDao.deleteByColumn("_KEY", key);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void deleteAllValue() {
        LogUtils.i(TAG, "deleteAllValue()----");
        try {
            mBaseDao.clearAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public boolean has(String k) {
        boolean b = false;
        List<TableKeyValue> _list = mBaseDao.queryByColumn("_KEY", k);
        if (_list != null) {
            for (TableKeyValue _k : _list) {
                if (_k != null) {
                    b = Boolean.TRUE;
                }
            }
        }
        return b;
    }
}
