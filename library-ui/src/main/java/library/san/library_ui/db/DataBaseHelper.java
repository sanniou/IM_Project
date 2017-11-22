package library.san.library_ui.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.lib_im.pro.R;
import library.san.library_ui.utils.LogUtils;

import java.sql.SQLException;

/**
 * Created by songgx on 2017/1/11.
 * ormLite数据库创建
 */

public class DataBaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "im.db";
    private static final int DATABASE_VERSION = 1;
    private Context mContext;
    private static DataBaseHelper sDataBaseHelper;

    public static DataBaseHelper getInstance(Context context) {
        if (sDataBaseHelper == null) {
            synchronized (DataBaseHelper.class) {
                sDataBaseHelper = new DataBaseHelper(context);
            }
        }
        return sDataBaseHelper;
    }

    private DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        try {
            String[] tb = mContext.getResources().getStringArray(R.array.db_table);
            for (String aTb : tb) {
                Class clazz;
                clazz = Class.forName(aTb);
                TableUtils.createTable(connectionSource, clazz);
                LogUtils.e("create table============" + clazz.getSimpleName());
            }
        } catch (ClassNotFoundException | SQLException e) {
            LogUtils.e("create Table=============" + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int i,
                          int i1) {
        try {
            String[] tb = mContext.getResources().getStringArray(R.array.db_table);
            for (String aTb : tb) {
                Class clazz;
                clazz = Class.forName(aTb);
                TableUtils.dropTable(connectionSource, clazz, true);
            }
            onCreate(sqLiteDatabase, connectionSource);
        } catch (ClassNotFoundException | SQLException e) {
            LogUtils.e("update Table============" + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        super.close();
        mContext = null;
    }
}
