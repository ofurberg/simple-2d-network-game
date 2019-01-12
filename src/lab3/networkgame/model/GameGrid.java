package lab3.networkgame.model;

import java.util.Observable;
import java.util.Random;

/**
 * The 2-d representation of the game grid
 */
public class GameGrid extends Observable {

    private int[][] gameGrid;
    private final int GRID_SIZE = 20;
    private int GOLD = 1;
    private int EMPTY = -1;

    public GameGrid(int size) {
        gameGrid = new int[size][size];
        initGameGrid();
    }

    // Get gamegrid
    public int[][] getGameGrid() {
        return gameGrid;
    }

    // Returns the size of the grid
    public int getSizeOfGrid() {
        return gameGrid.length;
    }

    // Get a location on the grid
    public int getLocation(int x, int y) {
        return gameGrid[x][y];
    }

    // Return coordinates of a position of a object
    public int[] getPositionOfObject(int obj) {
        int[] pos = new int[2];
        for (int x = 0; x < gameGrid.length; x++) {
            for (int y = 0; y < gameGrid[x].length; y++) {
                if (gameGrid[x][y] == obj) {
                    pos[0] = x;
                    pos[1] = y;
                }
            }
        }
        return pos;
    }

    // Initialize the grid
    public void initGameGrid() {
        // Set the entire grid ot empty
        for (int x = 0; x < gameGrid.length; x++) {
            for (int y = 0; y < gameGrid[x].length; y++) {
                if (gameGrid[x][y] == 0) {
                    gameGrid[x][y] = EMPTY;
                }
            }
        }
    }

    // Set position on the grid of a GameSquare
    public void setPosition(int obj, int x, int y) {
        gameGrid[x][y] = obj;
    }

    // Randomizes a position on the board
    public int[] randomPosition() {
        Random r = new Random();
        int low = 0;
        int high = getSizeOfGrid();
        int x = r.nextInt(high - low) + low;
        int y = r.nextInt(high - low) + low;
        int[] randomPos = new int[2];
        randomPos[0] = x;
        randomPos[1] = y;

        return randomPos;
    }

    // Place an object on the game grid
    public void placeObject(int obj) {
        int[] randomPos = randomPosition();
        int location = getLocation(randomPos[0], randomPos[1]);
        if (location == EMPTY) {
            setPosition(obj, randomPos[0], randomPos[1]);
        } else {
            placeObject(obj);
        }
    }

    // Enter move on the grid
    public String validMove(int player, int moveX, int moveY, int oldX, int oldY) {

        // Check for moves outside grid
        if (!(0 <= moveX) || !(moveX < getSizeOfGrid())) {
            System.out.println("Trying to move outside of grid x!");
            return "NV";
        } else if (!(0 <= moveY) || !(moveY < getSizeOfGrid())) {
            System.out.println("Trying to move outside of grid y!");
            return "NV";
        }
        else if (getLocation(moveX, moveY) == GOLD) {
            return "W";
        }
        else if (getLocation(moveX, moveY) == EMPTY) {
            return "V";
        }
        else if (getLocation(moveX, moveY) != EMPTY) {
            return "NV";
        }
        return "";
    }
}
