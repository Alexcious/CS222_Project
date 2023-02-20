package client;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.URL;

public class ChatBoxFrame extends JFrame {
    private ClientFrame clientFrame;
    private Client client;
    private JTextArea messageArea;
    private JTextField messageField;
    private JButton sendButton;
    private JPanel chatPanel;
    private String recipient;

    public ChatBoxFrame(String recipient, ClientFrame cf, Client c) {

        super(cf.getUser() + "'s conversation with " + recipient);
        this.recipient = recipient;
        clientFrame = cf;
        client = c;


        setSize(400, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        chatPanel.setLayout(new BorderLayout());
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        JScrollPane scrollPane = new JScrollPane(messageArea);
        chatPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());

        messageField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });
        messageField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        inputPanel.add(messageField, BorderLayout.CENTER);

        sendButton.setIcon(getImage("Send.png", 32, 32));
        sendButton.setPreferredSize(new Dimension(40, 40));
        sendButton.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        sendMessage();
                    }
                }
        );
        inputPanel.add(sendButton, BorderLayout.EAST);

        //TODO is somewhat faulty, idk if its this one or line 74
        chatPanel.add(inputPanel, BorderLayout.SOUTH);
        add(chatPanel);
        setVisible(true);

        client.addChatBoxFrame(this);
    }

    public String getRecipient(){ return recipient;}

    private ImageIcon getImage(String filename, int width, int height) {
        try {
            URL url = getClass().getResource("/icons/" + filename);
            Image img = ImageIO.read(url);
            Image scaledImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImg);
        } catch (Exception ex) {
            System.err.println("Error loading image: " + ex.getMessage());
            return null;
        }
    }

    public void updateGUI(String dialog) { messageArea.append(dialog + "\n"); }

    public void disableConversation(){
        messageArea.setEnabled( false );
        sendButton.setEnabled(false);
    }

    private void sendMessage()
    {
        String messageToSend = messageField.getText();

        // do nothing if the user has not typed a message
        if ( !messageToSend.equals( "" ) ) {

            Document sendMessage;
            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();

            try {

                // get DocumentBuilder
                DocumentBuilder builder =
                        factory.newDocumentBuilder();

                // create xml message
                sendMessage = builder.newDocument();
                Element root = sendMessage.createElement( "message" );

                root.setAttribute( "to", recipient );
                root.setAttribute( "from", clientFrame.getUser() );
                root.appendChild(
                        sendMessage.createTextNode( messageToSend ) );
                sendMessage.appendChild( root );

                client.send( sendMessage );

                updateGUI(clientFrame.getUser() +
                        ":  " + messageToSend );
                messageField.setText( "" );
            }
            catch ( ParserConfigurationException pce ) {
                pce.printStackTrace();
            }
        }
    }

}
