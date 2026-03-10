package gitlet;


import java.io.File;
import java.io.Serializable;
import java.util.*;

/** Represents a gitlet commit object.
 *  does at a high level.
 *
 *  @author Joy
 */
public class Commit implements Serializable {
    /**
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private final String message;

    // represents the parent commits as pointers to other commit objects,
    // then writing the head of a branch will write all the commits (and blobs)
    // in the entire subgraph of commits into one file, which is generally not what you want.
    // To avoid this, don’t use Java pointers to refer to commits and blobs in your runtime objects,
    // but instead use SHA-1 hash strings.
    private final String parent;
    private final String secondParent;

    private final Date timestamp;
    //文件名-->blobID(包含文件名和文件内容)
    private final TreeMap<String, String> blobs;
    public static final File COMMIT_DIR = Repository.COMMITS_DIR;

    //初始提交
    public Commit() {
        this.message = "initial commit";
        this.parent = null;
        this.secondParent = null;
        //this.blobs = null;
        //创建实例，否则无法调用getID
        this.blobs = new TreeMap<>();
        this.timestamp = new Date(0); //"00:00:00 UTC, Thursday, 1 January 1970"
    }

    //根据Stage内容创建新Commit
    public Commit(String mes, String par, String secPar) {
        this.message = mes;
        this.parent = par;
        secondParent = secPar;
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

    //get commit ID by its prefix
    public static String convertPrefixToCommitID(String prefix) {
        if (prefix.length() > Repository.SHA1_LEN) {
            Utils.exitWithError("No commit with that id exists.");
        } else if (prefix.length() == Repository.SHA1_LEN) {
            if (!Utils.join(COMMIT_DIR, prefix).exists()) {
                Utils.exitWithError("No commit with that id exists.");
            }
            return prefix;
        }
        List<String> historyCommits = Utils.plainFilenamesIn(COMMIT_DIR);
        String exactID = new String();
        boolean flag = false;;
        if (historyCommits != null) {
            for (String commitID : historyCommits) {
                if (commitID.startsWith(prefix)) {
                    if (flag) {
                        Utils.exitWithError("Ambiguous prefix.");
                    }
                    exactID = commitID;
                    flag = true;
                }
            }
        }
        if (!flag) {
            Utils.exitWithError("No commit with that id exists.");
        }
        return exactID;
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
    //获取当前提交的第二父提交
    public Commit getSecondParentCommit() {
        return getCommitByID(secondParent);
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

    //获取提交的所有祖先
    public Set<String> collectAllAncestors() {
        HashSet<String> ans = new HashSet<>();
        //This class(Deque) is likely to be faster than Stack when used as a stack
        Queue<Commit> q = new ArrayDeque<>();
        q.add(this);

        while (!q.isEmpty()) {
            Commit cur = q.poll();
            if (cur == null) {
                continue;
            }
            String id = cur.getID();
            if (ans.contains(id)) {
                continue;
            }
            ans.add(id);
            if (cur.getFirstParentCommit() != null) {
                q.add(cur.getFirstParentCommit());
            }
            if (cur.getSecondParentCommit() != null) {
                q.add(cur.getSecondParentCommit());
            }
        }
        return ans;
    }

    //寻找两个分支的分裂点
    public static Commit findSplitPoint(Commit c1, Commit c2) {
        if (c1 == null || c2 == null) {
            return null;
        }
        Set<String> anc1 = c1.collectAllAncestors();
        //从c2开始BFS
        Queue<Commit> q = new ArrayDeque<>();
        Set<String> vis = new HashSet<>();
        q.add(c2);
        while (!q.isEmpty()) {
            Commit cur = q.poll();
            if (cur == null) {
                continue;
            }
            String id = cur.getID();
            if (anc1.contains(id)) {
                return cur;
            }
            if (vis.contains(id)) {
                continue;
            }
            vis.add(id);
            if (cur.getFirstParentCommit() != null) {
                q.add(cur.getFirstParentCommit());
            }
            if (cur.getSecondParentCommit() != null) {
                q.add(cur.getSecondParentCommit());
            }
        }
        return null;
    }

    //合并提交，返回是否产生冲突
    public static boolean merge(Commit head, Commit given, Commit split) {
        boolean hasConflict = false;
        //收集所有文件名
        Set<String> filenames = new HashSet<>();
        filenames.addAll(head.blobs.keySet());
        filenames.addAll(given.blobs.keySet());
        filenames.addAll(split.blobs.keySet());
        for (String name : filenames) {
            String H = head.getBlobID(name);
            String G = given.getBlobID(name);
            String S = split.getBlobID(name);
            if (Objects.equals(H, G)) {
                //1. 三者相同
                //2. 做了相同修改（包括删除）
                continue;
            } else if (Objects.equals(H, S)) {
                //H != G
                //H未改变，G改变
                if (G != null) {
                    checkoutBlobToCWD(name, G);
                } else {
                    Stage.getStage().rm(name);
                }
            } else if (Objects.equals(G, S)) {
                //G不变，H变
                continue;
            } else {
                //冲突
                hasConflict = true;
                writeConflictFile(name, H, G);
            }
        }
        return hasConflict;
    }

    //生成冲突文件
    private static void writeConflictFile(String filename, String headBlobID, String givenBlobID) {
        File file = Utils.join(Repository.CWD, filename);
        String headContent = "";
        String givenContent = "";
        if (headBlobID != null) {
            headContent = new String(Blob.getBlobByID(headBlobID).getContent());
        }
        if (givenBlobID != null) {
            givenContent = new String(Blob.getBlobByID(givenBlobID).getContent());
        }
        String conflict =
                "<<<<<<< HEAD\n"
                + headContent
                + "=======\n"
                + givenContent
                + ">>>>>>>\n";
        Utils.writeContents(file, conflict);
        Stage.getStage().add(filename);
    }

    //更新文件并加入暂存区
    private static void checkoutBlobToCWD(String filename, String blobID) {
        File file = Utils.join(Repository.CWD, filename);
        byte[] content = Blob.getBlobByID(blobID).getContent();
        Utils.writeContents(file, content);
        Stage.getStage().add(filename);
    }
}
