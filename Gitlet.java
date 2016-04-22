import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Gitlet {
    public static void gitletInit() {
        File Gitletfile = new File(".gitlet");
        File commit = new File(".gitlet/" + ".commit");
        File branches = new File(".gitlet/" + ".branches");
        File log = new File(".gitlet/" + ".log");
        if (!Gitletfile.exists()) {
            try {
                Gitletfile.mkdir();
                commit.mkdir();
                branches.mkdir();
                log.mkdir();
                // make a status folder to contain Branches,
                Status gitletStatus = new Status();
                gitletStatus.addBranch("master");
                saveMyObject(gitletStatus, ".gitlet" + "/.status.ser");
                // create a commitObject.ser file
                CommitObject commitSerObj = new CommitObject();
                commitSerObj.initAdd();
                // save the object
                try {
                    File myCommitFile = new File(Gitletfile + "/.commit.ser");
                    FileOutputStream fileOut = new FileOutputStream(
                            myCommitFile);
                    ObjectOutputStream objectOut = new ObjectOutputStream(
                            fileOut);
                    objectOut.writeObject(commitSerObj);
                    objectOut.close();
                } catch (IOException e) {
                    String msg = "IOException while saving my commit.";
                    System.out.println(msg);
                }
            } catch (SecurityException se) {
                System.out.println("Weird problem happened.");
            }
        } else {
            System.out.println("A gitlet version control system already "
                    + "exists in the current directory.");
        }
    }

    public static void gitAdd(String fileName) {
        File newFile = new File(fileName);
        // check the file exist or not and add to stagedFile
        if (!newFile.exists()) {
            System.out.println("File does not exist.");
        } else {
            // check no commit object
            CommitObject commitObj = (CommitObject) tryLoading(".gitlet"
                    + "/.commit.ser");
            // System.out.println(" ");
            Status gitStatus = (Status) tryLoading(".gitlet" + "/.status.ser");
            if (commitObj != null && commitObj.getHeadFile(fileName) != null) {
                // compare the file in the commit
                // System.out.println("COMPARE");
                if (fileCompare(((CommitObject) tryLoading(".gitlet"
                        + "/.commit.ser")).getHeadFile(fileName), newFile)) {
                    System.out
                            .println("File has not been modified since the last commit.");
                    return;
                }
            }
            // compare if it is the same file
            if (!gitStatus.getStagedFileList().contains(fileName)) {
                gitStatus.addStagedFileList(fileName);
            }
            saveMyObject(gitStatus, ".gitlet" + "/.status.ser");
        }
    }

    // Remember to edit this method
    private static boolean fileCompare(File file1, File file2) {
        boolean isSame = false;
        if (file1.exists() && file2.exists()) {
            try {
                isSame = Arrays.equals(Files.readAllBytes(file1.toPath()),
                        Files.readAllBytes(file2.toPath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return isSame;
    }

    public static void commit(String message) throws IOException {
        Status gitStatus = (Status) tryLoading(".gitlet" + "/.status.ser");
        ArrayList<String> fileNameList = gitStatus.stagedFileList;
        if (fileNameList.isEmpty()
                && gitStatus.FileMarkedForRemovalList.isEmpty()) {
            System.out.println("Nothing to commit");
            return;
        }
        CommitObject myCommit = (CommitObject) tryLoading(".gitlet"
                + "/.commit.ser");
        if (myCommit != null) {
            myCommit.commitAdd(fileNameList, message);
        } else {
            myCommit = new CommitObject();
            myCommit.commitAdd(fileNameList, message);
        }
        saveMyObject(myCommit, ".gitlet" + "/.commit.ser");
    }

    public static Object tryLoading(String directory) {
        Object myObject = null;
        File myCommitFile = new File(directory);
        if (myCommitFile.exists()) {
            try {
                FileInputStream fileIn = new FileInputStream(myCommitFile);
                ObjectInputStream objectIn = new ObjectInputStream(fileIn);
                myObject = objectIn.readObject();
            } catch (IOException e) {
                String msg = "IOException while loading the object.";
            } catch (ClassNotFoundException e) {
                String msg = "ClassNotFoundException while loading myCommit.";
                System.out.println(msg);
            }
        }
        return myObject;
    }

    public static void saveMyObject(Object myCommit, String directory) {
        if (myCommit == null) {
            return;
        }
        try {
            File myCommitFile = new File(directory);
            FileOutputStream fileOut = new FileOutputStream(myCommitFile);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(myCommit);
        } catch (IOException e) {
            String msg = "IOException while saving myCat.";
            System.out.println(msg);
            System.out.println(e);
        }
    }

    public static void checkoutFileOrBranch(String branchOrFile) {
        CommitObject commit = (CommitObject) tryLoading(".gitlet"
                + "/.commit.ser");
        Status astatus = (Status) tryLoading(".gitlet" + "/.status.ser");
        System.out.println(commit.getCurrBranchName());
        if (commit.getBranchToId().keySet().contains(branchOrFile)) {
            if (commit.getRemoveList().contains(branchOrFile)) {
                System.out
                        .println("File does not exist in the most recent commit, or no such branch exists.");
                return;
            }
            if (commit.getCurrBranchName().equals(branchOrFile)) {
                System.out.println("No need to checkout the current branch.");
                return;
            }

            commit.switchABranch(branchOrFile);
            restoreFilesCurrentBranch(commit, branchOrFile);
            if (astatus.containsBranch(branchOrFile))
                astatus.switchHeadBranch(branchOrFile);
            saveMyObject(commit, ".gitlet" + "/.commit.ser");
            saveMyObject(astatus, ".gitlet" + "/.status.ser");
            return;
        }
        commitCheckoutFile(branchOrFile);
    }

    public static void commitCheckoutFile(String fileName) {
        CommitObject myCommit = (CommitObject) tryLoading(".gitlet"
                + "/.commit.ser");
        if (myCommit.getHeadFile(fileName) == null) {
            System.out
                    .println("File does not exist in the most recent commit, or no such branch exists.");
            return;
        }
        File newFile = myCommit.getHeadFile(fileName);
        File toBeUpdated = new File(fileName);
        try {
            if (toBeUpdated.exists()) {
                toBeUpdated.delete();
                Files.copy(newFile.toPath(), toBeUpdated.toPath());
            } else {
                Files.copy(newFile.toPath(), toBeUpdated.toPath());
            }
        } catch (IOException e) {
            System.out.println(e);
        }

    }

    public static void commitCheckoutIdFile(Integer id, String fileName) {
        CommitObject myCommit = (CommitObject) tryLoading(".gitlet"
                + "/.commit.ser");
        try {
            myCommit.checkoutAcommit(id, fileName);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public static ArrayList<Integer> commitFind(String message) {
        GlobalLog alog = (GlobalLog) tryLoading(".gitlet" + "/.globalLog.ser");
        ArrayList<Integer> id = alog.find(message);
        return id;
    }

    public static void createBranch(String branchName) {
        CommitObject myCommit = (CommitObject) tryLoading(".gitlet"
                + "/.commit.ser");
        Status gitStatus = (Status) tryLoading(".gitlet" + "/.status.ser");
        gitStatus.addBranch(branchName);
        myCommit.createABranch(branchName);
        saveMyObject(gitStatus, ".gitlet" + "/.status.ser");
        saveMyObject(myCommit, ".gitlet" + "/.commit.ser");
    }

    public static void removeFile(String fileName) {
        boolean containStagedFile;
        Status gitStatus = (Status) tryLoading(".gitlet" + "/.status.ser");
        CommitObject myCommit = (CommitObject) tryLoading(".gitlet"
                + "/.commit.ser");
        containStagedFile = gitStatus.removeOneStagedFile(fileName);
        gitStatus.addFileMarkedForRemoval(fileName);
        if (myCommit.getHeadFile(fileName) == null
                && containStagedFile == false) {
            System.out.println("No reason to remove the file");
        }
        saveMyObject(gitStatus, ".gitlet" + "/.status.ser");
        saveMyObject(myCommit, ".gitlet" + "/.commit.ser");
    }

    public static void removeBranch(String branchName) {
        CommitObject myCommit = (CommitObject) tryLoading(".gitlet"
                + "/.commit.ser");
        Status gitStatus = (Status) tryLoading(".gitlet" + "/.status.ser");
        if (branchName.equals(myCommit.getCurrBranchName())) {
            System.out.println("Cannot remove the current branch.");
        } else {
            gitStatus.removeBranch(branchName);
            myCommit.removeBranch(branchName);
        }
        saveMyObject(gitStatus, ".gitlet" + "/.status.ser");
        saveMyObject(myCommit, ".gitlet" + "/.commit.ser");
    }

    public static void resetCommit(Integer id) {
        if (!warnYes()) {
            return;
        }
        CommitObject myCommit = (CommitObject) tryLoading(".gitlet"
                + "/.commit.ser");
        myCommit.switchBranchPointer(id);
        for (String fileName : myCommit.getFileListbyId(id)) {
            commitCheckoutIdFile(id, fileName);
        }
        saveMyObject(myCommit, ".gitlet" + "/.commit.ser");
    }

    public static void restoreFilesCurrentBranch(CommitObject myCommit,
            String branchName) {
        for (String fileName : myCommit.getFileListbyId(myCommit.getCurrId())) {
            // System.out.print(fileName);
            commitCheckoutIdFile(myCommit.getBranchToId().get(branchName),
                    fileName);
        }
    }

    public static void mergeBranch(String givenBranchName) {
        if (!warnYes()) {
            return;
        }
        CommitObject myCommit = (CommitObject) tryLoading(".gitlet"
                + "/.commit.ser");
        try {
            if (myCommit.getCurrBranchName().equals(givenBranchName)) {
                System.out.println("Cannot merge a branch with itself");
                return;
            }
            if (myCommit.getBranchToId().get(givenBranchName) == null) {
                System.out.println("A branch with that name does not exist");
                return;
            }
            myCommit.merge(givenBranchName);
        } catch (IOException e) {
            System.out.print(e + " merge");
        }
        saveMyObject(myCommit, ".gitlet" + "/.commit.ser");
    }

    public static void rebaseBranch(String rebaseBranch) {
        CommitObject myCommit = (CommitObject) tryLoading(".gitlet"
                + "/.commit.ser");
        try {
            myCommit.rebase(rebaseBranch);
        } catch (IOException E) {
            System.out.println(E + "rebase");
        }
        saveMyObject(myCommit, ".gitlet" + "/.commit.ser");
    }

    public static void irebaseBranch(String rebaseBranch) {
        CommitObject myCommit = (CommitObject) tryLoading(".gitlet"
                + "/.commit.ser");
        try {
            myCommit.irebase(rebaseBranch);
        } catch (IOException E) {
            System.out.println(E + "rebase");
        }
        saveMyObject(myCommit, ".gitlet" + "/.commit.ser");
    }

    public static boolean warnYes() {
        System.out
                .println("Warning: The command you entered may alter the files in your directory. "
                        + "Uncommited changes may be lost. "
                        + "Are you sure you want to continue? (yes/no)");
        Scanner scanner = new Scanner(System.in);
        String x = scanner.nextLine();
        if (x.equals("yes")) {
            return true;
        } else {
            System.out.println("Did not type 'yes', so aborting.");
            return false;
        }
    }

    public static void printLog() {
        CommitObject myCommit = (CommitObject) tryLoading(".gitlet"
                + "/.commit.ser");
        myCommit.outputLog();
    }

    public static void main(String[] args) {
        switch (args[0]) {
        case "init":
            Gitlet.gitletInit();
            break;
        case "add":
            String fileName = args[1];
            Gitlet.gitAdd(fileName);
            break;
        case "commit":
            String message = args[1];
            try {
                Gitlet.commit(message);
            } catch (IOException e) {
                System.out.println(e);
            }
            break;
        case "global-log":
            GlobalLog alog = (GlobalLog) tryLoading(".gitlet"
                    + "/.globalLog.ser");
            alog.output();
            break;
        case "log":
            printLog();
            break;
        case "status":
            Status gitStatus = (Status) tryLoading(".gitlet" + "/.status.ser");
            gitStatus.output();
            break;
        case "checkout":
            if (!warnYes()) {
                return;
            }
            if (args.length <= 2) {
                String branchOrFile = args[1];
                checkoutFileOrBranch(branchOrFile);
            } else {
                commitCheckoutIdFile(Integer.parseInt(args[1]), args[2]);
            }
            break;
        case "find":
            String commitMessage = args[1];
            for (Integer i : commitFind(commitMessage)) {
                System.out.println(i);
            }
            break;
        case "branch":
            String branchName = args[1];
            createBranch(branchName);
            break;
        case "rm-branch":
            removeBranch(args[1]);
            break;
        case "rm":
            String filename = args[1];
            removeFile(filename);
            break;
        case "reset":
            Integer id = Integer.parseInt(args[1]);
            resetCommit(id);
            break;
        case "merge":
            String givenBranchName = args[1];
            mergeBranch(givenBranchName);
            break;
        case "rebase":
            String rebaseBranchName = args[1];
            rebaseBranch(rebaseBranchName);
            break;
        case "i-rebase":
            String irebaseBranchName = args[1];
            irebaseBranch(irebaseBranchName);
            break;
        default:
            System.out.println("Unrecognized command.");
            break;
        }
    }
}