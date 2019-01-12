package lab3.networkgame.view;

import lab3.networkgame.model.GameGrid;
import lab3.networkgame.model.GameSquare;

import javax.swing.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;

/*public class GamePanel extends JPanel implements Observer {

    private final int UNIT_SIZE = 30;
    private GameGrid gameGrid;
    private int[] gridPosition;
    private int clientID;


    public GamePanel(GameGrid gameGrid, int clientID) {

        this.gameGrid = gameGrid;
        this.clientID = clientID;
        gameGrid.addObserver(this);
        Dimension dimension = new Dimension(gameGrid.getSizeOfGrid()*UNIT_SIZE+1, gameGrid.getSizeOfGrid()*UNIT_SIZE+1);
        this.setMinimumSize(dimension);
        this.setPreferredSize(dimension);
        this.setMaximumSize(dimension);
        //102- 51- 0 brown
        this.setBackground(new Color(102,51,0));
    }

    public int[] getPosition(int x, int y) {
        gridPosition = new int[] {x / UNIT_SIZE, y / UNIT_SIZE};
        return gridPosition;
    }

    public void update(Observable arg0, Object arg1) {
        this.repaint();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draws the board and all the components
        for (int x = 0; x < gameGrid.getSizeOfGrid(); x++) {
            for (int y = 0; y < gameGrid.getSizeOfGrid(); y++) {
                g.setColor(Color.black);
                g.drawRect(x*UNIT_SIZE, y*UNIT_SIZE, UNIT_SIZE, UNIT_SIZE);
                //GameSquare gameSquare = gameGrid.getLocation(x, y);
                int gameSquare = gameGrid.getLocation(x, y);
                //System.out.println(gameSquare);

                if (gameSquare == clientID) {
                    g.setColor(Color.BLUE);
                    g.fillOval(x*UNIT_SIZE + 2, y*UNIT_SIZE + 2, UNIT_SIZE - 5, UNIT_SIZE - 5);
                }
                // PAINT THE EMPTY SQUARES
                if (gameSquare == 0) {
                    g.setColor(new Color(102,51,0));
                    g.fillOval(x*UNIT_SIZE + 2, y*UNIT_SIZE + 2, UNIT_SIZE - 5, UNIT_SIZE - 5);
                }
                // PAINT THE GOLD
                if (gameSquare == 1) {
                    g.setColor(Color.YELLOW);
                    g.fillOval(x*UNIT_SIZE + 2, y*UNIT_SIZE + 2, UNIT_SIZE - 5, UNIT_SIZE - 5);
                }
                if (gameSquare != clientID && gameSquare != 0 && gameSquare != 1){
                    g.setColor(Color.RED);
                    g.fillOval(x*UNIT_SIZE + 2, y*UNIT_SIZE + 2, UNIT_SIZE - 5, UNIT_SIZE - 5);
                }
            }
        }
    }
}*/
