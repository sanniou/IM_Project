package library.san.library_ui.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.io.Serializable;
import java.util.List;

@DatabaseTable(tableName = "GroupContact")
public class GroupContact extends DataEntity implements Serializable{

    /**
     * groupName :
     * groupID : aj201604
     * groupManagerID : wls
     * groupUserCounts : 50
     * groupHead : icon/io487l0.png
     */
    @DatabaseField(columnName = "groupName")
    private String groupName;
    @DatabaseField
    private String groupID;

    private List<String> groupManagers;
    @DatabaseField
    private int groupUserCounts;
    @DatabaseField
    private String groupHead;
    @DatabaseField
    private String groupJid;
    @DatabaseField
    private String groupType;

    public String getGroupJid() {
        return groupJid;
    }

    public void setGroupJid(String groupJid) {
        this.groupJid = groupJid;
    }

    public GroupContact(String name) {
        groupName = name;
    }

    public GroupContact() {
    }

    public int getGroupUserCounts() {
        return groupUserCounts;
    }

    public void setGroupUserCounts(int groupUserCounts) {
        this.groupUserCounts = groupUserCounts;
    }

    public String getGroupHead() {
        return groupHead;
    }

    public void setGroupHead(String groupHead) {
        this.groupHead = groupHead;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupID() {
        return this.groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public List<String> getGroupManagers() {
        return groupManagers;
    }

    public void setGroupManagers(List<String> groupManagers) {
        this.groupManagers = groupManagers;
    }

}
