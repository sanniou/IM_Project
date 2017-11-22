package library.san.library_ui.db;

/**
 * 缓存
 *
 * @author songgx
 */
public interface Cache {
    /**
     * 保存数据
     *
     * @param key
     * @param value
     */
    public void saveString(String key, String value);

    /**
     * 读取数据
     *
     * @param key
     * @return
     */
    String readString(String key);

    /**
     * 保存数据
     *
     * @param key
     * @param value
     */
    public void saveObject(String key, Object value);

    /**
     * 读取数据
     *
     * @param key
     * @return
     */
    public <T> T readObject(String key, Class cls);

    /**
     * 检查是否有数据
     *
     * @param key
     * @return
     */
    public boolean has(String key);

    /**
     * 删除一条数据
     *
     * @param key
     */
    public void deleteValue(String key);

    /**
     * 清除所有数据
     */
    public void deleteAllValue();
}
