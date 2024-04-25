package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;

public class ClientWindow extends JFrame {
    private Client client;
    private JTextArea messagesArea;
    private JTextField inputField;
    private JComboBox<Client> clientComboBox;
    private JButton broadcastButton;
    private static int windowCount = 0;
    private static final int WINDOW_DISTANCE = 360;

    public ClientWindow(Client client, Server server) {
        this.client = client;

        setTitle(client.getUsername());
        setSize(350, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocation(windowCount * WINDOW_DISTANCE, WINDOW_DISTANCE);
        windowCount++;

        messagesArea = new JTextArea();
        messagesArea.setEditable(false);
        add(new JScrollPane(messagesArea), BorderLayout.CENTER);

        inputField = new JTextField();
        add(inputField, BorderLayout.SOUTH);
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Client selectedClient = (Client) clientComboBox.getSelectedItem();
                    client.sendMessage(inputField.getText(), selectedClient);
                    inputField.setText("");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });


        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel toLabel = new JLabel("Para:");
        topPanel.add(toLabel);

        clientComboBox = new JComboBox<>(server.getClients().toArray(new Client[0]));
        topPanel.add(clientComboBox);

        add(topPanel, BorderLayout.NORTH);

        broadcastButton = new JButton("Broadcast");
        add(broadcastButton, BorderLayout.EAST);
        broadcastButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Client[] allClients = server.getClients().toArray(new Client[0]);
                    client.sendMessage(inputField.getText(), allClients);
                    inputField.setText("");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        setVisible(true);
    }

    public void addMessage(String message) {
        messagesArea.append(message + "\n");
    }

    public void updateClient(Client[] clients) {
        ArrayList<Client> otherClients = new ArrayList<>(Arrays.asList(clients));
        otherClients.remove(client);
        clientComboBox.setModel(new DefaultComboBoxModel<>(otherClients.toArray(new Client[0])));
    }
}