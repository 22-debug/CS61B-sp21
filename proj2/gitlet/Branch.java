package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.TreeMap;

public class Branch implements Serializable {
    public static final File BRANCH_DIR = Utils.join(Repository.GITLET_DIR, "branchs");
    //分支名-->分支 **最新** Commit的ID 相当于实现了HEAD功能，而HEAD只需要记录当前分支
    private TreeMap<String, String> branches;

    public Branch() {
        branches = new TreeMap<>();
        if (!BRANCH_DIR.exists()) {
            try {
                BRANCH_DIR.createNewFile();
            } catch (IOException e) {
                Utils.exitWithError(e.toString());
            }
        }
        save();
    }
    private void save() {
        Utils.writeObject(BRANCH_DIR, this);
    }

    //branch --> commit
    public void put(String key, String value) {
        branches.put(key, value);
        save();
    }

    //获得最新的提交
    public static Commit getHeadCommit() {
        return Commit.getCommitByID(getHeadCommitID());
    }

    //获得最新提交的ID
    public static String getHeadCommitID() {
        return getBranches().branches.get(HEAD.getHead().getCurBranch());
    }

    //从文件读取Branch类
    public static Branch getBranches() {
        return Utils.readObject(BRANCH_DIR, Branch.class);
    }
}
