package gitlet;

import edu.princeton.cs.introcs.StdRandom;

import java.io.File;
import static gitlet.Utils.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


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
    public static final int SHA1_LEN = 40;



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

    /**
     *status
     */
    public static void status() {
        //显示当前分支
        Branch.getBranches().printBranches();
        //显示暂存或删除的文件
        Stage.getStage().printStage();
        //选做内容
        //Modifications Not Staged For Commit
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        //Untracked Files
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    /**
     * checkout
     * @param args command
     */
    public static void checkout(String[] args) {
        if (args.length == 2) {
            //checkout [branch name]
            checkoutBranchName(args[1]);
        } else if (args.length == 3) {
            //checkout -- [filename]
            if (!args[1].equals("--")) {
                Utils.exitWithError("Incorrect operands.");
            }
            checkoutLatestFile(args[2]);
        } else if (args.length == 4) {
            //checkout [commit id] -- [filename]
            if (!args[2].equals("--")) {
                Utils.exitWithError("Incorrect operands.");
            }
            checkoutCommitID(args[1], args[3]);
        } else {
            Utils.exitWithError("Incorrect operands.");
        }
    }


    /* checkout help methods begin */
    /**
     * checkout [branch name]
     * @param branch the name of the target branch
     */
    private static void checkoutBranchName(String branch) {
        //分支不存在
        if (!Branch.getBranches().containsBranch(branch)) {
            Utils.exitWithError("No such branch exists.");
        }
        //分支是当前分支
        if (branch.equals(HEAD.getHead().getCurBranch())) {
            Utils.exitWithError("No need to checkout the current branch.");
        }
        //the nearest commit in the target commit
        Commit target = Branch.getBranches().getCommit(branch);
        //the current commit
        Commit head = Branch.getHeadCommit();
        //cover the working directory
        replaceCommit(target, head);
        //change the branch
        HEAD.getHead().changeCurBranch(branch);
        //clear the stage
        Stage.getStage().clear();
    }

    /**
     * checkout -- [filename]
     * @param filename Takes the version of the file as it exists in the head commit
     *                 and puts it in the working directory,
     *                 overwriting the version of the file that’s already there if there is one.
     */
    private static void checkoutLatestFile(String filename) {
        File cueFile = Utils.join(CWD, filename);
        Commit headCommit = Branch.getHeadCommit();
        if (!headCommit.hasFile(filename)) {
            Utils.exitWithError("File does not exist in that commit.");
        }
        byte[] content = Blob.getBlobByID(headCommit.getBlobID(filename)).getContent();
        overwriteFile(cueFile, content);
    }

    /**
     * checkout [commit id] -- [filename]
     * @param commitID the given id
     * @param filename Takes the version of the file as it exists in the commit with the given id,
     *      *                 and puts it in the working directory,
     *      *                 overwriting the version of the file that’s already there if there is one.
     */
    private static void checkoutCommitID(String commitID, String filename) {
        //前缀转换为提交ID
        String targetID = Commit.convertPrefixToCommitID(commitID);
        Commit target = Commit.getCommitByID(targetID);
        if (!target.hasFile(filename)) {
            Utils.exitWithError("File does not exist in that commit.");
        }
        File file = Utils.join(CWD, filename);
        byte[] content = Blob.getBlobByID(target.getBlobID(filename)).getContent();
        overwriteFile(file, content);
    }

    //cover the working directory to the stage of the target commit
    private static void replaceCommit(Commit target, Commit head) {
        checkUntracked(target, head);
        Map<String, String> targetBlobs = target.getBlobs();
        Map<String, String> headBlobs = head.getBlobs();
        //增量更新 不建议直接删除再全部写入
        //删除需要删除的
        for (Map.Entry<String, String> entry : headBlobs.entrySet()) {
            String filename = entry.getKey();
            if (!target.hasFile(filename)) {
                File file = Utils.join(CWD, filename);
                Utils.restrictedDelete(file);
            }
        }
        //覆盖需要覆盖的
        for (Map.Entry<String, String> entry : targetBlobs.entrySet()) {
            String filename = entry.getKey();
            String targetBlobID = entry.getValue();
            //文件不同时写入
            //if (!targetBlobID.equals(headBlobID)) {
            //    byte[] content = Blob.getBlobByID(targetBlobID).getContent();
            //    Utils.writeContents(Utils.join(CWD, filename), content);
            //}
            //未考虑当前不存在目标文件的情况
            byte[] content = Blob.getBlobByID(targetBlobID).getContent();
            overwriteFile(Utils.join(CWD, filename), content);
        }
    }

    //覆盖或创建文件
    private static void overwriteFile(File file, byte[] content) {
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            Utils.writeContents(file, content);
        } catch (IOException e) {
            Utils.exitWithError(e.toString());
        }
    }

    //check if there is a working file is untracked in the current branch
    //but would be overwritten by the checkout.
    //thanks to deepseek
    private static void checkUntracked(Commit target, Commit current) {
        List<String> workingFiles = Utils.plainFilenamesIn(CWD);
        if (workingFiles == null) {
            return;
        }

        // 获取 target 要操作的所有文件
        Set<String> targetFiles = target.getBlobs().keySet();

        for (String filename : workingFiles) {
            boolean inCurrent = current.hasFile(filename);
            boolean inTarget = targetFiles.contains(filename);

            // 如果是 untracked 文件（不在 current 中）
            // 并且会被 target 操作（创建或覆盖）
            if (!inCurrent && inTarget) {
                Utils.exitWithError("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
            }
        }
    }
    /* checkout help methods end */
}
