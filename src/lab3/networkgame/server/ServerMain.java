package lab3.networkgame.server;

public class ServerMain {

    private int portNumber;
    private String name;

    public ServerMain(int portNumber, String name) {
        this.portNumber = portNumber;
        this.name = name;
    }

    // 8192 8194 8196
    public static void main(String[] args) {
        new GameServer(8194, "oskar2");
    }
}
