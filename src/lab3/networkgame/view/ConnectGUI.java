package lab3.networkgame.view;

import lab3.networkgame.client.GameClient;
import lab3.networkgame.client.ServerListItem;
import lab3.networkgame.client.ServiceRequester;
import org.omg.CORBA.ServerRequest;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class ConnectGUI  {

    private JTextField serverTextField;
    private JTextField portTextField;
    private JTextField nameTextField;
    private JLabel serverLabel;
    private JLabel portLabel;
    private JLabel nameLabel;
    private JButton connectButton;

    private ServiceRequester serviceRequester;
    ArrayList<ServerListItem> serverList;

    private JFrame connectGUI;

    public ConnectGUI(ServiceRequester serviceRequester) {

        this.serviceRequester = serviceRequester;
        ArrayList<ServerListItem> serverList = serviceRequester.getServerList();
        connectGUI = new JFrame("Connect to a Server");
        connectGUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        connectGUI.setLocation(600, 400);

        serverTextField = new JTextField(15);
        portTextField = new JTextField(5);
        nameTextField = new JTextField(10);

        Container contentPane = connectGUI.getContentPane();
        SpringLayout layout = new SpringLayout();
        contentPane.setLayout(layout);
        serverLabel = new JLabel("Server IP address:");
        portLabel = new JLabel("Server port:");
        nameLabel = new JLabel("Name:");
        connectButton = new JButton("Connect");


        // Server ip fields and labels
        contentPane.add(serverLabel);
        layout.putConstraint("West", serverLabel, 5, "West", contentPane);
        layout.putConstraint("North", serverLabel, 5, "North", contentPane);
        contentPane.add(serverTextField);
        layout.putConstraint("West", serverTextField, 5, "East", serverLabel);
        layout.putConstraint("North", serverTextField, 5, "North", contentPane);

        // Server port fields and labels
        contentPane.add(portLabel);
        layout.putConstraint("West", portLabel, 5, "West", contentPane);
        layout.putConstraint("North", portLabel, 5, "South", serverTextField);
        contentPane.add(portTextField);
        layout.putConstraint("West", portTextField, 0, "West", serverTextField);
        layout.putConstraint("North", portTextField, 5, "South", serverTextField);

        JComboBox<String> scrollBox = new JComboBox<String>();
        for (ServerListItem server : serverList) {
            scrollBox.addItem(server.getServerName());
        }

        scrollBox.setVisible(true);
        // Client name field
        contentPane.add(nameLabel);
        layout.putConstraint("West", nameLabel, 5, "West", contentPane);
        layout.putConstraint("North", nameLabel, 5, "South", portTextField);
        //contentPane.add(nameTextField);
        contentPane.add(scrollBox);
        layout.putConstraint("West", scrollBox, 0, "West", portTextField);
        layout.putConstraint("North", scrollBox, 5, "South", portTextField);

        // Connect button
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                //System.out.println("Current item: " + scrollBox.getSelectedItem());
                for (ServerListItem server : serverList) {
                    if (scrollBox.getSelectedItem().equals(server.getServerName())) {
                        //System.out.println("Server address: " + server.getServerAddress());
                        //System.out.println("Server port: " + server.getServerPort());
                        int servPort = Integer.parseInt(server.getServerPort());
                        submit(server.getServerAddress(), servPort);
                    }
                }
                //submit(serverAddress, portNumber);//, name);
            }
        });
        contentPane.add(connectButton);
        layout.putConstraint("West", connectButton, 100, "West", contentPane);
        layout.putConstraint("North", connectButton, 5, "South", scrollBox);
        layout.putConstraint("East", contentPane, 5, "East", serverTextField);
        layout.putConstraint("South", contentPane, 5, "South", connectButton);

        connectGUI.setResizable(false);
        connectGUI.pack();
        connectGUI.setVisible(true);

    }

    // Makes connection to server
    private void submit(String serverAddress, int portNumber) {//, String name) {
        new GameGUI(new GameClient(serverAddress, portNumber));//, name));
        connectGUI.dispose();
    }
}
