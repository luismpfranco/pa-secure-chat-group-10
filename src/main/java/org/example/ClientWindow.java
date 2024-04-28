package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
 * This class represents the client window that is displayed when a client connects to the server.
 */
public class ClientWindow extends JFrame {
    private final Client client;
    private final JTextArea messagesArea;
    private final JTextField inputField;
    private final JList<Client> clientList;
    private static int windowCount = 0;
    private static final int WINDOW_DISTANCE = 360;

    /**
     * Instantiates a new Client window.
     *
     * @param client the client
     * @param server the server
     */
    public ClientWindow(Client client, Server server) {
        this.client = client;

        setTitle(client.getUsername());
        setSize(350, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLocation(windowCount * WINDOW_DISTANCE, WINDOW_DISTANCE);
        windowCount++;

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel toLabel = new JLabel("Para:");
        topPanel.add(toLabel);
        
        clientList = new JList<>(server.getClients().toArray(new Client[0]));
        clientList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JScrollPane scrollPane = new JScrollPane(clientList);
        scrollPane.setPreferredSize(new Dimension(205, 70));
        topPanel.add(scrollPane);

        add(topPanel, BorderLayout.NORTH);

        messagesArea = new JTextArea();
        messagesArea.setEditable(false);
        add(new JScrollPane(messagesArea), BorderLayout.CENTER);

        inputField = new JTextField();
        add(inputField, BorderLayout.SOUTH);
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    List<Client> selectedClients = clientList.getSelectedValuesList();
                    Client[] selectedClientsArray = selectedClients.toArray(new Client[0]);
                    client.sendMessage(inputField.getText(), selectedClientsArray);
                    inputField.setText("");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        JButton broadcastButton = new JButton("Broadcast");
        add(broadcastButton, BorderLayout.EAST);
        broadcastButton.addActionListener(e -> {
            try {
                Client[] allClients = server.getClients().toArray(new Client[0]);
                client.sendMessage(inputField.getText(), allClients);
                inputField.setText("");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        setVisible(true);
    }

    /**
     * Add message to the client window.
     *
     * @param message the message
     */
    public void addMessage(String message) {
        messagesArea.append(message + "\n");
    }

    /**
     * Update the client list.
     *
     * @param clients the clients
     */
    public void updateClient(Client[] clients) {
        ArrayList<Client> otherClients = new ArrayList<>(Arrays.asList(clients));
        otherClients.remove(client);
        DefaultListModel<Client> model = new DefaultListModel<>();
        for (Client c : otherClients) {
            model.addElement(c);
        }
        clientList.setModel(model);
    }
}