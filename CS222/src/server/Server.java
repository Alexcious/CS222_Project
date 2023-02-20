package server;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Vector;

public class Server extends JFrame {
    private Document users;
    private DocumentBuilderFactory factory;
    private DocumentBuilder builder;
    private JLabel status;
    public static JTextArea display;
    public Vector<ServerThread> threadList; // online users
    static UserManager userManager = new UserManager();

    public Server(){
        super ("Server");
        try{
            // obtain the default parser
            factory = DocumentBuilderFactory.newInstance();

            // get DocumentBuilder
            builder = factory.newDocumentBuilder();
        } catch ( ParserConfigurationException pce ) {
            pce.printStackTrace();
        }
        Container c = getContentPane();
        status = new JLabel( "Status" );
        c.add(status,BorderLayout.NORTH);
        display = new JTextArea();
        display.setLineWrap( true );
        display.setEditable( false );
        c.add( new JScrollPane( display ), BorderLayout.CENTER );
        display.append( "Server waiting for connections\n" );
        setSize( 300, 300 );
        setVisible(true);

        threadList = new Vector<>();
        users = initializeUsers();

        //load the users
        try {
            userManager.loadUsers();
            display.append("Users are read");
        }catch (Exception e){
            JOptionPane.showMessageDialog(this,"Users are not read");
        }
    }

    private Document initializeUsers() {
        Document users = builder.newDocument();
        users.appendChild(users.createElement("users"));
        return  users;
    }


    public void run(){
        try {
            // create a ServerSocket
            ServerSocket server = new ServerSocket( 2003 );

            // wait for connections
            while ( true ) {
                Socket clientSocket = server.accept();
                display.append( "\nConnection received from: " +
                        clientSocket.getInetAddress().getHostName() );
                ServerThread serverThread = new ServerThread(clientSocket, this);
                serverThread.start();
            }
        }
        catch ( Exception e ) {
            e.printStackTrace();
            System.exit( 1 );
        }
    }

    /**
     * findUserIndex finds if user is in userThread and returns
     * @param userName
     * @return index of user if present if not returns -1
     */
    public int findUserIndex( String userName )
    {
        // find index of specified UserThread in Vector onlineUsers
        // return -1 if no corresponding UserThread is found
        for ( int i = 0; i < threadList.size(); i++ ) {
            ServerThread current = threadList.get(i);
            if ( current.name.equals( userName ) )
                return i;
        }
        return -1;
    }

    public Document getUsers()
    {
        return users;
    }

    /**
     * This method send a document to the client which has two types
     * - this will send the name of the client
     * - sending this document means that the client's credentials are correct
     */
    public void userLoggedIn(ServerThread serverThread){
        String name = serverThread.getUsername();
        updateGUI("New user: "+ name);
        // notify all users of user's login
        updateUsers(name,"login");
        // add new user element to Document users
        Element usersRoot = users.getDocumentElement();
        Element newUser = users.createElement( "user" );
        newUser.appendChild(users.createTextNode( name) );
        usersRoot.appendChild( newUser );
        updateGUI( "Added user: " + name );
        // adds the serverThread in the arraylist for the broadcast function
        threadList.add(serverThread);
    }

    /**
     * This methods adds a string to the display JTextArea that serves as a log
     * @param s new log
     */
    public void updateGUI( String s )
    {
        display.append( "\n" + s );
    }

    public void sendMessage( Document message){
        // transfer message to specified receiver
        Element root = message.getDocumentElement();
        String from = root.getAttribute( "from" );
        String to = root.getAttribute( "to" );
        int index = findUserIndex( to );

        updateGUI( "Received message To: " + to + ",  From: " + from );

        // send message to corresponding user
        ServerThread receiver =
                threadList.elementAt( index );
        receiver.send( message );
        updateGUI( "Sent message To: " + to +
                ",  From: " + from );
    }

    /**
     *This method is sends a document to all clients an updated list of onlineUsers
     *Its purpose varies depending on String type (login , logout)
     */
    public void updateUsers( String userName, String type )
    {
        // create xml update document
        Document doc = builder.newDocument();
        Element root = doc.createElement( "update" );
        Element userElt = doc.createElement( "user" );

        doc.appendChild( root );
        root.setAttribute( "type", type );
        root.appendChild( userElt );
        userElt.appendChild( doc.createTextNode( userName ) ); // new log in or user log out
        // send to all users
        for ( int i = 0; i < threadList.size(); i++ ) {
            ServerThread receiver = threadList.elementAt( i );
            receiver.send(doc);
        }

        updateGUI( "Notified online users of " +
                userName + "'s " + type );
    }

    public void broadcastMessage(Document doc, String sender){
            int senderIndex = findUserIndex(sender);
            if (senderIndex == -1) {
                System.out.println("Error: sender is not in the list of users.");
                return;
            }
            for (int i = 0; i < threadList.size(); i++) {
                if (i != senderIndex) {
                    ServerThread serverThread = threadList.get(i);
                   serverThread.send(doc);
                }
            }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.addWindowListener(
                new WindowAdapter() {
                    public void windowClosing( WindowEvent e ) {
                        System.exit( 0 );
                    }
                }
        );
        server.run();

    }
}
