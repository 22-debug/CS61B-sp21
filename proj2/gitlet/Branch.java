package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.TreeMap;

public class Branch implements Serializable {
    public static final File BRANCH_DIR = Utils.join(Repository.GITLET_DIR, "branchs");
    //分支名-->分支最新Commit的ID
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

    public void put(String key, String value) {
        branches.put(key, value);
        save();
    }
}
