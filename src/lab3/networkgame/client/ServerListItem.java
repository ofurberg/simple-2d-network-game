package lab3.networkgame.client;

public class ServerListItem {

    private String serverName;
    private String serverAddress;
    private String serverPort;

    public ServerListItem(String sn, String sa, String sp) {

        this.serverName = sn;
        this.serverAddress = sa;
        this.serverPort = sp;
    }

    public String getServerName() {
        return serverName;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public String getServerPort() {
        return serverPort;
    }
}
