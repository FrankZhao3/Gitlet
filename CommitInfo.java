import java.io.Serializable;
import java.util.Date;

public class CommitInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	public Date time;
	public Integer ID;
	public String message;

	public CommitInfo(Date date, Integer id, String TheMessage) {
		time = date;
		ID = id;
		message = TheMessage;
	}
}
