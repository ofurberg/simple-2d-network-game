package lab3.networkgame.client;

import lab3.networkgame.view.ConnectGUI;

public class ClientMain {

    public static void main(String[] args) {
        ServiceRequester sr = new ServiceRequester();
        ConnectGUI cg = new ConnectGUI(sr);
    }
}
