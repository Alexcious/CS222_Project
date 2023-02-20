package client;

import jdk.dynalink.linker.ConversionComparator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.print.Doc;
import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Vector;

public class Client extends JFrame{
    private DocumentBuilderFactory factory;
    private DocumentBuilder builder;
    private ClientFrame clientFrame;
    private JPasswordField password;
    private JLabel passwordLab;
    private JLabel status, nameLab;
    private JTextField name,broadcast;
    private JButton submit;
    private Document users;
    private Socket clientSocket;
    private InputStream input;
    private OutputStream output;
    private boolean keepListening;
    private Vector conversations;

    /**
     * Client Constructor
     * - instantiates JPane,JFields, and other GUI elements
     * - obtains
     */
    public Client() {
        try {
            // obtain the default parser
            factory = DocumentBuilderFactory.newInstance();

            // get DocumentBuilder
            builder = factory.newDocumentBuilder();

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        }
        Container c = getContentPane();
        c.setLayout(null);

        nameLab = new JLabel("Please enter your name: ");
        c.add(nameLab);
        nameLab.setText("Name");
        nameLab.setBounds(7, 14, 49, 28);
        {
            name = new JTextField(15);
            c.add(name);
            name.setBounds(105, 21, 112, 21);
        }

        {
            passwordLab = new JLabel();
            c.add(passwordLab);
            passwordLab.setText("Password");
            passwordLab.setBounds(7, 42, 98, 35);
        }
        {
            password = new JPasswordField();
            c.add(password);
            password.setColumns(15);
            password.setBounds(105, 49, 112, 21);
        }
        submit = new JButton("Submit");
        submit.setEnabled(false);
        c.add(submit);
        submit.setBounds(7, 84, 217, 56);
        {

        }
        submit.setPreferredSize(new java.awt.Dimension(43, 84));
        submit.addActionListener(e -> sendCredentials());


        status = new JLabel("Status: Not connected");
        c.add(status);
        status.setBounds(0, 147, 231, 21);
        addWindowListener(
                new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                }
        );


