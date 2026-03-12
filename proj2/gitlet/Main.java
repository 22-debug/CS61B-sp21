package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Joy
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ...
     *
     *  If a user inputs a command with the wrong number or format of operands,
     *  print the message "Incorrect operands." and exit.
     *
     *  If a user inputs a command that requires being in an initialized Gitlet
     *  working directory (i.e., one containing a .gitlet subdirectory), but is not
     *  in such a directory, print the message "Not in an initialized Gitlet directory."
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            Utils.exitWithError("Please enter a command.");
        }

        String firstArg = args[0];
        switch (firstArg) {
            case "init":
                validateNumArgs(args, 0);
                Repository.init();
                break;
            case "add":
                isInitialized();
                validateNumArgs(args, 1);
                Repository.add(args[1]);
                break;
            case "commit":
                isInitialized();
                if (args.length != 2 || args[1].trim().isEmpty()) {
                    //.trim()移除空白字符
                    Utils.exitWithError("Please enter a commit message.");
                }
                Repository.commit((args[1]));
                break;
            case "rm":
                isInitialized();
                validateNumArgs(args, 1);
                Repository.rm(args[1]);
                break;
            case "log":
                isInitialized();
                validateNumArgs(args, 0);
                Repository.log();
                break;
            case "global-log":
                isInitialized();
                validateNumArgs(args, 0);
                Repository.globalLog();
                break;
            case "find":
                isInitialized();
                validateNumArgs(args, 1);
                Repository.find(args[1]);
                break;
            case "status":
                isInitialized();
                validateNumArgs(args, 0);
                Repository.status();
                break;
            case "checkout":
                isInitialized();
                Repository.checkout(args);
                break;
            case "branch":
                isInitialized();
                validateNumArgs(args, 1);
                Repository.branch(args[1]);
                break;
            case "rm-branch":
                isInitialized();
                validateNumArgs(args, 1);
                Repository.rmBranch(args[1]);
                break;
            case "reset":
                isInitialized();
                validateNumArgs(args, 1);
                Repository.reset(args[1]);
                break;
            case "merge":
                isInitialized();
                validateNumArgs(args, 1);
                Repository.merge(args[1]);
                break;
            case "add-remote":
                isInitialized();
                validateNumArgs(args, 2);
                Repository.addRemote(args[1], args[2]);
                break;
            case "rm-remote":
                isInitialized();
                validateNumArgs(args, 1);
                Repository.rmRemote(args[1]);
                break;
            case "push":
                isInitialized();
                validateNumArgs(args, 2);
                Repository.push(args[1], args[2]);
                break;
            case "fetch":
                isInitialized();
                validateNumArgs(args, 2);
                Repository.fetch(args[1], args[2]);
                break;
            case "pull":
                isInitialized();
                validateNumArgs(args, 2);
                Repository.pull(args[1], args[2]);
                break;
            default:
                Utils.exitWithError("No command with that name exists.");
                break;
        }
    }

    /**
     * 验证参数数量
     * @param args 所有参数
     * @param n 期望参数数量（不包含命令本身）
     */
    private static void validateNumArgs(String[] args, int n) {
        if (args.length != n + 1) {
            Utils.exitWithError("Incorrect operands.");
        }
    }

    //判断是否初始化
    private static void isInitialized() {
        if (!Repository.GITLET_DIR.exists()) {
            Utils.exitWithError("Not in an initialized Gitlet directory.");
        }
    }
}
