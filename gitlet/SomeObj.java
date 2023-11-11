package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Stream;

import static gitlet.Utils.join;
import static gitlet.Utils.readContents;
import static gitlet.Utils.readContentsAsString;
import static gitlet.Utils.readObject;
import static gitlet.Utils.sha1;
import static gitlet.Utils.writeContents;
import static gitlet.Utils.writeObject;

/**
 * SomeObj.
 *
 * @author Zihao
 */
public class SomeObj {
    /**
     * directory;.
     */
    private final File _directory = new File(System.getProperty("user.dir"));
    /**
     * workDirectory;.
     */
    private final File _workDirectory = join(_directory, ".gitlet");
    /**
     * commits;.
     */
    private final File _commits = join(_workDirectory, "commits");
    /**
     * branches;.
     */
    private final File _branches = join(_workDirectory, "heads");
    /**
     * head;.
     */
    private final File _head = join(_workDirectory, "HEAD");
    /**
     * status;.
     */
    private final File _status = join(_workDirectory, "STATUS");

    /**
     * SomeObj;.
     *
     * @param args to args
     */
    public SomeObj(String[] args) {
    }

    /**
     * currentBranch;.
     *
     * @return join
     */
    private File currentBranch() {
        return join(_workDirectory, readContentsAsString(_head));
    }

    /**
     * readStatus;.
     *
     * @return readObject
     */
    public Status readStatus() {
        return readObject(_status, Status.class);
    }

    /**
     * saveStatus;.
     *
     * @param s to s
     */
    public void saveStatus(Status s) {
        writeObject(_status, s);
    }

    /**
     * init;.
     */
    public void init() throws IOException {

        if (_workDirectory.exists()
                || _commits.exists() || _branches.exists()) {
            System.out.println("A Gitlet version-control system "
                    + "already exists in the current directory.");
        } else {
            _workDirectory.mkdir();
            _commits.mkdir();
            _branches.mkdir();
            Commit initial = new Commit("initial commit");
            File file = join(_workDirectory, "tmpcommit");
            file.createNewFile();
            writeObject(file, initial);
            String sha = sha1(readContents(file));
            File commit = join(_commits, sha);
            file.renameTo(commit);
            final File master = join(_branches, "master");
            master.createNewFile();
            writeContents(master, sha);
            _head.createNewFile();
            writeContents(_head, "heads/master");
            _status.createNewFile();
            saveStatus(new Status(new Blob[0], new String[0]));
        }
    }

    /**
     * currentCommit;.
     *
     * @return readObject
     */
    private Commit currentCommit() {
        String headCommit = readContentsAsString(_head);
        return readObject(join(_commits, readContentsAsString(
                join(_workDirectory, headCommit))), Commit.class);
    }

    /**
     * currentCommitSHA;.
     *
     * @return headCommit
     */
    private String currentCommitSHA() {
        String headCommit = readContentsAsString(_head);
        File file = join(_workDirectory, headCommit);
        if (file.exists()) {
            return readContentsAsString(file);
        } else {
            return headCommit;
        }
    }

