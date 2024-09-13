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
import java.net.Socket;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class Client extends JFrame {

    Socket socket;
    BufferedReader br;
    PrintWriter out;

    // Declare components
    private JLabel heading = new JLabel("Client Area");
    private JTextArea messageArea = new JTextArea();
    private JTextField messageInput = new JTextField();
    private Font font = new Font("Roboto", Font.PLAIN, 20);

    // Constructor
    public Client() {

        try {
            System.out.println("Sending request to server ");
            socket = new Socket("127.0.0.1", 7776);  // Ensure the port matches the server
            System.out.println("Connection done");

            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);  // Enable auto-flush
            createGUI();
            handleEvents();
            startReading();
            startWriting();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
                    out.println(contentToSend);  // Send the message to the server
                    out.flush();  // Ensure the message is sent
                    messageInput.setText("");  // Clear input
                    messageInput.requestFocus();  // Refocus on the input field
                }
            }
        });
    }

    public void createGUI() {

        this.setTitle("Client Messenger");
        this.setSize(700, 700);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set up the layout and components
        ImageIcon icon = new ImageIcon(getClass().getResource("/p.png")); // Ensure the path is correct
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

    public void startReading() {

        Runnable r1 = () -> {
            System.out.println("Reader started");
            while (true) {
                try {
                    String msg = br.readLine();
                    if (msg.equals("exit")) {
                        System.out.println("Server terminated the chat");
                        JOptionPane.showMessageDialog(this, "Server terminated the chat");
                        messageInput.setEditable(false);  // Disable input after exit
                        socket.close();
                        break;
                    }
                    messageArea.append("Server: " + msg + "\n");
                } catch (IOException e) {
                    System.out.println("Connection closed.");
                    break;
                }
            }
        };

        new Thread(r1).start();
    }

    public void startWriting() {
        // Data from user input to be sent to the server

        Runnable r2 = () -> {
            System.out.println("Writer started");
            while (true && !socket.isClosed()) {
                try {
                    BufferedReader br3 = new BufferedReader(new InputStreamReader(System.in));
                    String content = br3.readLine();
                    out.println(content);
                    out.flush();
                    if (content.equalsIgnoreCase("exit")) {
                        socket.close();
                        break;
                    }
                } catch (IOException e) {
                    System.out.println("Connection closed.");
                    break;
                }
            }
        };

        // Start the writing thread
        new Thread(r2).start();
    }

    public static void main(String[] args) {
        System.out.println("This is client");
        new Client();
    }
}