        this.setSize(244, 200);
        setVisible(true);
    }

    /**
     * runClient method
     * -connects clientSocket with LocalHost to chosen Port
     * -connects it to Input and output stream
     *
     *
     * while keep listening is true
     * -creates InputSource source that is bytearrayinputstream with buffer array
     * -parses read source and converts it as Doc message
     * -messageReceived method is called with Document message as its parameter
     */
    public void runClient() {
        try {
            clientSocket = new Socket(InetAddress.getLocalHost(), 2003);
            status.setText("Status : Connected to " + clientSocket.getInetAddress().getHostName());
            output = clientSocket.getOutputStream();
            input = clientSocket.getInputStream();
            submit.setEnabled(true);
            keepListening = true;
            while ( keepListening ) {
                int bufferSize = input.available();

                if ( bufferSize > 0 ) {
                    byte[] buf = new byte[ bufferSize ];
                    input.read( buf );

                    InputSource source = new InputSource(
                            new ByteArrayInputStream( buf ) );
                    Document message = builder.parse( source );
                    if ( message != null )
                        messageReceived( message );
                }
            }
            input.close();
            output.close();
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public int findConversationIndex( String username ){
        // find index of a specified ChatBoxFrame
        // if no corresponding ChatBoxFrame is found, return -1
        for ( int i = 0; i < conversations.size(); i++){
            ChatBoxFrame current =
                    (ChatBoxFrame) conversations.elementAt(i);
            if ( current.getRecipient().equals(username))
                return i ;
        }

        return -1;
    }


    public void addChatBoxFrame( ChatBoxFrame newChatBox) { conversations.add( newChatBox); }

    public void removeChatBox( String userName){
        conversations.removeElement(
                findConversationIndex( userName )
        );
    }


    /**
     * messageReceived method
     * -gets the element of received message Doc
     * -checks if root element is equal to one of the invalid inputs tagnames or equal to a login or update
     *  then displays appropriate message
     * @param message -
     */
    private void messageReceived(Document message) {
        //TODO conditions for:
        // -unique logins,
        // -update for new log in or logged out
        // -messages received from the server

        Element root = message.getDocumentElement();

        if (root.getTagName().equals("nameInUse"))
            // did not enter a unique name
            JOptionPane.showMessageDialog(this,
                    "That name is already in use." +
                            "\nPlease enter a unique name.");
        else if (root.getTagName().equals("nameNotExists"))
            // did not enter a unique name
            JOptionPane.showMessageDialog(this,
                    "UserName does not exist");
        else if (root.getTagName().equals("passwordIncorrect"))
            // did not enter a unique name
            JOptionPane.showMessageDialog(this,
                    "Password Incorrect");
        else if (root.getTagName().equals("users")) {
            // entered a unique name for login
            users = message;
            clientFrame = new ClientFrame(name.getText(), this);
            conversations = new Vector();
            dispose();

        }

        else if (root.getTagName().equals("update")) {
            // either a new user login or a user logout
            String type = root.getAttribute("type");
            NodeList userElt = root.getElementsByTagName("user");
            String updatedUser = userElt.item(0).getFirstChild().getNodeValue();
            // test for login or logout
            if (type.equals("login")){
                // login
                // add user to onlineUsers Vector
                // and update usersList
                clientFrame.add(updatedUser);
            }
            else
            {
                // logout
                // remove user from onlineUsers Vector
                // and update usersList
                clientFrame.remove(updatedUser);

                int index = findConversationIndex( updatedUser );

                if ( index != -1 ) {
                    ChatBoxFrame receiver = ( ChatBoxFrame ) conversations.elementAt( index );

                    receiver.updateGUI( updatedUser + " logged out ");
                    receiver.disableConversation();
                }
            }
        }
        else if (root.getTagName().equals("broadcast")){
            NodeList nodeList = message.getElementsByTagName("broadcast");
            Element msg = (Element) nodeList.item(0);
            JOptionPane.showMessageDialog(this
                    ,msg.getElementsByTagName("message").item(0).getTextContent()
                    ,"Broadcasted by: "+msg.getElementsByTagName("sender").item(0).getTextContent()
                    ,JOptionPane.PLAIN_MESSAGE);
        }
        else if ( root.getTagName().equals( "message" ) ) {
            String from = root.getAttribute( "from" );
            String messageText = root.getFirstChild().getNodeValue();

            // test if conversation already exists
            int index = findConversationIndex( from );

            if ( index != -1 ) {
                // conversation exists
                ChatBoxFrame receiver =
                        ( ChatBoxFrame ) conversations.elementAt( index );
                receiver.updateGUI( from + ":  " + messageText );
            }
            else {
                // conversation does not exist
                ChatBoxFrame newConv =
                        new ChatBoxFrame( from, clientFrame, this );
                newConv.updateGUI( from + ":  " + messageText );
            }
        }

        //create another class for the ui if the user is logged in

    }

    /**
     * getter for Users
     * @return Document of users
     */
    public Document getUsers() {
        return users;
    }


    // TODO broadcast
    // 1. Create new doc and put the bm (tagname = broadcast)
    public void sendBroadcast(){
        String message = JOptionPane.showInputDialog("Type broadcast message here.");
        Document brdCast = builder.newDocument();
        Element root = brdCast.createElement("broadcast");
        Element sender = brdCast.createElement("sender");
        Element msg = brdCast.createElement("message");
        brdCast.appendChild(root);
        root.appendChild(sender);
        root.appendChild(msg);
        msg.appendChild(brdCast.createTextNode( message));
        sender.appendChild(brdCast.createTextNode( name.getText()));
        send(brdCast);
    }
    /*
    <broadcast>
    string
    </broadcast>
    <sender> kami </sender>
     */

    /**
     * sendCredentials method sends Doc of submitted name as login attempt
     * -creates Doc submitName with "user" as root element tagname and string from Jtextfields name and password
     * -call send with submitName doc in parameter
     */
    public void sendCredentials() {
        Document submitName = builder.newDocument();
        Element root = submitName.createElement( "user" );
        submitName.appendChild( root );
        root.appendChild(submitName.createTextNode( name.getText()  +  "|"  +  new String( password.getPassword() ) ) );
        send(submitName);
    }

    /**
     * send method
     * - uses tranformer to transform a DOM with received message as parameter to an output stream
     * @param message
     */
    public void send(Document message) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(new DOMSource(message), new StreamResult(output));
        }
        catch ( TransformerException | TransformerFactoryConfigurationError e ) {
            e.printStackTrace();
        }
    }

    /**
     * main method that runs Client
     * @param args -
     */
    public static void main(String[] args) {
        Client client = new Client();
        client.runClient();
    }

    public void stopListening() {
        keepListening = false;
    }
}
