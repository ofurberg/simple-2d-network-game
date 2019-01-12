package lab3.networkgame.server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler extends Thread {

    private GameServer gameServer;
    private Socket clientSocket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private BufferedReader reader;
    private int clientID = UniqueIdentifier.getIdentifier();
    private int[] currentPosition;
    private List<Object> clientUDPInfo = new ArrayList<Object>();
    private boolean running = false;
    private boolean initClient = false;

    private final String multiSocketAddr = "239.255.255.249";
    private int multiCastPort;

    public ClientHandler(GameServer gameServer, Socket clientSocket) {
        this.gameServer = gameServer;
        this.clientSocket= clientSocket;
        multiCastPort = gameServer.getPortNumber()+1;
    }

    private int getClientID() {
        return clientID;
    }

    private int[] getCurrentPosition() {
        return currentPosition;
    }

    private List<Object> getClientUDPInfo() {
        return clientUDPInfo;
    }

    // Start one thead for reading TCP and one thread for reading UDP
    private void handleClientSocket() throws IOException {
        startUDPReader();
        startTCPReader();
    }

    // Reads TCP msg
    public void readTCPLoop() throws IOException {
        inputStream = clientSocket.getInputStream();
        outputStream = clientSocket.getOutputStream();
        if (initClient == false) {
            gameServer.getGameLogic().getGrid().placeObject(clientID);
            currentPosition = gameServer.getGameLogic().getGrid().getPositionOfObject(clientID);
            String playerStart = ";ps:" + Integer.toString(clientID) + ":" + Integer.toString(currentPosition[0]) + ":" + Integer.toString(currentPosition[1]) + ";";
            sendTCP(playerStart);
            initClient = true;
        }

        // Give the client the other players positions and tell them the clients
        List<ClientHandler> clientHandlerList = gameServer.getHandlerList();
        if (clientHandlerList.size() > 1) {
            for (ClientHandler handler : clientHandlerList) {

                int[] pos = handler.getCurrentPosition();
                int id = handler.getClientID();

                if (id != this.clientID) {
                    String msgOtherPlayersPos = ";op:" + id + ":" + pos[0] + ":" + pos[1] + ";";
                    sendTCP(msgOtherPlayersPos);
                    String msgTellOthersMyPos = ";np:" + this.clientID + ":" + this.currentPosition[0] + ":" + this.currentPosition[1] + ";";
                    handler.sendTCP(msgTellOthersMyPos);
                }
            }
        }

        // Read the input from the client
        reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            String input = line.replace(";", "");
            String[] msgArray = input.split(":");

            if (msgArray[0].equals("m")) {
                handleMove(msgArray);
            }
            else if (msgArray[0].equals("q")) {
                handleQuit(msgArray);
                System.out.println("Client with id: " + clientID + " disconnected!");
                break;
            }

        }
    }

    // Loop for reading TCP
    public void startTCPReader() {
        Thread ts = new Thread() {
            public void run() {
                try {
                    readTCPLoop();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        ts.start();
    }

    // Loop for reading UDP
    public void startUDPReader() {
        Thread us = new Thread() {
            public void run() {
                readUDPLoop();
            }
        };
        us.start();
    }

    // Read loop for udp
    public void readUDPLoop() {
        try {
            receiveUDP();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // receive UDP packet
    public void receiveUDP() throws IOException {
        running = true;
        while(running) {
            byte[] buffer = new byte[512];
            DatagramPacket datagramPacket = new DatagramPacket(buffer, 0, buffer.length);
            gameServer.getUDPSocket().receive(datagramPacket);
            String receivedMsg = new String(datagramPacket.getData());

            if ((clientUDPInfo.isEmpty())) {
                clientUDPInfo.add(0, datagramPacket.getAddress());
                clientUDPInfo.add(1, datagramPacket.getPort());
            }
            String inputUDP = receivedMsg.replace(";", "");
            String[] msgArrayUDP = inputUDP.split(":");

            if (msgArrayUDP[0].equals("us")) {
                handleNewUDP(msgArrayUDP, datagramPacket);
            }
            else if (msgArrayUDP[0].equals("m")) {
                handleMove(msgArrayUDP);
            }
            else {
                System.out.println("No if in receiveUDP worked!");
            }
        }
    }

    // send UDP packet to one client
    public void sendUDP(String msgtoSend, InetAddress address, int port) throws IOException {
        Thread send = new Thread("Send") {
            public void run() {
                DatagramPacket datagramPacket = new DatagramPacket(msgtoSend.getBytes(), msgtoSend.length(), address, port);
                try {
                    gameServer.getUDPSocket().send(datagramPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        send.start();
    }

    // send UDP packet to all clients
    public void sendToAllUDP(String msgtoSend) throws IOException {
        List<ClientHandler> clientList = gameServer.getHandlerList();
        for (ClientHandler handler : clientList) {
            List<Object> addressAndPort = handler.getClientUDPInfo();
            sendUDP(msgtoSend, (InetAddress) addressAndPort.get(0), (int) addressAndPort.get(1));
        }
    }

    // send multicast message to group
    public void sendMulticast(String msgtoSend) throws IOException {
        InetAddress multiAddr = InetAddress.getByName(multiSocketAddr);
        sendUDP(msgtoSend, multiAddr, multiCastPort);
    }

    // send message to one client
    public void sendTCP(String msg) throws IOException {
        msg = msg + "\n";
        outputStream.write(msg.getBytes());
    }
    
    // send message to all clients
    public void sendToAllTCP(String msg) throws IOException {
        List<ClientHandler> clientList = gameServer.getHandlerList();
        for (ClientHandler handler : clientList) {
            handler.sendTCP(msg);
        }
    }

    // handles new UDP connection
    private void handleNewUDP(String[] msgArray, DatagramPacket datagramPacket) {
        if (msgArray.length == 2) {

            String clientToAdd = msgArray[1];
            int cta = Integer.parseInt(clientToAdd.trim());
            if (cta == this.clientID) {
                clientUDPInfo.add(0, datagramPacket.getAddress());
                clientUDPInfo.add(1, datagramPacket.getPort());
            }
        }
    }
    // handles the moves from the player
    private void handleMove(String[] msgArray) throws IOException {
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

            // Server decides what to do depending on move request
            String serverDecision = gameServer.getGameLogic().getGrid().validMove(cID, cnpx, cnpy, copx, copy);
            switch (serverDecision) {
                case "NV":
                    String clientNoMoveMsg = ";mf:" + clientToMove + ";";
                    sendTCP(clientNoMoveMsg);
                    break;

                case "V":
                    gameServer.getGameLogic().getGrid().setPosition(cID, cnpx, cnpy);
                    this.currentPosition[0] = cnpx;
                    this.currentPosition[1] = cnpy;
                    gameServer.getGameLogic().getGrid().setPosition(-1, copx, copy);
                    System.out.println("Client with id: " + cID + " moved from " + copx + ":" + copy + " to " + cnpx + ":" + cnpy);
                    String clientMoveMsg = ";mv:" + clientToMove + ":" + clientNewPosX + ":" + clientNewPosY + ":" + clientOldPosX + ":" + clientOldPosY + ";";
                    //sendToAllTCP(clientMoveMsg);
                    //sendToAllUDP(clientMoveMsg);
                    sendMulticast(clientMoveMsg);
                    break;

                case "W":
                    String clientWinMsg = ";w:" + clientToMove + ";";
                    sendToAllTCP(clientWinMsg);
                    System.out.println("Client with id " + cID + " won!");
                    break;
            }
        }
    }

    // handles quit from client
    private void handleQuit(String[] msgArray) throws IOException {
        if (msgArray.length == 2) {
            String clientToDisconnect = msgArray[1];
            int ctr = Integer.parseInt(clientToDisconnect);
            try {
                reader.close();
                clientSocket.close();

                gameServer.getHandlerList().remove(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Remove client from the grid and tell the other clients
            int[] removeFromPos = gameServer.getGameLogic().getGrid().getPositionOfObject(ctr);
            gameServer.getGameLogic().getGrid().setPosition(-1, removeFromPos[0], removeFromPos[1]);
            String clientRemoveMsg = ";q:" + clientToDisconnect + ";";
            sendToAllTCP(clientRemoveMsg);
        }
    }

    // run method of the client handler
    @Override
    public void run() {
            try {
                handleClientSocket();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
}
