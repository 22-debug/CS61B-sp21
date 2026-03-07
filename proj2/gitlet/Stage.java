package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.TreeMap;
import java.util.TreeSet;

public class Stage implements Serializable {
    //文件名-->blob ID
    private TreeMap<String, String> added;
    //private TreeSet<String> removed;
    public static final File STAGE_DIR = Utils.join(Repository.GITLET_DIR, "stage");

    public Stage() {
        added = new TreeMap<>();
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
}
