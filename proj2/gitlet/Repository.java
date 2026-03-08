package gitlet;

import java.io.File;
import static gitlet.Utils.*;
import java.util.List;


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

    /**
     * add
     * @param filename 添加文件的文件名
     */
    public static void add(String filename) {
        Stage.getStage().add(filename);
    }

    /**
     * commit
     * @param message 提交信息
     * 保存父提交
     * 创建新提交并保存
     * 清空Stage
     * 更改HEAD
     */
    public static void commit(String message) {
        String parentCommitID = Branch.getHeadCommitID();
        Commit commit = new Commit(message, parentCommitID);
        Stage.getStage().clear();
        Branch.getBranches().put(HEAD.getHead().getCurBranch(), commit.getID());
    }

    /**
     * rm
     * @param filename file to be removed
     */
    public static void rm(String filename) {
        Stage.getStage().rm(filename);
    }

    /**
     * log
     */
    public static void log() {
        Commit curCommit = Branch.getHeadCommit();
        while(true) {
            curCommit.printLog();
            if (!curCommit.hasParent()) {
                break;
            }
            curCommit = curCommit.getFirstParentCommit();
        }
    }

    /**
     * global_log
     */
    public static void globalLog() {
        List<String> commitHistory = Utils.plainFilenamesIn(Commit.COMMIT_DIR);
        if (commitHistory != null) {
            for (String commitID : commitHistory) {
                Commit.getCommitByID(commitID).printLog();
            }
        }
    }

    /**
     * find
     * @param message Prints out the ids of all commits
     *                that have the given commit message
     */
    public static void find(String message) {
        List<String> commitHistory = Utils.plainFilenamesIn(Commit.COMMIT_DIR);
        boolean flag = false;
        if (commitHistory != null) {
            for (String commitID : commitHistory) {
                if (Commit.getCommitByID(commitID).getMessage().equals(message)) {
                    flag = true;
                    System.out.println(commitID);
                }
            }
        }
        if (!flag) {
            exitWithError("Found no commit with that message.");
        }
    }
}
