package main;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class Server extends JFrame {

    ServerSocket server;
    Socket socket;
    BufferedReader br;
    PrintWriter out;

    // GUI Components
    private JLabel heading = new JLabel("Server Area");
    private JTextArea messageArea = new JTextArea();
    private JTextField messageInput = new JTextField();
    private Font font = new Font("Roboto", Font.PLAIN, 20);

    // Constructor
    public Server() {

        try {
            server = new ServerSocket(7776);  // Change the port if necessary
            System.out.println("Server is ready to accept connection");
            System.out.println("Waiting for client...");

            socket = server.accept();  // Accept the connection from the client

            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);  // Autoflush enabled

            createGUI();  // Initialize the GUI
            handleEvents();  // Handle events like key presses
            startReading();  // Start reading messages from the client
            startWriting();  // Start writing messages to the client

        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to create GUI for the Server
    public void createGUI() {
        this.setTitle("Server Messenger");
        this.setSize(700, 700);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set up the layout and components
        ImageIcon icon = new ImageIcon(getClass().getResource("/p.png"));  // Ensure the path is correct
        Image img = icon.getImage();
        Image scaledImg = img.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        icon = new ImageIcon(scaledImg);

        heading.setFont(font);
        messageArea.setFont(font);
        messageInput.setFont(font);
        heading.setIcon(icon);
        heading.setHorizontalTextPosition(SwingConstants.CENTER);
        heading.setVerticalTextPosition(SwingConstants.BOTTOM);
        messageInput.setHorizontalAlignment(SwingConstants.CENTER);

        this.setLayout(new BorderLayout());
        heading.setHorizontalAlignment(SwingConstants.CENTER);
        heading.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Add components to the frame
        this.add(heading, BorderLayout.NORTH);
        this.add(messageArea, BorderLayout.CENTER);
        this.add(messageInput, BorderLayout.SOUTH);

        this.setVisible(true);
    }

    // Method to handle key events (send message on Enter key press)
    public void handleEvents() {
        messageInput.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {}

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == 10) {  // Enter key
                    String contentToSend = messageInput.getText();
                    messageArea.append("Me: " + contentToSend + "\n");
                    out.println(contentToSend);  // Send the message to the client
                    out.flush();  // Ensure the message is sent
                    messageInput.setText("");  // Clear input
                    messageInput.requestFocus();  // Refocus on the input field
                }
            }
        });
    }

    // Method to start reading messages from the client
    public void startReading() {
        Runnable r1 = () -> {
            System.out.println("Reader started");
            while (true) {
                try {
                    String msg = br.readLine();
                    if (msg.equals("exit")) {
                        System.out.println("Client terminated the chat");
                        JOptionPane.showMessageDialog(this, "Client terminated the chat");
                        messageInput.setEditable(false);  // Disable input after exit
                        socket.close();
                        break;
                    }
                    messageArea.append("Client: " + msg + "\n");
                } catch (IOException e) {
                    System.out.println("Connection closed.");
                    break;
                }
            }
        };

        new Thread(r1).start();
    }

    // Method to start writing messages to the client
    public void startWriting() {
        Runnable r2 = () -> {
            System.out.println("Writer started");
            try {
                BufferedReader brConsole = new BufferedReader(new InputStreamReader(System.in));
                while (true) {
                    String content = brConsole.readLine();
                    if (content.equalsIgnoreCase("exit")) {
                        out.println(content);
                        socket.close();  // Close the connection if "exit" is typed
                        break;
                    }
                    out.println(content);
                    out.flush();  // Ensure the message is sent immediately
                }
            } catch (IOException e) {
                System.out.println("Connection closed.");
            }
        };

        new Thread(r2).start();
    }

    // Main method to start the Server application
    public static void main(String[] args) {
        System.out.println("This is the server...");
        new Server();
    }
}
