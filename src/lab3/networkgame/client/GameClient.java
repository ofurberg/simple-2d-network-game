package lab3.networkgame.client;

import lab3.networkgame.model.GameLogic;

import java.util.Observable;
import java.net.*;
import java.io.*;

public class GameClient extends Observable {

    private Socket clientSocketTCP;
    private DatagramSocket clientSocketUDP;
    private MulticastSocket multicastSocket;
    private OutputStream serverOut;
    private InputStream serverIn;
    private BufferedReader bufferedIn;
    private String host;
    private int portNumber;

    private final String multiSocketAddr = "239.255.255.249";
    private int multiSocketPort;

    private String name; // is name needed?
    private int clientID;
    private int[] currentPosition = new int[2];
    public String message = "";
    private GameLogic gameLogic;

    private boolean running = false;
    private Thread runThread;

    public GameClient(String host, int portNumber) {//, String name)  {
        this.host = host;
        this.portNumber = portNumber;
        this.name = name;
        gameLogic = new GameLogic();
        multiSocketPort = portNumber+1;

        if (!openConnection(host, portNumber)) {
            System.err.println("Connection to server failed!");
        }
        else {
            startServerReader();
            startUDPReader();
            System.out.println("Connection to server was successful!");
            sendUDPSocketInfo();
        }
    }

    // Get the game logic
    public GameLogic getGameLogic() {
        return gameLogic;
    }

    // Get clients id
    public int getID() {
        return clientID;
    }

    // Get client current position
    public int[] getCurrentPos() {
        return currentPosition;
    }

