package server;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ServerThread extends Thread {
    public String name = "";
    private DocumentBuilderFactory factory;
    private InputStream input;
    private OutputStream output;
    private boolean keepListening;
    private DocumentBuilder builder;
    private Socket socket;
    private Server server;


    public ServerThread(Socket socket, Server server) {
        try
        {
            // obtain the default parser
            factory = DocumentBuilderFactory.newInstance();

            // get DocumentBuilder
            builder = factory.newDocumentBuilder();
        }
        catch ( ParserConfigurationException pce ) {
            pce.printStackTrace();
            System.exit( 1 );
        }
        this.socket = socket;
        this.server = server;
        keepListening = true;

        try {
            input = socket.getInputStream();
            output = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        {
            try {
                int bufferSize;

                while ( keepListening ) {
                    bufferSize = input.available();

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
            }
            catch ( SAXException | IOException e ) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method processes the message received from the server
     * @param message
     */
    private void messageReceived(Document message) {
        Element  root = message.getDocumentElement();
        String userName="";
        // validating the credentials of the client
        if (root.getTagName().equals("user")){
            String enteredName = root.getFirstChild().getNodeValue();
            server.updateGUI(enteredName);
            userName = enteredName.substring(0, enteredName.indexOf("|")  );
            String password = enteredName.substring(enteredName.indexOf("|") + 1);
            int returnValue = Server.userManager.isUserExists(new User(userName,password, false)); // 0, 1 ,2
            if (server.findUserIndex(userName) != -1){
                nameInUse(); // name is already logged in
            }else if ((returnValue != UserManager.userCorrect)) { //credentials are wrong
                if (returnValue == UserManager.userNameDoesNotExists)
                    nameNotExists(); // name is not in database
                else
                    passwordIncorrect(); // incorrect password
            }else { // name is unique and credentials are correct
                // send the online users
                name = userName;
                send(server.getUsers());
                server.userLoggedIn(this);

            }
        }
        else if (root.getTagName().equals("message")){
            server.sendMessage( message );
        }
        else if (root.getTagName().equals("broadcast")){
            server.broadcastMessage(message,name);
        }
        else if (root.getTagName().equals("disconnect")){
            keepListening = false;
            // TODO make a method to remove the client in the serverThread
        }
    }

    private void nameInUse()
    {
        Document enterUniqueName = builder.newDocument();

        enterUniqueName.appendChild(
                enterUniqueName.createElement( "nameInUse" ) );

        send( enterUniqueName );
    }


    private void nameNotExists()
    {
        Document enterUniqueName = builder.newDocument();

        enterUniqueName.appendChild(
                enterUniqueName.createElement( "nameNotExists" ) );

        send( enterUniqueName );
    }


    private void passwordIncorrect()
    {
        Document enterUniqueName = builder.newDocument();

        enterUniqueName.appendChild(
                enterUniqueName.createElement( "passwordIncorrect" ) );

        send( enterUniqueName );
    }


    public void send( Document message )
    {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(new DOMSource(message), new StreamResult(output));
        }
        catch (TransformerException | TransformerFactoryConfigurationError e ) {
            e.printStackTrace();

        }
    }

    public String getUsername() { return name;
    }
}
