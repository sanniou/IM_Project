package library.san.library_ui.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * key-value 数据缓存表
 */
@DatabaseTable(tableName = "TableKeyValue")
class TableKeyValue {
    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField(columnName = "_KEY")
    private String key;
    @DatabaseField(columnName = "_VALUE")
    private String value;
    @DatabaseField(columnName = "_USER")
    private String user = "1";

    public TableKeyValue() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
