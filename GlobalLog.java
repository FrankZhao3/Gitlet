import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class GlobalLog implements Serializable {
    private static final long serialVersionUID = 1L;
    ArrayList<CommitInfo> gLog;

    public GlobalLog() {
        gLog = new ArrayList<CommitInfo>();
    }

    public void add(Integer Id, String message) {
        Date date = new Date();
        CommitInfo cInf = new CommitInfo(date, Id, message);
        gLog.add(cInf);
    }

    public ArrayList<Integer> find(String message) {
        ArrayList<Integer> idArray = new ArrayList<Integer>();
        for (CommitInfo s : gLog) {
            if (message.equals(s.message)) {
                idArray.add(s.ID);
            }
        }
        return idArray;
    }

    public void output() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Stack<CommitInfo> temp = new Stack<CommitInfo>();
        for (CommitInfo info : gLog) {
            temp.push(info);
        }
        for (CommitInfo info : temp) {
            System.out.println("====");
            System.out.println("Commit " + info.ID);
            System.out.println(dateFormat.format(info.time));
            System.out.println(info.message);
            System.out.println();
        }
    }
}