    /**
     * currentCommitSHA;.
     *
     * @param message to message
     */
    public void commit(String message) throws IOException {
        File currentBranch = join(_workDirectory, readContentsAsString(_head));
        File tmpcommit = join(_workDirectory, "tmpcommit");

        if (message.isEmpty()) {
            System.out.println("Please enter a commit message.");
            return;
        }

        Status status = readStatus();
        String[] removeList = new String[status.getRemovedFile().size()];
        String[] changeList = new String[status.getBlob().size()];

        if (status.getBlob().isEmpty() && status.getRemovedFile().isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }


        ArrayList<Blob> blobs = new ArrayList<>();
        Blob[] blobs1 = new Blob[blobs.size()];
        for (Blob blob1 : status.getBlob()) {
            blobs.add(blob1);
        }
        for (Blob blob
                : currentCommit().getBlobs()) {
            if (blobs.stream().anyMatch(blob1 ->
                    blob1.getFileName().equals(blob.getFileName()))) {
                continue;
            }
            blobs.add(blob);
        }
        blobs.removeIf(blob ->
                status.getRemovedFile().contains(blob.getFileName()));


        Arrays.copyOf(status.getBlob().stream().map(blob ->
                        blob.getFileName()).toArray(),
                changeList.length, String[].class);

        removeList = status.getRemovedFile().toArray(removeList);

        Commit initial = new Commit(message,
                currentCommitSHA(),
                blobs.toArray(blobs1),
                Stream.of(changeList, removeList).
                        flatMap(Stream::of).toArray(String[]::new),
                removeList);
        if (!tmpcommit.exists()) {
            tmpcommit.createNewFile();
        }
        writeObject(tmpcommit, initial);
        String sha = sha1(readContents(tmpcommit));
        File commit = join(_commits, sha);
        tmpcommit.renameTo(commit);
        if (currentBranch.exists()) {
            writeContents(currentBranch, sha);
            saveStatus(new Status(new Blob[0], new String[0]));
        }
    }

    /**
     * add;.
     *
     * @param path to path
     */
    public void add(String path) {
        File file = join(_directory, path);
        if (file.exists()) {

            Status status = readStatus();
            if (status.getRemovedFile().contains(path)) {
                status.getRemovedFile().remove(path);
            } else {
                Blob blob = new Blob(path, readContentsAsString(file),
                        sha1(readContents(file)));
                if (!Arrays.stream(currentCommit().getBlobs()).anyMatch(blob1 ->
                        blob1.getSha().equals(blob.getSha())
                                && blob1.getFileName().equals(path))) {
                    status.addBlobs(blob);
                }
            }
            saveStatus(status);
        } else {
            System.out.println("File does not exist.");
            return;
        }

    }

    /**
     * rm;.
     *
     * @param path to path
     */
    public void rm(String path) {
        Status status = readStatus();
        Commit currentCommit = currentCommit();
        if (Arrays.stream(currentCommit.getBlobs()).
                anyMatch(blob -> blob.getFileName().equals(path))) {
            status.addremovedFile(path);
            status.getBlob().removeIf(blob ->
                    blob.getFileName().equals(path));
            saveStatus(status);
        } else if (status.getBlob().stream().anyMatch(blob ->
                blob.getFileName().equals(path))) {
            status.getBlob().removeIf(blob ->
                    blob.getFileName().equals(path));
            saveStatus(status);
        } else {
            System.out.println("No reason to remove the file.");
            return;
        }
        File file = join(_directory, path);
        if (file.exists()
                && Arrays.stream(currentCommit().getBlobs())
                .anyMatch(blob -> blob.getFileName().equals(path))) {
            file.delete();
        }
    }

    /**
     * log;.
     */
    public void log() {
        String shaa = currentCommitSHA();
        Commit commit = readObject(join(_commits, shaa), Commit.class);
        while (!commit.getParrentSHA().isEmpty()) {
            System.out.println("===\ncommit " + shaa.substring(0, 9)
                    + "\nDate: " + commit.getTimestamp()
                    + "\n" + commit.getMessage() + "\n");
            shaa = commit.getParrentSHA();
            commit = readObject(join(_commits, shaa), Commit.class);
        }
        System.out.println("===\ncommit " + shaa.substring(0, 9)
                + "\nDate: " + commit.getTimestamp()
                + "\n" + commit.getMessage());
    }

    /**
     * global_log;.
     */
    public void globalLog() {
        for (File file : _commits.listFiles()) {
            Commit commit = readObject(file, Commit.class);
            System.out.println("===\ncommit " + file.getName().substring(0, 9)
                    + "\nDate: " + commit.getTimestamp()
                    + "\n" + commit.getMessage() + "\n");
        }
    }

