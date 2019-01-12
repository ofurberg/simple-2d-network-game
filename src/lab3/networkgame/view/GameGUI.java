package lab3.networkgame.view;

import lab3.networkgame.client.GameClient;
import lab3.networkgame.model.GameGrid;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Observable;
import java.util.Observer;

public class GameGUI implements Observer {

    private JFrame gameGUI;
    private GamePanel gameGridPanel;
    private GameClient gameClient;
    private JButton disconnectButton;
    private JLabel clientMessageLabel;
    private JLabel clientsID;

    public GameGUI(GameClient gc) {

        this.gameClient = gc;
        gameClient.addObserver(this);
        gameClient.getGameLogic().addObserver(this);

        gameGUI = new JFrame("Hidden Gold");
        gameGUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameGUI.setLocation(500, 200);
        gameGUI.setFocusable(true);

        JPanel gridPanel = new JPanel();
        JPanel buttonPanel = new JPanel(new GridLayout(3,3));

        // Functions of disconnectButton
        disconnectButton = new JButton("Disconnect");
        disconnectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                gameClient.disconnect();
            }
        });

        buttonPanel.add(disconnectButton);

        gameGridPanel = new GamePanel(gameClient.getGameLogic().getGrid());//, gameClient.getID());

        gameGUI.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == 87) {
                    //gameClient.sendUDP("Hello");
                    //gameClient.sendMoveTCP(0, -1);
                    gameClient.sendMoveUDP(0, -1);
                }
                if (e.getKeyCode() == 83) {
                    //gameClient.sendMoveTCP(0, 1);
                    gameClient.sendMoveUDP(0, 1);
                }
                if (e.getKeyCode() == 68) {
                    //gameClient.sendMoveTCP(1, 0);
                    gameClient.sendMoveUDP(1, 0);
                }
                if (e.getKeyCode() == 65) {
                    //gameClient.sendMoveTCP(-1, 0);
                    gameClient.sendMoveUDP(-1, 0);
                }
            }
        });

        clientsID = new JLabel("Clients ID: " + gameClient.getID());
        buttonPanel.add(clientsID);
        clientMessageLabel = new JLabel("Make a move!");
        buttonPanel.add(clientMessageLabel);

        gridPanel.add(gameGridPanel);

        gameGUI.add(gridPanel, BorderLayout.NORTH);
        gameGUI.add(buttonPanel, BorderLayout.SOUTH);
        gameGUI.setResizable(false);
        gameGUI.pack();
        gameGUI.setVisible(true);
    }

    @Override
    public void update(Observable arg0, Object arg1) {
        if(arg0 == gameClient) {
            clientMessageLabel.setText(gameClient.message);
            clientsID.setText("Clients ID: " + Integer.toString(gameClient.getID()));
        }
    }

    public class GamePanel extends JPanel implements Observer {

        private final int UNIT_SIZE = 30;
        private GameGrid gameGrid;
        private int clientID;
        private int[] gridPosition;

        public GamePanel(GameGrid gameGrid) { //, int clientID) {

            this.gameGrid = gameGrid;
            gameGrid.addObserver(this);
            gameClient.addObserver(this);
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
                    //g.setColor(new Color(102,51,0));
                    g.setColor(Color.BLACK);
                    g.drawRect(x*UNIT_SIZE, y*UNIT_SIZE, UNIT_SIZE, UNIT_SIZE);
                    int gameSquare = gameGrid.getLocation(x, y);

                    // PAINT THE PLAYERS DOT
                    if (gameSquare == gameClient.getID()) {
                        g.setColor(Color.BLUE);
                        g.fillOval(x*UNIT_SIZE + 2, y*UNIT_SIZE + 2, UNIT_SIZE - 5, UNIT_SIZE - 5);
                    }
                    // PAINT THE EMPTY SQUARES
                    if (gameSquare == -1) {
                        g.setColor(new Color(102,51,0));
                        g.fillOval(x*UNIT_SIZE + 2, y*UNIT_SIZE + 2, UNIT_SIZE - 5, UNIT_SIZE - 5);
                    }
                    // PAINT THE GOLD
                    if (gameSquare == 1) {
                        g.setColor(Color.YELLOW);
                        g.fillOval(x*UNIT_SIZE + 2, y*UNIT_SIZE + 2, UNIT_SIZE - 5, UNIT_SIZE - 5);
                    }
                    // PAINT THE OPPONENTS DOTS
                    if (gameSquare != gameClient.getID() && gameSquare != -1 && gameSquare != 1){
                        g.setColor(Color.RED);
                        g.fillOval(x*UNIT_SIZE + 2, y*UNIT_SIZE + 2, UNIT_SIZE - 5, UNIT_SIZE - 5);
                    }
                }
            }
        }
    }
}
