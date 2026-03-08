package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.TreeMap;
import java.util.TreeSet;

public class Stage implements Serializable {
    //文件名-->blob ID
    private TreeMap<String, String> stagedFiles;
    private TreeSet<String> removedFiles;
    public static final File STAGE_DIR = Utils.join(Repository.GITLET_DIR, "stage");

    public Stage() {
        stagedFiles = new TreeMap<>();
        if (!STAGE_DIR.exists()) {
            try {
                STAGE_DIR.createNewFile();
            } catch (IOException e) {
                Utils.exitWithError(e.toString());
            }
        }
        save();
    }
    private void save() {
        Utils.writeObject(STAGE_DIR, this);
    }

    //从文件中获取Stage类
    public static Stage getStage() {
        return Utils.readObject(STAGE_DIR, Stage.class);
    }

    //实现add命令
    public void add(String filename) {
        //查看是否存在这个文件
        File curFile = Utils.join(Repository.CWD, filename);
        if (!curFile.exists() || !curFile.isFile()) {
            Utils.exitWithError("File does not exist.");
        }

        Commit headCommit = Branch.getHeadCommit();
        Blob curBlob = new Blob(curFile);
        String curBlobID = curBlob.getID();

        //文件在headCommit中存在且内容一致，则不暂存
        //提交后更改文件，add后又改回去
        if (headCommit.hasFile(filename) &&
                headCommit.getBlobID(filename).equals(curBlobID)) {
            stagedFiles.remove(filename);
            //即防止重复提交
            //文件不存在则返回false
        } else {
            stagedFiles.put(filename, curBlobID);
        }
        //add说明不会被rm
        removedFiles.remove(filename);
        save();
    }
}