    /**
     * find;.
     *
     * @param meesage to meesage
     */
    public void find(String meesage) {
        int count = 0;
        for (File file : _commits.listFiles()) {
            Commit commit = readObject(file, Commit.class);
            String commitMessage = commit.getMessage().toLowerCase();
            if (commitMessage.contains(meesage.toLowerCase())) {
                if (count > 0) {
                    System.out.println();
                }
                System.out.print(file.getName().substring(0, 9));
                count++;
            }
        }
        if (count == 0) {
            System.out.println("Found no commit with that message.");
        }

    }

    /**
     * find;.
     *
     * @param fileList to fileList
     * @param path     to path
     * @return untrackList
     */
    private ArrayList<String> checkUntrack(
            ArrayList<String> fileList, File path) throws IOException {
        ArrayList<String> untrackList = new ArrayList();

        if (path.isDirectory()) {
            for (File file : path.listFiles()) {
                if (file.isFile()) {
                    if (!fileList.stream().anyMatch(s ->
                            join(_directory, s).getPath().
                                    equals(file.getPath()))) {
                        untrackList.add(file.getName());
                    }
                }
                if (file.isDirectory() && !file.getName().equals(".gitlet")) {
                    checkUntrack(fileList, file).stream().map(s ->
                                    file.getName() + "/" + s).
                            forEach(s -> untrackList.add(s));
                }
            }
        }
        return untrackList;
    }

