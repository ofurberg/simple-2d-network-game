package lab3.networkgame.server;

import lab3.networkgame.model.GameGrid;
import lab3.networkgame.model.GameLogic;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class GameServer implements Runnable{

    private ServerSocket serverSocketTCP = null;
    private DatagramSocket serverSocketUDP = null;
    private Socket clientSocket;
    private int portNumber;
    private String name;
    private Thread runThread;
    private boolean running = false;

    private GameLogic game;
    private GameGrid gameGrid;

    private ArrayList<ClientHandler> clientHandlerList = new ArrayList<>();

    public GameServer(int port, String name) {

        // Set up the game grid on the server and place the gold
        game = new GameLogic();
        game.getGrid().placeObject(1);
        int[] goldPosition = game.getGrid().getPositionOfObject(1);
        System.out.println("Coordinates of gold: (" + goldPosition[0] + ", " + goldPosition[1] + ")");

        this.name = name;
        // Create the ServerSocket
        this.portNumber = port;
        try {
            serverSocketTCP = new ServerSocket();
            serverSocketTCP.bind(new InetSocketAddress(InetAddress.getLocalHost(), portNumber));
            serverSocketUDP = new DatagramSocket(portNumber);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Start the Service Responder
        ServiceResponder serviceResponder = new ServiceResponder(name, serverSocketTCP.getInetAddress(), serverSocketTCP.getLocalPort());
        serviceResponder.start();

        // Start the thread running the server
        runThread = new Thread(this, "GameServer");
        runThread.start();
    }

    // Gets the list of client handlers
    public List<ClientHandler> getHandlerList() {
        return clientHandlerList;
    }

    // Gets the port number
    public int getPortNumber() {
        return portNumber;
    }

    // Returns the game grid
    public GameGrid getGameGrid() {
        return gameGrid;
    }

    public GameLogic getGameLogic() {
        return game;
    }

    // Get the udp socket
    public DatagramSocket getUDPSocket() {
        return serverSocketUDP;
    }

    // Run method for the server
    @Override
    public void run() {
        running = true;
        System.out.println("Server with name: " + name + ",  started on: " + serverSocketTCP.getLocalSocketAddress()); //+ ", on port " + portNumber);

        while(running) {
            try {
                clientSocket = serverSocketTCP.accept();
                System.out.println("Client connected from: " + clientSocket.getRemoteSocketAddress().toString());
                ClientHandler clientHandler = new ClientHandler(this, clientSocket);
                clientHandlerList.add(clientHandler);
                clientHandler.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
