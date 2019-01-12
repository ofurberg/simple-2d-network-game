package lab3.networkgame.client;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public class ServiceRequester implements Runnable {

    public String SERVICE_NAME = "JavaGameServer";
    public String multicastDiscoveryAddr = "239.255.255.250";
    public int multicastDiscoveryPort = 1900;
    public String requestString = "SERVICE QUERY " + SERVICE_NAME;
    private Thread requestThread;
    private boolean running = false;

    public ArrayList<ServerListItem> serverList = new ArrayList<>();

    private DatagramSocket requestSocket;

    public ServiceRequester() {
        try {
            requestSocket = new DatagramSocket();
            DatagramPacket requestPacket = new DatagramPacket(requestString.getBytes(), requestString.length(), InetAddress.getByName(multicastDiscoveryAddr), multicastDiscoveryPort);
            requestSocket.send(requestPacket);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        requestThread = new Thread(this, "Request Thread");
        requestThread.start();
    }

    public ArrayList<ServerListItem> getServerList() {
        return serverList;
    }

    public void run() {
        running = true;
        while (running) {
            byte[] buffer = new byte[512];
            DatagramPacket receivedPacket = new DatagramPacket(buffer, 0, buffer.length);
            try {
                requestSocket.receive(receivedPacket);
                String receivedMsg = new String(receivedPacket.getData());
                String requestedService = receivedMsg.trim();

                if (requestedService.startsWith("SERVICE REPLY")) {
                    String listItem[] = requestedService.split(" ");
                    serverList.add(new ServerListItem(listItem[3], listItem[4], listItem[5]));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