    /**
     * status;.
     */
    public void status() throws IOException {
        final ArrayList<String> fileList = new ArrayList<>();
        Status status = readStatus();

        for (Blob blob : currentCommit().getBlobs()) {
            fileList.add(blob.getFileName());
        }

        for (Blob blob : status.getBlob()) {
            if (!fileList.contains(blob.getFileName())) {
                fileList.add(blob.getFileName());
            }
        }

        System.out.println("=== Branches ===");
        for (File file : _branches.listFiles()) {
            if (currentBranch().getName().equals(file.getName())) {
                System.out.println("*" + file.getName());
            } else {
                System.out.println(file.getName());
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        for (Blob blob : status.getBlob()) {
            System.out.println(blob.getFileName());
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (String removedfile : status.getRemovedFile()) {
            System.out.println(removedfile);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        for (Blob blob : currentCommit().getBlobs()) {
            File file = join(_directory, blob.getFileName());
            if (!(status.getRemovedFile().contains(blob.getFileName())
                    || status.getBlob().stream()
                    .anyMatch(blob1 -> blob1.getFileName().
                            equals(blob.getFileName())))) {
                if (file.exists()) {
                    String sha = sha1(readContents(file));
                    if (!sha.equals(blob.getSha())) {
                        System.out.println(file.getName() + " (modified)");
                    }
                } else {
                    System.out.println(file.getName() + " (deleted)");
                }
            }
        }

        System.out.println();
        System.out.println("=== Untracked Files ===");
        for (String s : checkUntrack(fileList, _directory)) {
            System.out.println(s);
        }
        System.out.println();
    }

    /**
     * find;.
     *
     * @param args to args
     */
    public void checkout(String args) throws IOException {
        File branch = join(_branches, args);
        Status status = readStatus();
        ArrayList<String> fileList = new ArrayList<>();

        for (Blob blob : currentCommit().getBlobs()) {
            fileList.add(blob.getFileName());
        }

        ArrayList<String> untrackList = checkUntrack(fileList, _directory);

        if (!status.getBlob().isEmpty()
                || !status.getRemovedFile().isEmpty()
                || !untrackList.isEmpty()) {
            System.out.println("There is an untracked file in the way; "
                    + "delete it, or add and commit it first.");
        }


        if (branch.exists()) {
            if (currentBranch().getName().equals(args)) {
                System.out.println("No need to checkout the current branch.");
                return;
            }
            File file = join(_commits, readContentsAsString(branch));
            Commit commit = readObject(file, Commit.class);
            fileList.clear();
            for (Blob blob : commit.getBlobs()) {
                fileList.add(blob.getFileName());
                writeContents(join(_directory,
                        blob.getFileName()), blob.getContent());
            }
            untrackList = checkUntrack(fileList, _directory);
            for (String s : untrackList) {
                join(_directory, s).delete();
            }

            writeContents(_head, "heads/" + args);
        } else {
            System.out.println("No such branch exists.");
            return;
        }


    }

    /**
     * find;.
     *
     * @param args     to args
     * @param filename to filename
     */
    public void checkout(String args, String filename) {
        Commit commit;
        if (args.isEmpty()) {
            commit = currentCommit();
        } else {
            File commitFile = null;
            for (File file : _commits.listFiles()) {
                if (file.getName().contains(args)) {
                    commitFile = file;
                    break;
                }
            }

            if (commitFile == null) {
                System.out.println("No commit with that id exists.");
                return;
            }
            commit = readObject(commitFile, Commit.class);
        }

        for (Blob blob : commit.getBlobs()) {
            if (blob.getFileName().equals(filename)) {
                writeContents(join(_directory,
                        blob.getFileName()), blob.getContent());
                return;
            }
        }
        System.out.println("File does not exist in that commit.");
    }

    /**
     * branch;.
     *
     * @param branch to branch
     */
    public void branch(String branch) throws IOException {
        File file = join(_branches, branch);
        if (file.exists()) {
            System.out.println("A branch with that name already exists.");
            return;
        } else {
            file.createNewFile();
            writeContents(file, readContentsAsString(join(_workDirectory,
                    readContentsAsString(_head))));
            return;
        }
    }

    /**
     * rm_branch;.
     *
     * @param branch to branch
     */
    public void rmBranch(String branch) throws IOException {
        File file = join(_branches, branch);
        Status status = readStatus();
        if (file.exists()) {
            if (currentBranch().getName().equals(branch)) {
                System.out.println("Cannot remove the current branch.");
                return;
            }
            file.delete();
            return;
        } else {
            System.out.println("A branch with that name does not exist.");
            return;
        }
    }

    /**
     * reset;.
     *
     * @param commitId to commitId
     */
    public void reset(String commitId) throws IOException {
        Commit currentCommit = currentCommit();
        ArrayList<String> fileList = new ArrayList<>();

        for (Blob blob : currentCommit.getBlobs()) {
            fileList.add(blob.getFileName());
        }
        for (Blob blob : readStatus().getBlob()) {
            fileList.add(blob.getFileName());
        }
        if (!checkUntrack(fileList, _directory).isEmpty()) {
            System.out.println("There is an untracked "
                    + "file in the way; delete it,"
                    + " or add and commit it first.");
            return;
        }

        File commitFile = null;
        for (File file : _commits.listFiles()) {
            if (file.getName().contains(commitId)) {
                commitFile = file;
                break;
            }
        }

        if (commitFile == null || !commitFile.exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit commit = readObject(commitFile, Commit.class);
        for (Blob blob : currentCommit.getBlobs()) {
            File file1 = join(_directory, blob.getFileName());
            if (file1.exists()) {
                file1.delete();
            }
        }
        fileList.clear();
        for (Blob blob : commit.getBlobs()) {
            File file1 = join(_directory, blob.getFileName());
            fileList.add(blob.getFileName());
            if (!file1.exists()) {
                file1.createNewFile();
                writeContents(file1, blob.getContent());
            }
        }
        ArrayList<String> untrackList = checkUntrack(fileList, _directory);
        for (String s : untrackList) {
            join(_directory, s).delete();
        }

        saveStatus(new Status(new Blob[0], new String[0]));
        writeContents(join(_workDirectory,
                readContentsAsString(_head)), commitFile.getName());

    }

    /**
     * reset;.
     *
     * @param branchName  to branchName
     * @param mergeBranch to mergeBranch
     * @return true
     */
    public boolean mergeErr(File mergeBranch,
                            String branchName) throws IOException {
        if (currentBranch().getName().equals(branchName)) {
            System.out.println("Cannot merge a branch with itself.");
            return false;
        }
        if (!mergeBranch.exists()) {
            System.out.println("A branch with that name does not exist.");
            return false;
        }

        ArrayList<String> fileList = new ArrayList<>();

        if (!readStatus().getBlob().isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return false;

        }

        for (Blob blob : currentCommit().getBlobs()) {
            fileList.add(blob.getFileName());
        }

        if (!checkUntrack(fileList, _directory).isEmpty()) {
            System.out.println("There is an untracked file in the way; "
                    + "delete it, or add and commit it first.");
            return false;
        }
        return true;
    }

    /**
     * reset;.
     *
     * @param currentBranchParent to currentBranchParent
     * @param mergeBranchParent   to mergeBranchParent
     * @param mergeSHA            to mergeSHA
     * @return true
     */
    public boolean mergeErr2(ArrayList<String> currentBranchParent,
                             ArrayList<String> mergeBranchParent,
                             String mergeSHA) throws IOException {

        if (currentBranchParent.contains(mergeSHA)) {
            System.out.println("Given branch is an "
                    + "ancestor of the current branch.");
            return false;
        }

        if (mergeBranchParent.contains(currentCommitSHA())) {
            reset(mergeSHA);
            System.out.println("Current branch fast-forwarded.");
            return false;
        }
        return true;
    }

    /**
     * reset;.
     *
     * @param currentBranchParent to currentBranchParent
     * @param mergeBranchParent   to mergeBranchParent
     * @param currentFileList     to currentFileList
     * @param mergeCommit         to mergeCommit
     * @param currentCommit       to currentCommit
     * @param splitPointCommit    to splitPointCommit
     */
    public void mergeExist(ArrayList<String> currentBranchParent,
                           ArrayList<String> mergeBranchParent,
                           ArrayList<String> currentFileList,
                           Commit mergeCommit,
                           Commit currentCommit, Commit splitPointCommit) {
        for (Blob blob : mergeCommit.getBlobs()) {
            if (currentFileList.contains(blob.getFileName())) {
                Blob currentblob = null;
                for (Blob blob1 : currentCommit.getBlobs()) {
                    if (blob1.getFileName().equals(blob.getFileName())) {
                        currentblob = blob1;
                    }
                }
                if (currentblob != null) {
                    String content = blob.getContent();
                    HashSet<String> mergeParentSHA = new HashSet<>();
                    for (String s : mergeBranchParent) {
                        Commit commit = readObject(
                                join(_commits, s), Commit.class);
                        for (Blob blob1
                                : commit.getBlobs()) {
                            if (blob1.getFileName().
                                    equals(blob.getFileName())) {
                                mergeParentSHA.add(blob1.getSha());
                            }
                        }
                    }
                    Boolean isPast = false;
                    for (String s : currentBranchParent) {
                        Commit commit = readObject(
                                join(_commits, s), Commit.class);
                        for (Blob blob1
                                : commit.getBlobs()) {
                            if (blob1.getFileName().equals(blob.getFileName())
                                    && blob1.getSha().equals(blob.getSha())) {
                                isPast = true;
                            }
                        }
                    }
                    if (isPast) {
                        continue;
                    }
                    if (!mergeParentSHA.contains(
                            currentblob.getSha())) {
                        content = "<<<<<<< HEAD\n"
                                + currentblob.getContent() + "=======\n"
                                + blob.getContent() + ">>>>>>>\n";
                        System.out.println(
                                "Encountered a merge conflict.");
                    }
                    writeContents(join(_directory,
                            blob.getFileName()), content);
                    add(blob.getFileName());
                }
            } else {
                if (!Arrays.stream(splitPointCommit.getBlobs()).anyMatch
                        (blob1 -> blob1.getSha().equals(blob.getSha()))) {
                    writeContents(join(_directory,
                            blob.getFileName()), blob.getContent());
                    add(blob.getFileName());
                }
            }
        }
    }

    /**
     * reset;.
     *
     * @param mergeBranchParent to currentBranchParent
     * @param mergeCommit       to mergeBranchParent
     * @param currentCommit     to currentFileList
     */
    public void mergeNoExist(
            ArrayList<String> mergeBranchParent,
            Commit mergeCommit,
            Commit currentCommit
    ) {
        ArrayList<Blob> tmpRemoveList = new ArrayList<>();
        ArrayList<String> removeList = new ArrayList<>();
        for (String s : mergeBranchParent) {
            Commit commit = readObject(join(_commits, s), Commit.class);
            for (Blob blob : commit.getBlobs()) {
                if (!Arrays.stream(mergeCommit.getBlobs()).
                        anyMatch(blob1 -> blob1.getFileName().
                                equals(blob.getFileName()))) {
                    if (!tmpRemoveList.contains(blob.getFileName())) {
                        tmpRemoveList.add(blob);
                    }
                }
            }
        }

        boolean ran = false;
        for (Blob tmpBlob : tmpRemoveList) {
            for (Blob blob : currentCommit.getBlobs()) {
                if (blob.getSha().equals(tmpBlob.getSha())) {
                    removeList.add(tmpBlob.getFileName());
                    rm(tmpBlob.getFileName());
                } else if (blob.getFileName().equals(tmpBlob.getFileName())) {
                    String content = blob.getContent();
                    if (Arrays.stream(currentCommit.getBlobs()).
                            anyMatch(blob1 -> blob1.getFileName().
                                    equals(blob.getFileName()))) {
                        content = "<<<<<<< HEAD\n"
                                + blob.getContent() + "=======\n"
                                + ">>>>>>>\n";
                        if (!ran) {
                            System.out.println(
                                    "Encountered a merge conflict.");
                        }
                        ran = true;
                    }
                    writeContents(join(_directory,
                            blob.getFileName()), content);
                    add(blob.getFileName());
                }
            }
        }
    }


    /**
     * reset;.
     *
     * @param branchName to branchName
     */
    public void merge(String branchName) throws IOException {
        File branch = currentBranch();
        File mergeBranch = join(_branches, branchName);
        if (!mergeErr(mergeBranch, branchName)) {
            return;
        }
        Commit currentCommit = currentCommit();
        ArrayList<String> currentFileList = new ArrayList<>();
        ArrayList<Blob> resultBlob = new ArrayList<>();
        for (Blob blob : currentCommit.getBlobs()) {
            currentFileList.add(blob.getFileName());
            resultBlob.add(blob);
        }
        String mergeSHA = readContentsAsString(mergeBranch);
        Commit mergeCommit = readObject(join(_commits, mergeSHA), Commit.class);
        Map<String, String> resultMap = new HashMap<>();
        ArrayList<String> mergeBranchParent = new ArrayList<>();
        ArrayList<String> currentBranchParent = new ArrayList<>();

        Commit mergeCommitHist = mergeCommit;
        mergeBranchParent.add(mergeSHA);
        while (!mergeCommitHist.getParrentSHA().isEmpty()) {
            mergeBranchParent.
                    add(mergeCommitHist.getParrentSHA());
            mergeCommitHist = readObject(join
                    (_commits, mergeCommitHist.getParrentSHA()), Commit.class);
        }
        mergeCommitHist = currentCommit;
        currentBranchParent.add(currentCommitSHA());
        while (!mergeCommitHist.getParrentSHA().isEmpty()) {
            currentBranchParent.add(mergeCommitHist.getParrentSHA());
            mergeCommitHist = readObject(
                    join(_commits, mergeCommitHist.
                            getParrentSHA()), Commit.class);
        }


        String[] splitPoints = mergeBranchParent.stream().
                filter(s -> currentBranchParent.contains(s)).
                toArray(String[]::new);
        String splitPoint = splitPoints[0];
        Commit splitPointCommit = readObject(
                join(_commits, splitPoint), Commit.class);

        if (!mergeErr2(currentBranchParent, mergeBranchParent, mergeSHA)) {
            return;
        }

        mergeExist(currentBranchParent, mergeBranchParent,
                currentFileList, mergeCommit, currentCommit, splitPointCommit);

        mergeNoExist(mergeBranchParent, mergeCommit, currentCommit);


        commit("Merged " + branchName + " into " + branch.getName() + ".");
        return;
    }
}
