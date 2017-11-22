package library.san.library_ui.db;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by songgx on 2017/1/11.
 * 数据库操作类
 */

public class BaseDao<T> {

    public static final int CODE_FAILED = -1;

    /**
     * 声明dao
     */
    private Dao<T, Integer> daoOpe;

    /**
     * 获取对应的dao对象
     */
    public BaseDao(OrmLiteSqliteOpenHelper sqliteOpenHelper, Class<T> cls) {
        try {
            daoOpe = sqliteOpenHelper.getDao(cls);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存数据按对象存储
     */
    public int add(T t) {
        try {
            return daoOpe.create(t);
        } catch (SQLException e) {
            e.printStackTrace();
            return CODE_FAILED;
        }
    }

    public int add(List<T> t) {
        try {
            return daoOpe.create(t);
        } catch (SQLException e) {
            e.printStackTrace();
            return CODE_FAILED;
        }
    }

    /**
     * 删除数据
     */
    public int delete(T t) {
        try {
            return daoOpe.delete(t);
        } catch (SQLException e) {
            e.printStackTrace();
            return CODE_FAILED;
        }
    }

    public int delete(List<T> t) {
        try {
            return daoOpe.delete(t);
        } catch (SQLException e) {
            e.printStackTrace();
            return CODE_FAILED;
        }
    }

    /**
     * 更新数据
     */
    public void update(T t) {
        try {
            daoOpe.update(t);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询所有
     */
    public List<T> queryAll() {
        try {
            return daoOpe.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 按列名查询
     */
    public List<T> queryByColumn(String columnName, Object columnValue) {
        try {
            QueryBuilder<T, Integer> builder = daoOpe.queryBuilder();
            builder.where().eq(columnName, columnValue);
            return builder.query();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 多条件按列名查询
     */
    public List<T> queryByColumn(String columnName1, Object columnValue1, String columnName2,
                                 Object columnValue2) {
        try {
            QueryBuilder<T, Integer> builder = daoOpe.queryBuilder();
            builder.where().eq(columnName1, columnValue1).and().eq(columnName2, columnValue2);
            return builder.query();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 按列名多条件删除
     */
    public int deleteByColumn(String columnName1, Object columnValue1, String columnName2,
                              Object columnValue2) {
        try {
            DeleteBuilder<T, Integer> builder = daoOpe.deleteBuilder();
            builder.where().eq(columnName1, columnValue1).and().eq(columnName2, columnValue2);
            return builder.delete();
        } catch (SQLException e) {
            e.printStackTrace();
            return CODE_FAILED;
        }
    }

    /**
     * 按列名删除
     */
    public int deleteByColumn(String columnName, Object columnValue) {
        try {
            DeleteBuilder<T, Integer> builder = daoOpe.deleteBuilder();
            builder.where().eq(columnName, columnValue);
            return builder.delete();
        } catch (SQLException e) {
            e.printStackTrace();
            return CODE_FAILED;
        }
    }

    /**
     * 删除所有数据
     */
    public int clearAll() {
        try {
            return daoOpe.deleteBuilder().delete();
        } catch (SQLException e) {
            e.printStackTrace();
            return CODE_FAILED;
        }
    }

    public long count() {
        try {
            return daoOpe.countOf();
        } catch (SQLException e) {
            e.printStackTrace();
            return CODE_FAILED;
        }
    }

    /**
     * 更新或存储
     */
    public void createOrUpdate(T t) {
        try {
            daoOpe.createOrUpdate(t);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