    // connect to server
    public boolean openConnection (String host, int portNumber) {
        try {
            this.clientSocketTCP = new Socket(host, portNumber);
            this.clientSocketUDP = new DatagramSocket(clientSocketTCP.getLocalPort()+1);

            this.multicastSocket = new MulticastSocket(multiSocketPort);
            InetAddress multiAddr = InetAddress.getByName(multiSocketAddr);
            multicastSocket.joinGroup(multiAddr);

            this.serverOut = clientSocketTCP.getOutputStream();
            this.serverIn = clientSocketTCP.getInputStream();
            this.bufferedIn = new BufferedReader(new InputStreamReader(serverIn));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Start new thread to read from server
    public void startServerReader() {
        Thread t = new Thread() {
          public void run() {
              readServerLoop();
          }
        };
        t.start();
    }

    public void startUDPReader() {
        Thread u = new Thread() {
            public void run() {
                readUDPLoop();
            }
        };
        u.start();
    }

    public void readUDPLoop() {
        running = true;
        while(running) {
            try {
                byte[] buffer = new byte[512];
                DatagramPacket datagramPacket = new DatagramPacket(buffer, 0, buffer.length);
                multicastSocket.receive(datagramPacket);
                String receivedMsg = new String(datagramPacket.getData());
                String inputUDP = receivedMsg.replace(";", "");
                String[] msgArrayUDP = inputUDP.split(":");

                if (msgArrayUDP[0].equals("mv")) {
                    receiveMove(msgArrayUDP);
                }
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Send move to server using UDP
    public void sendMoveUDP(int moveX, int moveY) {

            String cmd = ";m:";
            String strclientID = Integer.toString(this.clientID);

            String strmoveX = Integer.toString(this.currentPosition[0] + moveX);
            String strmoveY = Integer.toString(this.currentPosition[1] + moveY);

            String curPosX = Integer.toString(this.currentPosition[0]);
            String curPosY = Integer.toString(this.currentPosition[1]);

            String moveMsg = cmd + strclientID + ":" + strmoveX + ":" + strmoveY + ":" + curPosX + ":" + curPosY + ";";
            sendUDP(moveMsg);
    }

    public void sendUDP(String msgtoSend) {
        Thread sendToServer = new Thread("Send") {
            public void run() {
                try {
                    InetAddress inetAddr = InetAddress.getByName(host);
                    DatagramPacket datagramPacket = new DatagramPacket(msgtoSend.getBytes(), msgtoSend.length(), inetAddr, portNumber);
                    clientSocketUDP.send(datagramPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        sendToServer.start();
    }

    // SKICKA MEDDELANDE SÅ SERVERN VET VILKEN UDP SOCKET DEN SKA SKICKA TILL
    public void sendUDPSocketInfo() {
        String udpSetUp = ";us:" + this.clientID + ";";
        sendUDP(udpSetUp);
    }

    // Loop for reading from server
    public void readServerLoop() {
        try {
            String line;
            while ((line = this.bufferedIn.readLine()) != null) {
                String input = line.replace(";", "");
                String[] msgArray = input.split(":");

                if (msgArray[0].equals("ps")) {
                    getStartPosition(msgArray);
                    sendUDPSocketInfo();
                }
                else if (msgArray[0].equals("np")) {
                    initNewPlayer(msgArray);
                }
                else if (msgArray[0].equals("mv")) {
                    receiveMove(msgArray);
                }
                else if(msgArray[0].equals("w")) {
                        playerWin(msgArray);
                }
                else if(msgArray[0].equals("mf")) {
                        moveFailed(msgArray);
                }
                else if(msgArray[0].equals("op")) {
                    initOtherPlayers(msgArray);
                }
                else if(msgArray[0].equals("q")) {
                    playerQuit(msgArray);
                }
                else {
                    System.out.println(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // player quit
    public void playerQuit(String[] msgArray) {
        if (msgArray.length == 2) {

            String clientToRemove = msgArray[1];
            int ctr = Integer.parseInt(clientToRemove);

            int[] removeFromPos = getGameLogic().getGrid().getPositionOfObject(ctr);
            getGameLogic().getGrid().setPosition(-1, removeFromPos[0], removeFromPos[1]);
            message = "Client with id " + clientToRemove + " DISCONNECTED!";
            setChanged();
            notifyObservers();
        }
    }

    // Init all other players
    public void initOtherPlayers(String[] msgArray) {
        if (msgArray.length == 4) {

            String playerID = msgArray[1];
            int pID = Integer.parseInt(playerID);

            String playerStartPosX = msgArray[2];
            String playerStartPosY = msgArray[3];
            int cspx = Integer.parseInt(playerStartPosX);
            int cspy = Integer.parseInt(playerStartPosY);

            getGameLogic().getGrid().setPosition(pID, cspx, cspy);
        }
    }

    // Init new player
    public void initNewPlayer(String[] msgArray) {
        if (msgArray.length == 4) {

            String playerID = msgArray[1];
            int pID = Integer.parseInt(playerID);

            String playerStartPosX = msgArray[2];
            String playerStartPosY = msgArray[3];
            int cspx = Integer.parseInt(playerStartPosX);
            int cspy = Integer.parseInt(playerStartPosY);

            getGameLogic().getGrid().setPosition(pID, cspx, cspy);
            message = "Player with client id " + pID + " joined the game!";
        }
        setChanged();
        notifyObservers();
    }

    // Give ID and start position to Client
    public void getStartPosition(String[] msgArray) {
        if (msgArray.length == 4) {

            String clientID = msgArray[1]; // if of client
            int cID = Integer.parseInt(clientID);
            this.clientID = cID;

            String clientStartPosX = msgArray[2];
            String clientStartPosY = msgArray[3];
            int cspx = Integer.parseInt(clientStartPosX);
            int cspy = Integer.parseInt(clientStartPosY);

            this.currentPosition[0] = cspx;
            this.currentPosition[1] = cspy;

            getGameLogic().getGrid().setPosition(this.clientID, currentPosition[0], currentPosition[1]);
            System.out.println("Your start position is: (" + currentPosition[0] + ", " + currentPosition[1] + ")");
        }
    }

    // Send move to server
    public void sendMoveTCP(int moveX, int moveY) {

        String cmd = ";m:";
        String strclientID = Integer.toString(this.clientID);

        String strmoveX = Integer.toString(currentPosition[0] + moveX);
        String strmoveY = Integer.toString(currentPosition[1] + moveY);

        String curPosX = Integer.toString(currentPosition[0]);
        String curPosY = Integer.toString(currentPosition[1]);

        String moveMsg = cmd + strclientID + ":" + strmoveX + ":" + strmoveY + ":" + curPosX + ":" + curPosY + ";" + "\n";
        try {
            serverOut.write(moveMsg.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // If move failed
    public void moveFailed(String[] msgArray) {
        if (msgArray.length == 2) {
            message = "Move not allowed!";
            message = "";
        }
        setChanged();
        notifyObservers();
    }

    // if player wins
    public void playerWin(String[] msgArray) {
        if (msgArray.length == 2) {
            String clientThatWon = msgArray[1];
            int clientWin = Integer.parseInt(clientThatWon);
            if (clientWin == this.clientID) {
                message = "You WON!";
            }
            else {
                message = "Client with id " + clientWin + " WON!";
            }
        }
        setChanged();
        notifyObservers();
    }

    // Receives moves from server
    public void receiveMove(String[] msgArray) {
        if (msgArray.length == 6) {
            String clientToMove = msgArray[1];
            int cID = Integer.parseInt(clientToMove);

            String clientNewPosX = msgArray[2];
            String clientNewPosY = msgArray[3];
            int cnpx = Integer.parseInt(clientNewPosX);
            int cnpy = Integer.parseInt(clientNewPosY);

            String clientOldPosX = msgArray[4];
            String clientOldPosY = msgArray[5];
            int copx = Integer.parseInt(clientOldPosX);
            int copy = Integer.parseInt(clientOldPosY.trim());

            synchronized (this) {
                getGameLogic().getGrid().setPosition(cID, cnpx, cnpy);
                getGameLogic().getGrid().setPosition(-1, copx, copy);

                if (cID == this.clientID) {
                    currentPosition[0] = cnpx;
                    currentPosition[1] = cnpy;

                    System.out.println("Client with id: " + cID + " moved from " + copx + ":" + copy + " to " + cnpx + ":" + cnpy);
                    message = "Move OK";
                }
                setChanged();
                notifyObservers();
            }
        }
    }

    // Disconnect from server
    public void disconnect()  {
        String quitMsg = ";q:" + Integer.toString(this.clientID) + ";\n";

        try {
            InetAddress multiLeave = InetAddress.getByName(multiSocketAddr);
            this.multicastSocket.leaveGroup(multiLeave);
            this.serverOut.write(quitMsg.getBytes());
            this.serverOut.flush();
            this.bufferedIn.close();
            this.clientSocketTCP.close();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
