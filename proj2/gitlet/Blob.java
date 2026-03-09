package gitlet;

import java.io.File;
import java.io.Serializable;
import java.net.ContentHandler;

public class Blob implements Serializable {
    public static final File BLOBS_DIR = Repository.BLOBS_DIR;
    private final String filename;
    private final byte[] content;

    public Blob(File file) {
        filename = file.getName();
        content = Utils.readContents(file);
        //保存
        if (!Utils.join(BLOBS_DIR, getID()).exists()) {
            save();
        }
    }

    //get the blob by its ID
    public static Blob getBlobByID(String ID) {
        return Utils.readObject(Utils.join(BLOBS_DIR, ID), Blob.class);
    }

    //保存
    //文件名是ID，内容是对象的序列化
    public void save() {
        Utils.writeObject(Utils.join(BLOBS_DIR, getID()), this);
    }

    //获取文件名
    public String getID() {
        return Utils.sha1(filename, content);
    }

    //get the content of the blob
    public byte[] getContent() {
        return content;
    }
}
