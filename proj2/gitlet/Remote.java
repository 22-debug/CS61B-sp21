package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.TreeMap;

import static gitlet.Utils.join;

public class Remote implements Serializable {
    //本地另一个位置的.gitlet仓库
    static File DIR = join(Repository.GITLET_DIR, "remote");
    //remote name -> its path
    private TreeMap<String, String> remote;
    public Remote() {
        remote = new TreeMap<>();
        if (!DIR.exists()) {
            try {
                DIR.createNewFile();
            } catch (IOException e) {
                Utils.exitWithError(e.toString());
            }
        }
        save();
    }
    private void save() {
        Utils.writeObject(DIR, this);
    }
    //load
    public static Remote getRemote() {
        return Utils.readObject(DIR, Remote.class);
    }

    //if contain the remote name
    public boolean contains(String name) {
        return remote.containsKey(name);
    }

    //add the remote
    public void put(String name, String path) {
        remote.put(name, path);
        save();
    }

    //remove the remote
    public void remove(String name) {
        remote.remove(name);
        save();
    }

    //get the path of the remote
    public String get(String name) {
        return remote.get(name);
    }
}

