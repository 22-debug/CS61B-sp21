package gitlet;

import java.io.File;
import java.io.Serializable;

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

    //保存
    public void save() {
        Utils.writeObject(Utils.join(BLOBS_DIR, getID()), this);
    }

    //获取文件名
    public String getID() {
        return Utils.sha1(filename, content);
    }
}
