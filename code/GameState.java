

import java.awt.Point;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public class GameState implements Serializable {
	private static final long serialVersionUID = 1L;
	public String TimeStamp;
	
	public int N;
	public int K;
	public String gameStartTime;
	
	public String primaryServerName;
	public String backupServerName;
	
	public Set<Point> treasures;
	
	public Map<String, Point> playersPosition;
	public Map<String, Integer> playersScore;
}
