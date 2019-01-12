package lab3.networkgame.model;

import java.util.Observable;
import java.util.Observer;

public class GameLogic extends Observable implements Observer {

    private final int GRID_SIZE = 20;
    private GameGrid gameGrid;

    public GameLogic() {
        gameGrid = new GameGrid(GRID_SIZE);
    }

    // Returns the game grid
    public GameGrid getGrid() {
        return gameGrid;
    }

    @Override
    public void update(Observable o, Object arg) {

    }
}
