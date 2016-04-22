import java.io.Serializable;
import java.util.ArrayList;

public class Status implements Serializable {
	private static final long serialVersionUID = 1L;
	ArrayList<String> branchList;
	ArrayList<String> stagedFileList;
	ArrayList<String> FileMarkedForRemovalList;
	String headBranch;

	public Status() {
		branchList = new ArrayList<String>();
		stagedFileList = new ArrayList<String>();
		FileMarkedForRemovalList = new ArrayList<String>();
		headBranch = "master";
	}

	public void addBranch(String branch) {
		branchList.add(branch);
	}

	public void addStagedFileList(String file) {
		stagedFileList.add(file);
	}

	public void addFileMarkedForRemoval(String file) {
		FileMarkedForRemovalList.add(file);
	}

	public ArrayList<String> getStagedFileList() {
		return stagedFileList;
	}

	public ArrayList<String> getBranchList() {
		return branchList;
	}

	public ArrayList<String> getFileMarkedForRemovalList() {
		return FileMarkedForRemovalList;
	}

	public boolean containsBranch(String branchName) {
		return branchList.contains(branchName);
	}

	public void switchHeadBranch(String branchName) {
		headBranch = branchName;
	}

	public void removeBranch(String branchName) {
		if (!branchList.remove(branchName)) {
			System.out.println("A branch with that name does not exist.");
		}
	}

	public void removeStagedFile() {
		stagedFileList.clear();
	}

	public boolean removeOneStagedFile(String fileName) {
		if (stagedFileList.contains(fileName)) {
			stagedFileList.remove(fileName);
			return true;
		} else {
			return false;
		}
	}

	public void output() {
		System.out.println("=== Branches ===");
		System.out.println("*" + headBranch);
		for (String temp : branchList) {
			if (!temp.equals(headBranch))
				System.out.println(temp);
		}
		System.out.println();
		System.out.println("=== Staged Files ===");
		for (String temp : stagedFileList) {
			System.out.println(temp);
		}
		System.out.println();
		System.out.println("=== Files Marked for Removal ===");
		for (String temp : FileMarkedForRemovalList) {
			System.out.println(temp);
		}
	}
}
