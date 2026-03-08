package gitlet;


import java.io.File;
import java.io.Serializable;
import java.util.Date; // You'll likely use this in this class
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/** Represents a gitlet commit object.
 *  does at a high level.
 *
 *  @author Joy
 */
public class Commit implements Serializable {
    /**
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private final String message;
    private final String parent;
    private final String secondParent;
    private final Date timestamp;
    //文件名-->hashcode ID(包含文件名和文件内容)
    private final TreeMap<String, String> blobs;
    public static final File COMMIT_DIR = Repository.COMMITS_DIR;

    //初始提交
    public Commit() {
        this.message = "initial commit";
        this.parent = null;
        this.secondParent = null;
        this.blobs = null;
        this.timestamp = new Date(0); //"00:00:00 UTC, Thursday, 1 January 1970"
    }

    //根据Stage内容创建新Commit
    public Commit(String mes, String par) {
        this.message = mes;
        this.parent = par;
        secondParent = null;
        blobs = new TreeMap<>();
        Commit parCommit = getCommitByID(par);
        Stage stage = Stage.getStage();
        if (stage.isEmpty()) {
            Utils.exitWithError("No changes added to the commit.");
        }

        //复制父提交的blobs
        for (Map.Entry<String, String> entry : parCommit.getBlobs().entrySet()) {
            String filename = entry.getKey();
            String blobID = entry.getValue();
            if (!stage.isInRemove(filename)) {
                blobs.put(filename, blobID);
            }
        }

        //推入暂存区的Blob
        for (Map.Entry<String, String> entry : stage.getStagedFiles().entrySet()) {
            String filename = entry.getKey();
            String blobID = entry.getValue();
            //添加或替换
            blobs.put(filename, blobID);
        }
        //当前时间
        timestamp = new Date();
        //保存
        save();
    }

    //持久化
    public void save() {
        File newCommit = Utils.join(COMMIT_DIR, this.getID());
        Utils.writeObject(newCommit, this);
    }

    //获得提交的ID
    //Utils.sha1(Object ...val) 只允许传入String/bytes[]
    //修改blobs和timestamp
    public String getID() {
        StringBuilder blobStr = new StringBuilder();
        for (Map.Entry<String, String> entry : blobs.entrySet()) { //遍历键值对
            blobStr.append(entry.getKey() + entry.getValue());
        }
        String parentStr = (parent == null) ? "" : parent;
        String secondParStr = (secondParent == null) ? "" : secondParent;
        String timeStr = Long.toString(timestamp.getTime());
        return Utils.sha1(message, parentStr, secondParStr, timeStr);
    }

    //获取对应ID的Commit对象
    //不存在则返回null
    public static Commit getCommitByID(String commitID) {
        if (commitID == null) {
            return null;
        }
        File curFile = Utils.join(COMMIT_DIR, commitID);
        if (!curFile.exists()) {
            return null;
        }
        return Utils.readObject(curFile, Commit.class);
    }

    //是否存在该文件
    public boolean hasFile(String filename) {
        return blobs.containsKey(filename);
    }

    //返回文件名对应的ID
    public String getBlobID(String filename) {
        //调用时先调用hanFile，若不存在则直接熔断
        if (!hasFile(filename)) {
            return null;
        }
        return blobs.get(filename);
    }

    //获取blobs
    public Map<String, String> getBlobs() {
        return blobs;
    }

    //判断是否存在父提交，即判断是否为初始提交
    public boolean hasParent() {
        return parent != null || secondParent != null;
    }

    //获取当前提交第一父提交
    public Commit getFirstParentCommit() {
        return getCommitByID(parent);
    }

    //输出提交的log
    public void printLog() {
        String commitID = getID();
        System.out.println("===");
        System.out.println("commit " + commitID);
        if (hasMerged()) {
            System.out.println("Merge: " +
                    parent.substring(0, 7) + " "
                    + secondParent.substring(0, 7));
        }
        System.out.println("Date: " + String.format(Locale.ENGLISH,
                "%1$ta %1$tb %1$td %1$tT %1$tY %1$tz", timestamp));
        System.out.println(message);
        System.out.println();
    }

    //判断是否为合并节点
    public boolean hasMerged() {
        return secondParent != null;
    }

    //获取提交信息
    public String getMessage() {
        return message;
    }
}
