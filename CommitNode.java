import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class CommitNode implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer prevId;
    private Integer currentId;
    private HashMap<String, String> fileTofilePath;
    private CommitInfo inf;

    public CommitNode() {
        prevId = -1;
        currentId = 0;
        Date date = new Date();
        fileTofilePath = new HashMap<String, String>();
        inf = new CommitInfo(date, 0, "initial commit");
    }

    public CommitNode(Integer currId, Integer previousId, String commitMessage,
            HashMap<String, String> prevCommitPath) {
        prevId = previousId;
        currentId = currId;
        // check may be wrong
        Date date = new Date();
        fileTofilePath = new HashMap<String, String>(prevCommitPath);
        inf = new CommitInfo(date, currId, commitMessage);
    }

    public void addFile(String fileName, String filePath) {
        fileTofilePath.put(fileName, filePath);
    }

    public String getFile(String fileName) {
        // for(String s: fileTofilePath.keySet()){
        // System.out.println(s + "===" + fileTofilePath.get(fileName));
        // }
        String filePath = fileTofilePath.get(fileName);
        // System.out.println(returnFile.getName());
        return filePath;
    }

    public boolean isEmpty() {
        return fileTofilePath.isEmpty();
    }

    public void outputInfo() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        System.out.println("====");
        System.out.println("Commit " + inf.ID);
        System.out.println(dateFormat.format(inf.time));
        System.out.println(inf.message);
        System.out.println();
    }

    public Integer getPreId() {
        return prevId;
    }

    public HashMap<String, String> getFileTofilePath() {
        return fileTofilePath;
    }

    public Integer getCurrId() {
        return currentId;
    }

    public CommitInfo getInf() {
        return inf;
    }
}
