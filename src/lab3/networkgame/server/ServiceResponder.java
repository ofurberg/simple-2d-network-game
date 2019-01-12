package lab3.networkgame.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ServiceResponder extends Thread{

    private String serverName;
    private InetAddress serverAddress;
    private int serverPort;

    private MulticastSocket responseSocket;
    public String SERVICE_NAME = "JavaGameServer";
    public String multicastDiscoveryAddr = "239.255.255.250";
    public int multicastDiscoveryPort = 1900;

    private boolean running = false;

    public ServiceResponder(String serverName, InetAddress serverAddress, int serverPort) {

        this.serverName = serverName;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        try {
            responseSocket = new MulticastSocket(multicastDiscoveryPort);
            responseSocket.joinGroup(InetAddress.getByName(multicastDiscoveryAddr));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        running = true;
        while(running) {
            byte[] buffer = new byte[512];
            DatagramPacket receivedPacket = new DatagramPacket(buffer, 0, buffer.length);
            try {
                responseSocket.receive(receivedPacket);
                String receivedMsg = new String(receivedPacket.getData());
                String requestedService = receivedMsg.trim();

                if(requestedService.startsWith("SERVICE QUERY")) {
                    String responseMsg = "SERVICE REPLY " + SERVICE_NAME + " " + serverName + " " + serverAddress.getHostAddress() + " " + serverPort;
                    DatagramPacket responsePacket = new DatagramPacket(responseMsg.getBytes(), responseMsg.length(), receivedPacket.getAddress(), receivedPacket.getPort());
                    responseSocket.send(responsePacket);
                    // Sleep 5 sec between send so it doesn't get spammed
                    Thread.sleep(5000);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
