package client;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.event.*     ;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientFrame extends JFrame {
    private JPanel mainPanel;
    private JList<String> userList;
    private JButton mainButton;
    private JButton favButton;
    private JButton GCButton;
    private Client client;
    private String user;
    private JTextField searchTextField;
    private JButton searchButton;
    private JButton broadcastButton;
    private JButton disconnectButton;
    private JButton addFavoriteButton;
    private List<String> onlineUsers;
    private int numberOfOnlineUsers;

    public String getUser(){
        return user;
    }

    public ClientFrame(String name, Client client) {
        setContentPane(mainPanel);
        setTitle(name);

        this.client = client;
        user = name;

        updateOnlineUsersList();

        NodeList userElts = client.getUsers().getDocumentElement().getElementsByTagName("user");
        numberOfOnlineUsers = userElts.getLength();

        //to be put in jList
        onlineUsers = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < numberOfOnlineUsers; i++) {
            String currentUser = userElts.item(i).getFirstChild().getNodeValue();
            onlineUsers.add(currentUser);
        }


        DefaultListModel<String> model = new DefaultListModel<>();
        for (String item : onlineUsers) {
            model.addElement(item);
        }

        userList.setModel(model);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        MouseListener usersListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int selectedIndex = userList.getSelectedIndex();

                if (e.getClickCount() == 2 && selectedIndex >= 0)
                    initiateMessage(selectedIndex);
            }
        };
        userList.addMouseListener(usersListener);

        setSize(400, 600);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);

        JButton addFavoriteButton = new JButton("Add Favorite");

// Add an ActionListener to the button
        addFavoriteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        mainButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        favButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        GCButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        broadcastButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.sendBroadcast();
            }
        });

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String searchUser = searchTextField.getText();
                DefaultListModel<String> model = (DefaultListModel<String>) userList.getModel();
                int index = model.indexOf(searchUser);
                if (index != -1) {
                    userList.setSelectedIndex(index);
                    userList.ensureIndexIsVisible(index);
                } else {
                    JOptionPane.showMessageDialog(ClientFrame.this, "User not found.");
                }
            }
        });

        disconnectButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        disconnectUser();
                    }
                }
        );

        addWindowListener(
                new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        disconnectUser();
                    }
                }
        );
    }

    // This method should be called to update the contents of the JList
    private void updateOnlineUsersList() {

    }

    private void initiateMessage(int selectedIndex) {
        //TODO make the gui for the conversation\

        String recipient = onlineUsers.get(selectedIndex);

        if ( client.findConversationIndex( recipient ) == -1){
            new ChatBoxFrame( recipient, this, client);
        }
    }

    public void add(String userToAdd) {
        // add user to ArrayList onlineUsers
        onlineUsers.add(userToAdd);
        // update JList usersList
        DefaultListModel<String> model = new DefaultListModel<>();
        for (String user : onlineUsers) {
            model.addElement(user);
        }
        userList.setModel(model);
    }
    public void remove( String userToRemove )
    {
        // remove user from Vector onlineUsers
        onlineUsers.remove(findOnlineUsersIndex( userToRemove ) );
        String[] onlineUsersArray = onlineUsers.toArray(new String[0]);
        // update JList usersList
        userList.setListData( onlineUsersArray );
    }
    public void disconnectUser()
    {
        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
        Document disconnectUser;
        try {

            // get DocumentBuilder
            DocumentBuilder builder =
                    factory.newDocumentBuilder();

            // create root node
            disconnectUser = builder.newDocument();

            disconnectUser.appendChild(
                    disconnectUser.createElement( "disconnect" ) );

            client.send( disconnectUser );
            client.stopListening();
        }
        catch ( ParserConfigurationException pce ) {
            pce.printStackTrace();
        }

    }
    public int findOnlineUsersIndex( String onlineUserName )
    {
        for ( int i = 0; i < onlineUsers.size(); i++ ) {
            String currentUserName = onlineUsers.get( i );

            if ( currentUserName.equals( onlineUserName ) )
                return i;
        }

        return -1;
    }
}
