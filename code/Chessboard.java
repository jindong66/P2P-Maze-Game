

import javax.swing.*;
import java.awt.*;

public class Chessboard extends JPanel {

    private static final long serialVersionUID = 1L;
	private final int gridSize;
    private final int height;
    private final int width;

    private final String[][] pieces;

    public Chessboard(int gridSize, int height, int width) {
        this.gridSize = gridSize;
        this.height = height;
        this.width = width;
        this.pieces = new String[gridSize][gridSize];
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                this.pieces[i][j] = "Empty";  // Make sure to assign a non-null value
            }
        }

    }
    //每一次棋盘变化的时候调用
    public void setPiece(String[][] grids){
        for(int i=0;i<gridSize;i++){
            for(int j=0;j<gridSize;j++){
                pieces[i][j] = grids[i][j];
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int cellWidth = width / gridSize;
        int cellHeight = height / gridSize;

        g.setColor(Color.BLACK);
        for (int row = 0; row <= gridSize; row++) {
            g.drawLine(0, row * cellHeight,gridSize*cellWidth, row * cellHeight);
        }
        for (int col = 0; col <= gridSize; col++) {
            g.drawLine(col * cellWidth, 0, col * cellWidth, gridSize*cellHeight);
        }

        // Draw pieces
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                if(pieces[row][col].equals("EMPTY")) continue;
                Color color = null;
                int x = col * cellWidth;
                int y = row * cellHeight;
                int radius = Math.min(cellWidth, cellHeight) / 2;
                if(pieces[row][col].equals("TREASURE895")){
                    g.setFont(new Font("Arial", Font.BOLD, radius));
                    // 设置字符颜色
                    g.setColor(Color.blue);

                    FontMetrics metrics = g.getFontMetrics();
                    int stringWidth = metrics.stringWidth("*");
                    int stringHeight = metrics.getHeight();
                    int xx = (cellWidth - stringWidth) / 2;
                    int yy = (cellHeight - stringHeight) / 2 + metrics.getAscent(); // add ascent to baseline y position

                    // 画字符
                    g.drawString(Character.toString('*'), xx + x, yy + y);
                }else{
                    g.setFont(new Font("Arial", Font.BOLD, radius));
                    // 设置字符颜色
                    g.setColor(Color.blue);

                    FontMetrics metrics = g.getFontMetrics();
                    int stringWidth = metrics.stringWidth(pieces[row][col]);
                    int stringHeight = metrics.getHeight();
                    int xx = (cellWidth - stringWidth) / 2;
                    int yy = (cellHeight - stringHeight) / 2 + metrics.getAscent(); // add ascent to baseline y position

                    // 画字符
                    g.drawString(pieces[row][col], xx + x, yy + y);
                }
            }
        }
    }
}
