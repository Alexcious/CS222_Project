
package server;

import org.w3c.dom.*;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import java.util.List;

public class UserManager {
    private User tempUser = null;
    private Users users = new Users();	//Users Array ...
    public static String fileName = "res/usersList"; //XML File Name ...
    public static final int userNameDoesNotExists = 0;
    public static final int userPasswordIncorrect = 1;
    public static final int userCorrect = 2;


    public void loadUsers() throws Exception {
        //Locate the file ...
        StreamSource xmlSource = new StreamSource(fileName);
        DOMResult domResult = new DOMResult();
        try {
            TransformerFactory.newInstance()
                    .newTransformer()
                    .transform(xmlSource, domResult);
        } catch( Exception ex ) {
            throw new Exception("Xml file not found");
        }
        Document doc = (Document) domResult.getNode();
        doc.normalize();
        //Begin parsing ...
        processElements(doc.getDocumentElement(), 0);

    }
    

    private void processElements(Element elem, int indent) {
        try {
            NodeList children = elem.getChildNodes();
            for (int i = 0 ; i < children.getLength() ; i++)
            {
                Node child = children.item(i);
                if (child instanceof Element) {
                    processElements((Element) child, indent+4);
                }
                else if (child instanceof Text) {
                    if ( elem.getNodeName().equals("name") ) {
                        if(tempUser == null) tempUser = new User();
                        //Set userName ...
                        tempUser.setName( child.getNodeValue());
                    } else if ( elem.getNodeName().equals("password") ) {
                        if(tempUser == null) tempUser = new User();
                        //Set password ...
                        tempUser.setPassword( child.getNodeValue() );
                        //Add the users to the list ...
                        users.getUsers().add( tempUser );
                        //Null it ...
                        tempUser = null;
                    }
                }
            }
        } catch( Exception ex ) {
            ex.printStackTrace();
        }
    }

    public int isUserExists( User user ) {
        List<User> dbUsers = users.getUsers();
        for (User o : dbUsers) {
            if (o.getName().equalsIgnoreCase(user.getName()) &&
                    o.getPassword().equals(user.getPassword())) {
                return userCorrect;
            } else if (o.getName().equalsIgnoreCase(user.getName()) &&
                    !o.getPassword().equals(user.getPassword())) {
                return userPasswordIncorrect;
            }
        }
        return userNameDoesNotExists;
    }


}
