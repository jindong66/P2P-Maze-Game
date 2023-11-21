

import javax.swing.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainGUI {
    private final Chessboard chessboard;
    private final int gridSize;
    public final JFrame mainFrame;
    private final RankPanel rankPanel;
    private final JLabel timeLabel;
    private final JLabel primaryServerLabel;
    private final JLabel backupServerLabel;
    
    public MainGUI(String name, GameState gameState){
        this.gridSize = gameState.N;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double screen_width = screenSize.getWidth();
        double screen_height = screenSize.getHeight();

        mainFrame = new JFrame(name + " is playing");
        mainFrame.setBounds((int)(screen_width-Constants.FRAME_WIDTH)/2,(int)(screen_height-Constants.FRAME_HEIGHT)/2,
                Constants.FRAME_WIDTH,Constants.FRAME_HEIGHT); //getContentPane().setPreferredSize(new Dimension(Constants.FRAME_WIDTH,Constants.FRAME_HEIGHT));

        mainFrame.setLayout(new BorderLayout());
        chessboard = new Chessboard(gridSize, Constants.FRAME_HEIGHT-50,Constants.FRAME_HEIGHT-50);
        chessboard.setPreferredSize(new Dimension(Constants.FRAME_HEIGHT-5,Constants.FRAME_HEIGHT-10));
        mainFrame.add(chessboard,BorderLayout.EAST);
        JPanel leftPanel = new JPanel();
        leftPanel.setPreferredSize(new Dimension(Constants.FRAME_WIDTH-Constants.FRAME_HEIGHT-20,Constants.FRAME_HEIGHT-10));
        leftPanel.setLayout(new FlowLayout());
        //排行榜，primary server和backup server，start-time
        //currently not leader board, just show all players and their scores
        List<PlayerScore> playerScores = new ArrayList<>();
        for(String key : gameState.playersScore.keySet()){
        	double value = (double)gameState.playersScore.get(key);
            PlayerScore playerScore1 = new PlayerScore(key, value);
            playerScores.add(playerScore1);
        }
        JPanel rankRegion = new JPanel();
        rankRegion.setPreferredSize(new Dimension(Constants.FRAME_WIDTH-Constants.FRAME_HEIGHT-20,(int)((Constants.FRAME_HEIGHT-10)/2)));

        JLabel jLabel = new JLabel("Players");
        rankRegion.add(jLabel,BorderLayout.NORTH);

        rankPanel = new RankPanel(playerScores);
        rankPanel.setPreferredSize(new Dimension(Constants.FRAME_WIDTH-Constants.FRAME_HEIGHT-20,(int)(4*(Constants.FRAME_HEIGHT-10)/2)/5));
        rankRegion.add(rankPanel,BorderLayout.SOUTH);

        leftPanel.add(rankRegion,BorderLayout.NORTH);

        JPanel otherPanel = new JPanel();
        otherPanel.setPreferredSize(new Dimension(Constants.FRAME_WIDTH-Constants.FRAME_HEIGHT-20,(int)(4*(Constants.FRAME_HEIGHT-10)/2)/5));
        otherPanel.setLayout(new GridLayout(5,1));
        timeLabel = new JLabel();
        timeLabel.setText("Start Time: " + gameState.gameStartTime);
        primaryServerLabel = new JLabel();
        backupServerLabel = new JLabel();
        primaryServerLabel.setText("primary Server: " + gameState.primaryServerName);
        backupServerLabel.setText("backup Server: " + gameState.backupServerName);
        otherPanel.add(timeLabel);
        otherPanel.add(primaryServerLabel);
        otherPanel.add( backupServerLabel);
        leftPanel.add(otherPanel, BorderLayout.SOUTH);

        mainFrame.add(leftPanel, BorderLayout.WEST);
        mainFrame.setVisible(true);
        
        System.out.println(name + " show the GUI.");
    }
    
    public void refresh(GameState gameState) {
    	System.out.println("refresh the GUI.");
    	String[][] grids = new String[gameState.N][gameState.N];
    	for (int i=0; i<gameState.N; i++) {
    		for (int j=0; j<gameState.N; j++) {
    			Point point = new Point(i, j);
    			if (gameState.treasures.contains(point)) {
    				grids[i][j] = "TREASURE895";
    			} else {
    				grids[i][j] = "EMPTY";
    			}
    		}
    	}
    	for (String key : gameState.playersPosition.keySet()) {
    		Point value = gameState.playersPosition.get(key);
    		grids[value.x][value.y] = key;
    	}
    	setGrids(grids);
    	
    	List<PlayerScore> playerScores = new ArrayList<>();
        for(String key : gameState.playersScore.keySet()){
        	double value = (double)gameState.playersScore.get(key);
            PlayerScore playerScore1 = new PlayerScore(key, value);
            playerScores.add(playerScore1);
        }
        setRankPlayer(playerScores);
        
        setPrimaryServer(gameState.primaryServerName);
        setBackupServer(gameState.backupServerName);
    }
    private void setGrids(String[][] grids){
        chessboard.setPiece(grids);
    }
    private void setRankPlayer(List<PlayerScore> playerScores){
        rankPanel.setPlayers(playerScores);
    }
    private void setPrimaryServer(String primaryServer){
        primaryServerLabel.setText("primary Server: "+primaryServer);
    }
    private void setBackupServer(String backupServer){
        backupServerLabel.setText("backup Server: "+backupServer);
    }

}
