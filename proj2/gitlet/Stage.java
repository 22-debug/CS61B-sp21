package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.TreeMap;
import java.util.TreeSet;

public class Stage implements Serializable {
    /**
     * 暂存区只有一个文件，每次更改后都需要保存
     */
    //文件名-->blob ID
    private TreeMap<String, String> stagedFiles;
    private TreeSet<String> removedFiles;
    public static final File STAGE_DIR = Utils.join(Repository.GITLET_DIR, "stage");

    public Stage() {
        stagedFiles = new TreeMap<>();
        removedFiles = new TreeSet<>();
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

    //从文件中获取Stage类
    public static Stage getStage() {
        return Utils.readObject(STAGE_DIR, Stage.class);
    }

    //实现add命令
    public void add(String filename) {
        //查看是否存在这个文件
        File curFile = Utils.join(Repository.CWD, filename);
        if (!curFile.exists() || !curFile.isFile()) {
            Utils.exitWithError("File does not exist.");
        }

        Commit headCommit = Branch.getHeadCommit();
        Blob curBlob = new Blob(curFile);
        String curBlobID = curBlob.getID();

        //文件在headCommit中存在且内容一致，则不暂存
        //提交后更改文件，add后又改回去
        if (headCommit.hasFile(filename)
                && headCommit.getBlobID(filename).equals(curBlobID)) {
            stagedFiles.remove(filename);
            //即防止重复提交
            //文件不存在则返回false
        } else {
            stagedFiles.put(filename, curBlobID);
        }
        //add说明不会被rm
        removedFiles.remove(filename);
        save();
    }

    //判断暂存区是否为空
    public boolean isEmpty() {
        return stagedFiles.isEmpty() && removedFiles.isEmpty();
    }

    //清空暂存区
    public void clear() {
        stagedFiles.clear();
        removedFiles.clear();
        //需要保存
        save();
    }

    //判断是否在待删除区
    public boolean isInRemove(String filename) {
        return removedFiles.contains(filename);
    }

    //获取暂存区文件
    public TreeMap<String, String> getStagedFiles() {
        return stagedFiles;
    }

    //实现rm命令
    public void rm(String filename) {
        File curFile = Utils.join(Repository.CWD, filename);
        Commit headCommit = Branch.getHeadCommit();
        if (stagedFiles.containsKey(filename)) {
            //处于暂存状态则取消该状态
            stagedFiles.remove(filename);
        } else if (headCommit.hasFile(filename)) {
            //如果该文件被当前提交追踪，则加入removeFile中，并删除该文件
            removedFiles.add(filename);
            if (curFile.exists()) {
                curFile.delete();
            }
        } else {
            Utils.exitWithError("No reason to remove the file.");
        }
        save();
    }

    //打印Stage
    public void printStage() {
        //stagedFiles
        printStagedFiles();
        //removedFiles
        printRemovedFiles();
    }
    //打印stagedFiles
    private void printStagedFiles() {
        System.out.println("=== Staged Files ===");
        for (String filename : stagedFiles.keySet()) {
            System.out.println(filename);
        }
        System.out.println();
    }
    //打印removedFiles
    private void printRemovedFiles() {
        System.out.println("=== Removed Files ===");
        for (String filename : removedFiles) {
            System.out.println(filename);
        }
        System.out.println();
    }
}
