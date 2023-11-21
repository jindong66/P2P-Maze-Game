

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class RankPanel extends JScrollPane {
    private static final long serialVersionUID = 1L;
	JPanel contentPanel;
    List<PlayerScore> playerScores;
    public RankPanel(List<PlayerScore> playerScores2){
        this.playerScores = playerScores2;
        contentPanel = new JPanel(new GridLayout(playerScores2.size(), 1, 5, 5));
        contentPanel.setBackground(Color.WHITE);
        setPlayers(playerScores2);
        this.setViewportView(contentPanel);
        this.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        this.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

    }
    public void setPlayers(List<PlayerScore> playerScores) {
        contentPanel.removeAll(); // 移除所有现有的玩家
        contentPanel.setLayout(new GridLayout(playerScores.size(), 1, 5, 5)); // 更新布局管理器

        for (PlayerScore playerScore : playerScores) {
            JPanel playerPanel = new JPanel(new BorderLayout());
            JLabel nameLabel = new JLabel(playerScore.getPlayerName());
            JLabel scoreLabel = new JLabel(String.valueOf(playerScore.getScore()));

            playerPanel.add(nameLabel, BorderLayout.WEST);
            playerPanel.add(scoreLabel, BorderLayout.EAST);
            contentPanel.add(playerPanel);
        }

        contentPanel.revalidate(); // 重新验证组件
        contentPanel.repaint(); // 重新绘制组件
    }

}
