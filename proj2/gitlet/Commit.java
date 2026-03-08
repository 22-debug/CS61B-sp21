package gitlet;


import java.io.File;
import java.io.Serializable;
import java.util.Date; // You'll likely use this in this class
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
    //文件名-->hashcode ID
    private final TreeMap<String, String> blobs;
    public static final File COMMIT_DIR = Repository.COMMITS_DIR;

    /*
    public Commit(String message, String parent) {
        this.message = message;
        this.parent = parent;
        this.timestamp = timestamp;
    }
    */
    //初始提交
    public Commit() {
        this.message = "initial commit";
        this.parent = null;
        this.secondParent = null;
        this.blobs = null;
        this.timestamp = new Date(0); //"00:00:00 UTC, Thursday, 1 January 1970"
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
}
