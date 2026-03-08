package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public class HEAD implements Serializable {
    private static final File HEAD_PATH = Utils.join(Repository.GITLET_DIR, "HEAD");
    private String curBranch;

    public HEAD() {
        if (!HEAD_PATH.exists()) {
            try {
                HEAD_PATH.createNewFile();
            } catch (IOException e) {
                Utils.exitWithError(e.toString());
            }
        }
        this.curBranch = "master";
        save();
    }

    //持久化
    private void save() {
        Utils.writeObject(HEAD_PATH, this);
    }

    //获取当前分支
    public String getCurBranch() {
        return this.curBranch;
    }

    //从文件中读取HEAD类
    public static HEAD getHead() {
        return Utils.readObject(HEAD_PATH, HEAD.class);
    }
}
