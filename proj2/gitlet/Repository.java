package gitlet;

import java.io.File;
import static gitlet.Utils.*;


/** Represents a gitlet repository.
 *  does at a high level.
 *
 *  @author Joy
 */
public class Repository {
    /**
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File BLOBS_DIR = join(GITLET_DIR, "blobs");
    public static final File COMMITS_DIR = join(GITLET_DIR, "commits");



    /**
     * init
     * 创建文件结构：
     * .gitlet/
     *    |---blobs/
     *    |---commits/
     *    |---branchs  在Branch类中实现
     *    |---stage    在Stage类中实现
     *    |---HEAD      在HEAD中实现
     *
     * 初始commit
     * HEAD & branch初始化
     */
    public static void init() {
        if (GITLET_DIR.exists()) {
            Utils.exitWithError("A Gitlet version-control system already exists in the current directory.");
        }
        GITLET_DIR.mkdir();

        if (!BLOBS_DIR.exists()) {
            BLOBS_DIR.mkdir();
        }
        if (!COMMITS_DIR.exists()) {
            COMMITS_DIR.mkdir();
        }

        HEAD head = new HEAD();
        Branch branch = new Branch();
        Stage stage = new Stage();
        Commit initcommit = new Commit();
        branch.put(head.getCurBranch(), initcommit.getID());
        initcommit.save();
    }

    public static void add(String filename) {
        Stage.getStage().add(filename);
    }
}
