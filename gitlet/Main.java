package gitlet;

import java.io.File;
import java.io.IOException;

import static gitlet.Utils.*;


/**
 * Driver class for Gitlet, the tiny stupid version-control system.
 *
 * @author Zihao
 */
public class Main {
    /**
     * File;.
     */
    static final File DIRECTORY = new File(System.getProperty("user.dir"));
    /**
     * USAGE;.
     */
    static final String USAGE = "gitlet/Usage.txt";

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND> ....
     */
    public static void main(String... args) throws IOException {
        SomeObj obj = new SomeObj(args);

        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }

        if (args[0].equals("init")) {
            obj.init();
            System.exit(0);
        }

        if (!join(DIRECTORY, ".gitlet").exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }

        if (args[0].equals("add")) {
            obj.add(args[1]);
            System.exit(0);
        }

        if (args[0].equals("commit")) {
            obj.commit(args[1]);
            System.exit(0);
        }
        if (args[0].equals("rm")) {
            obj.rm(args[1]);
            System.exit(0);
        }
        if (args[0].equals("log")) {
            obj.log();
            System.exit(0);
        }

        if (args[0].equals("global-log")) {
            obj.globalLog();
            System.exit(0);
        }

        if (args[0].equals("find")) {
            obj.find(args[1]);
            System.exit(0);
        }

        if (args[0].equals("status")) {
            obj.status();
            System.exit(0);
        }

        mainTwo(obj, args);
    }
    /**
     * update and .paintComponent are synchronized because they are called
     *      *  by three different threa.
     * @param  obj  to obj
     *              @param  args  to args
     */
    private static void mainTwo(SomeObj obj, String... args)
            throws IOException {
        if (args[0].equals("checkout")) {
            if (args.length == 4) {
                if (!args[2].equals("--")) {
                    System.out.println("Incorrect operands.");
                    return;
                }
                obj.checkout(args[1], args[3]);
            } else if (args.length == 3) {
                if (!args[1].equals("--")) {
                    System.out.println("Incorrect operands.");
                    return;
                }
                obj.checkout("", args[2]);
            } else {
                obj.checkout(args[1]);
            }
            System.exit(0);
        }

        if (args[0].equals("branch")) {
            if (args.length == 2) {
                obj.branch(args[1]);
            }
            System.exit(0);
        }


        if (args[0].equals("rm-branch")) {
            if (args.length == 2) {
                obj.rmBranch(args[1]);
            }
            System.exit(0);
        }

        if (args[0].equals("reset")) {
            if (args.length == 2) {
                obj.reset(args[1]);
            }
            System.exit(0);
        }

        if (args[0].equals("merge")) {
            if (args.length == 2) {
                obj.merge(args[1]);
            }
            System.exit(0);
        }

        System.out.println(("No command with that name exists."));
        System.exit(0);
    }
}
