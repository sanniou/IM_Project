package library.san.library_ui.entity;

import java.io.File;

/**
 * Created by songgx on 2016/7/28.
 * 聊天图片上传实体封装
 */
public class FileUpEntity {

    private File file;
    private String storeFileName;
    private String localFileName;
    private String fileUrl;
    public static final int FILE_TYPE_IMAGE = 2;
    public static final int FILE_TYPE_VIDEO = 1;


    public int getFileType() {
        return fileType;
    }

    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    private int fileType;

    public File getFile() {
        if (file == null) {
            file = new File(localFileName);
        }
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getStoreFileName() {
        return storeFileName;
    }

    public void setStoreFileName(String storeFileName) {
        this.storeFileName = storeFileName;
    }

    public String getLocalFileName() {
        return localFileName;
    }

    public void setLocalFileName(String localFileName) {
        this.localFileName = localFileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
}
