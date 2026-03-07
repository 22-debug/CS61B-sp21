package gitlet;


import java.io.File;
import java.io.Serializable;
import java.util.Date; // You'll likely use this in this class
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
    //文件名-->hashcode
    private final TreeMap<String, String> blobs;
    public static final File COMMIT_DIR = Repository.COMMITS_DIR;

    /*
    public Commit(String message, String parent) {
        this.message = message;
        this.parent = parent;
        this.timestamp = timestamp;
    }
    */

    public Commit() {
        this.message = "initial commit";
        this.parent = null;
        this.secondParent = null;
        this.blobs = null;
        this.timestamp = new Date(0); //"00:00:00 UTC, Thursday, 1 January 1970"
    }

    public void save() {
        File newCommit = Utils.join(COMMIT_DIR, this.getID());
        Utils.writeObject(newCommit, this);
    }

    //TODO
    public String getID() {
        return null;
    }

    public String getMessage() {
        return this.message;
    }

    public Date getTimestamp() {
        return this.timestamp;
    }

    public String getParent() {
        return this.parent;
    }
}
