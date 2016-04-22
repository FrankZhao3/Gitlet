import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class CommitObject implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private Integer currId; // no repeated id
    private Integer prevId;
    private Integer branchId;
    private String currBranch;
    private HashSet<String> removeBranchList;
    // public HashMap<String, Branch> branchList;
    private HashMap<String, Integer> branchToId;
    private HashMap<Integer, CommitNode> idToCommitNode;
    private HashMap<Integer, HashMap<String, String>> idToFiles;

    public CommitObject() {
        prevId = null;
        currId = 0;
        branchId = currId;
        currBranch = "master";
        // branchList = new HashMap<String, Branch>();
        branchToId = new HashMap<String, Integer>();
        idToCommitNode = new HashMap<Integer, CommitNode>();
        removeBranchList = new HashSet<String>();
        CommitNode initCommit = new CommitNode();
        branchToId.put(currBranch, currId);
        idToCommitNode.put(currId, initCommit);
        // branchList.put("master", MasterBranch);
        idToFiles = new HashMap<Integer, HashMap<String, String>>();
    }

    public HashMap<String, Integer> getBranchToId() {
        return branchToId;
    }

    public HashMap<Integer, CommitNode> getIdToCommitNode() {
        return idToCommitNode;
    }

    public HashMap<Integer, HashMap<String, String>> getIdToFiles() {
        return idToFiles;
    }

    public void commitAdd(ArrayList<String> fileNameList, String message)
            throws IOException {
        GlobalLog gLog = (GlobalLog) Gitlet.tryLoading(".gitlet"
                + "/.globalLog.ser");
        Status gitStatus = (Status) Gitlet.tryLoading(".gitlet"
                + "/.status.ser");
        branchId = branchToId.get(currBranch);
        prevId = branchId;
        currId += 1;
        Integer tempId = 0;
        gLog.add(currId, message);
        HashMap<String, String> fileArrayList = new HashMap<String, String>();
        Gitlet.saveMyObject(gLog, ".gitlet" + "/.globalLog.ser");
        CommitNode newFileList = new CommitNode(currId, prevId, message,
                new HashMap<String, String>());
        // inheritance
        if (prevId >= 1) {
            // System.out.println();
            if (idToCommitNode.get(prevId) != null) {
                // System.out.println("inhert");
                // System.out.println("getPreId() " + getPreId());
                for (String name : idToCommitNode.get(prevId)
                        .getFileTofilePath().keySet()) {
                    if (!gitStatus.FileMarkedForRemovalList.contains(name)) {
                        newFileList.getFileTofilePath().put(
                                name,
                                idToCommitNode.get(prevId).getFileTofilePath()
                                        .get(name));
                    }
                }
            }
        }
        for (String fileName : fileNameList) {
            String filePath = ".gitlet/.commit/" + "file" + currId + tempId;
            File newFile = new File(fileName);
            File fileToBeSaved = new File(filePath);
            fileArrayList.put(fileName, filePath);
            Files.copy(newFile.toPath(), fileToBeSaved.toPath());
            if (newFile.exists()) {
                if (!gitStatus.FileMarkedForRemovalList.contains(fileName)) {
                    newFileList.addFile(fileName, filePath);
                    // System.out.println("added" + fileName + "id" + currId);
                }
            } else {
                System.out.println("This staged file no longer exist!");
            }
            tempId++;
        }
        // System.out.println(currBranch + "id " + currId);
        branchToId.put(currBranch, currId);
        branchId = currId;
        idToCommitNode.put(currId, newFileList);
        gitStatus.FileMarkedForRemovalList.clear();
        gitStatus.stagedFileList.clear();
        if (idToCommitNode.get(currId) != null) {
            idToFiles.put(currId, new HashMap<String, String>(idToCommitNode
                    .get(currId).getFileTofilePath()));
        }
        Gitlet.saveMyObject(gitStatus, ".gitlet" + "/.status.ser");
    }

    public void commitAddForRebase(CommitNode node) {
        GlobalLog gLog = (GlobalLog) Gitlet.tryLoading(".gitlet"
                + "/.globalLog.ser");
        Status gitStatus = (Status) Gitlet.tryLoading(".gitlet"
                + "/.status.ser");
        branchId = branchToId.get(currBranch);
        prevId = branchId;
        currId += 1;
        gLog.add(currId, node.getInf().message);
        CommitNode newFileList = new CommitNode(currId, prevId,
                node.getInf().message, node.getFileTofilePath());
        branchToId.put(currBranch, currId);
        branchId = currId;
        idToCommitNode.put(currId, newFileList);
        if (idToCommitNode.get(currId) != null)
            idToFiles.put(currId, new HashMap<String, String>(idToCommitNode
                    .get(currId).getFileTofilePath()));
        Gitlet.saveMyObject(gitStatus, ".gitlet" + "/.status.ser");
    }

    public void initAdd() {
        GlobalLog gLog = (GlobalLog) Gitlet.tryLoading(".gitlet"
                + "/.globalLog.ser");
        if (gLog == null) {
            gLog = new GlobalLog();
        }
        gLog.add(currId, "initial commit");
        // getBranch().branchLog.add(currId, "initial commit");
        Gitlet.saveMyObject(gLog, ".gitlet" + "/.globalLog.ser");
    }

    public CommitNode getBranchNode() {
        // System.out.println("*"+idToCommitNode.get(branchToId.get(currBranch)).getCurrId());
        branchToId.get(currBranch);
        return idToCommitNode.get(branchToId.get(currBranch));
    }

    public CommitNode getBranchNode(String branchName) {
        return idToCommitNode.get(branchToId.get(branchName));
    }

    public File getHeadFile(String fileName) {
        // System.out.println(currId);
        if (getBranchNode() == null
                || getBranchNode().getFileTofilePath().get(fileName) == null) {
            return null;
        } else {
            // System.out.println(headId.get(fileName));
            return new File(getBranchNode().getFileTofilePath().get(fileName));
        }
    }

    public void checkoutAcommit(Integer id, String fileName) throws IOException {
        if (idToFiles.get(id) == null) {
            System.out.println("No commit with that id exists.");
            return;
        }
        if (idToFiles.get(id).get(fileName) == null) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        File newFile = new File(idToCommitNode.get(id).getFileTofilePath()
                .get(fileName));
        // System.out.println(idToCommitNode.get(id).getFileTofilePath().get(fileName));
        if (!newFile.exists()) {
            System.out
                    .println("File does not exist in the most recent commit, or no such branch exists.");
        } else {
            File toBeUpdated = new File(fileName);
            if (toBeUpdated.exists()) {
                toBeUpdated.delete();
                Files.copy(newFile.toPath(), toBeUpdated.toPath());
            } else {
                Files.copy(newFile.toPath(), toBeUpdated.toPath());
            }
        }
    }

    public File getHeadFileWithGivenBranch(String fileName, String branchName) {
        if (getBranchNode(branchName).getFileTofilePath().get(fileName) == null) {
            return null;
        } else {
            // System.out.println(headId.get(fileName));
            return new File(getBranchNode(branchName).getFileTofilePath().get(
                    fileName));
        }
    }

    public void switchBranchPointer(Integer thebranchId) {
        prevId = branchId;
        branchId = thebranchId;
        branchToId.put(currBranch, thebranchId);
    }

    public void createABranch(String branchName) {
        branchToId.put(branchName, currId);
        currBranch = branchName;
        // branchId = currId;
    }

    public Set<String> getBranchListMap() {
        return branchToId.keySet();
    }

    public void switchABranch(String branchName) {
        currBranch = branchName;
    }

    public void removeBranch(String branchName) {
        removeBranchList.add(branchName);
    }

    public Set<String> getFileListbyId(Integer id) {
        return idToFiles.get(id).keySet();
    }

    // find common ancestor
    public void merge(String givenBranch) throws IOException {
        CommitNode currBranchNode = idToCommitNode.get(branchToId
                .get(currBranch));
        CommitNode givenBranchNode = idToCommitNode.get(branchToId
                .get(givenBranch));
        CommitNode parent = splitingPointer(givenBranch);
        parent.outputInfo();
        // System.out.println("^^^^^");
        // //givenBranchNode.outputInfo();
        // System.out.println(branchToId.get(givenBranch));
        // System.out.println("^^^^^^");
        for (String fileName : givenBranchNode.getFileTofilePath().keySet()) {
            // System.out.println("****" + "fileName " + fileName + "****");
            if (givenFileModified(parent, givenBranchNode, fileName)
                    && !currFileModified(parent, currBranchNode, fileName)) {
                // System.out.println("****" + "write " + fileName + "****");
                File newFile = new File(givenBranchNode.getFileTofilePath()
                        .get(fileName));
                File fileToBeUpdated = new File(fileName);
                if (newFile.exists()) {
                    Files.copy(newFile.toPath(), fileToBeUpdated.toPath(),
                            StandardCopyOption.REPLACE_EXISTING);
                }
            } else if (givenFileModified(parent, givenBranchNode, fileName)
                    && currFileModified(parent, currBranchNode, fileName)) {
                // System.out.println("****" + "conflict " + fileName + "****");
                File newFile = new File(givenBranchNode.getFileTofilePath()
                        .get(fileName));
                File FileToBeUpdated = new File(fileName + ".conflicted");
                if (newFile.exists()) {
                    Files.copy(newFile.toPath(), FileToBeUpdated.toPath(),
                            StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    public boolean givenFileModified(CommitNode parent,
            CommitNode givenBranchNode, String fileName) {
        if (parent.getFileTofilePath().get(fileName) == null) {
            return true;
        } else if (givenBranchNode.getFileTofilePath().get(fileName)
                .equals((parent.getFileTofilePath().get(fileName)))) {
            return false;
        } else {
            return true;
        }
    }

    public boolean currFileModified(CommitNode parent,
            CommitNode currCommitNode, String fileName) {
        if (currCommitNode.getFileTofilePath().get(fileName) == null) {
            return false;
        } else if ((parent.getFileTofilePath().get(fileName) == null)
                || !currCommitNode.getFileTofilePath().get(fileName)
                        .equals(parent.getFileTofilePath().get(fileName))) {
            return true;
        } else {
            return false;
        }
    }

    public void rebase(String givenBranch) throws IOException {
        // CommitNode currBranchNode =
        // idToCommitNode.get(branchToId.get(currBranch));
        if (branchToId.get(givenBranch) == null) {
            System.out.println(" A branch with that name does not exist.");
            return;
        }
        if (givenBranch == currBranch) {
            System.out.println("Cannot rebase a branch onto itself");
            return;
        }
        Stack<CommitNode> stackForRebaseOnly = new Stack<CommitNode>();
        CommitNode givenBranchNode = idToCommitNode.get(branchToId
                .get(givenBranch));
        CommitNode parent = splitingPointer(givenBranch);
        traverseCopy(parent, givenBranchNode, stackForRebaseOnly);
        switchBranchPointer(branchToId.get(givenBranch));
        for (CommitNode node : stackForRebaseOnly) {
            commitAddForRebase(node);
        }
        for (String fileName : givenBranchNode.getFileTofilePath().keySet()) {
            // System.out.println("****"+ "write" + fileName + "****");
            File newFile = new File(givenBranchNode.getFileTofilePath().get(
                    fileName));
            File fileToBeUpdated = new File(fileName);
            if (newFile.exists()) {
                Files.copy(newFile.toPath(), fileToBeUpdated.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    public void irebase(String givenBranch) throws IOException {
        // CommitNode currBranchNode =
        // idToCommitNode.get(branchToId.get(currBranch));
        if (branchToId.get(givenBranch) == null) {
            System.out.println(" A branch with that name does not exist.");
            return;
        }
        if (givenBranch == currBranch) {
            System.out.println("Cannot rebase a branch onto itself");
            return;
        }
        Stack<CommitNode> stackForRebaseOnly = new Stack<CommitNode>();
        CommitNode givenBranchNode = idToCommitNode.get(branchToId
                .get(givenBranch));
        CommitNode parent = splitingPointer(givenBranch);
        traverseCopy(parent, givenBranchNode, stackForRebaseOnly);
        switchBranchPointer(branchToId.get(givenBranch));
        for (CommitNode node : stackForRebaseOnly) {
            char x = askQuestion();
            if (x == 'c') {
                commitAddForRebase(node);
            } else if (x == 's') {
                if (node.getInf().message == "initial commit"
                        || node.getCurrId().equals(givenBranchNode.getCurrId())) {
                    char y = askQuestion();
                    if (y == 'c') {
                        commitAddForRebase(node);
                    } else if (y == 'm') {
                        System.out
                                .println("Please enter a new message for this commit.");
                        Scanner scanner = new Scanner(System.in);
                        String s = scanner.nextLine();
                        node.getInf().message = s;
                        commitAddForRebase(node);
                        scanner.close();
                    }
                }
            } else if (x == 'm') {
                System.out
                        .println("Please enter a new message for this commit.");
                Scanner scanner = new Scanner(System.in);
                String s = scanner.nextLine();
                node.getInf().message = s;
                commitAddForRebase(node);
                scanner.close();
            }
        }
        for (String fileName : givenBranchNode.getFileTofilePath().keySet()) {
            // System.out.println("****"+ "write" + fileName + "****");
            File newFile = new File(givenBranchNode.getFileTofilePath().get(
                    fileName));
            File FileToBeUpdated = new File(fileName);
            if (newFile.exists()) {
                Files.copy(newFile.toPath(), FileToBeUpdated.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    public char askQuestion() {
        System.out.println("Currently replaying:");
        System.out
                .println("Would you like to (c)ontinue, (s)kip this commit, or change this commit's (m)essage?");
        while (true) {
            Scanner scanner = new Scanner(System.in);
            String x = scanner.nextLine();
            scanner.close();
            if (x.equals("c")) {
                return 'c';
            } else if (x.equals("s")) {
                return 's';
            } else if (x.equals("m")) {
                return 'm';
            } else {
                System.out.println("Enter again!.");
            }
        }
    }

    public void traverseCopy(CommitNode parent, CommitNode givenBranchNode,
            Stack<CommitNode> stack) {
        if (parent.getCurrId() == givenBranchNode.getCurrId()) {
            return;
        } else {
            stack.push(givenBranchNode);
            traverseCopy(parent,
                    idToCommitNode.get(givenBranchNode.getPreId()), stack);
        }
    }

    public CommitNode splitingPointer(String givenBranchName) {
        if (branchToId.get(currBranch) == branchToId.get(givenBranchName)) {
            return idToCommitNode.get(branchToId.get(currBranch));
        }
        return findParent(idToCommitNode.get(branchToId.get(currBranch)),
                idToCommitNode.get(branchToId.get(givenBranchName)));
    }

    public CommitNode findParent(CommitNode currentBranchNode,
            CommitNode givenBranchNode) {
        if (currentBranchNode.getCurrId() == givenBranchNode.getCurrId()) {
            return currentBranchNode;
        } else if (currentBranchNode.getCurrId() > givenBranchNode.getCurrId()) {
            return findParent(idToCommitNode.get(currentBranchNode.getPreId()),
                    givenBranchNode);
        } else {
            return findParent(currentBranchNode,
                    idToCommitNode.get(givenBranchNode.getPreId()));
        }
    }

    public void outputLog() {
        CommitNode temp = getBranchNode();
        // System.out.println(temp.getCurrId());
        while (temp.getCurrId() > 0) {
            temp.outputInfo();
            temp = idToCommitNode.get(temp.getPreId());
        }
        temp.outputInfo();
    }

    public String getCurrBranchName() {
        return currBranch;
    }

    public Integer getCurrId() {
        return currId;
    }

    public HashSet<String> getRemoveList() {
        return removeBranchList;
    }
}
