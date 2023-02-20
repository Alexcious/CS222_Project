package server;

import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class UserXMLWriter {
    private static final String FILE_NAME = "res/usersList";

    /**
     * It takes a User object as a parameter, and then it checks if a user already exists in the XML file. If a user
     * exists, it replaces the existing user with the new user. If a user doesn't exist, it adds the new user to the XML
     * file
     *
     * @param user The user object that contains the user's name, username, password, and status.
     * @return A boolean value.
     */
    public static boolean registerUser(User user) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new File("res/TempUserData.xml"));

            // Check if user already exists
            NodeList userList = doc.getElementsByTagName("user");
            if (userList.getLength() > 0) {
                Element oldUser = (Element) userList.item(0);
                // Replace existing user
                oldUser.getElementsByTagName("name").item(0).setTextContent(user.getName());
                oldUser.getElementsByTagName("password").item(0).setTextContent(user.getPassword());
                oldUser.getElementsByTagName("isBanned").item(0).setTextContent(Boolean.toString(user.getBanned()));
            } else {
                // Add new user
                Element userElement = doc.createElement("user");

                Element nameElement = doc.createElement("name");
                nameElement.appendChild(doc.createTextNode(user.getName()));
                userElement.appendChild(nameElement);

                Element passwordElement = doc.createElement("password");
                passwordElement.appendChild(doc.createTextNode(user.getPassword()));
                userElement.appendChild(passwordElement);

                Element statusElement = doc.createElement("isBanned");
                statusElement.appendChild(doc.createTextNode(Boolean.toString(user.getBanned())));
                userElement.appendChild(statusElement);

                doc.appendChild(userElement);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);

            StreamResult result = new StreamResult(new File("res/TempUserData.xml"));
            transformer.transform(source, result);
            appendXMLFiles();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * It takes the contents of the file "res/TempUserData.xml" and appends it to the file "res/TempUserData.xml"
     */
    public static void appendXMLFiles(){
        File file1 = new File("res/TempUserData.xml");
        File file2 = new File(FILE_NAME);
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc1 = db.parse(file1);
            Document doc2 = db.parse(file2);
            Element root1 = doc1.getDocumentElement();
            Node importedNode = doc2.importNode(root1, true);
            doc2.getDocumentElement().appendChild(importedNode);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            DOMSource source = new DOMSource(doc2);
            StreamResult result = new StreamResult(new StringWriter());
            transformer.transform(source, result);
            String xmlString = result.getWriter().toString();
            FileWriter fw = new FileWriter(file2);
            fw.write(xmlString);
            fw.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * It takes a username as a parameter, finds the user in the XML file, and adds a status element with the value
     * "banned" to the user's element
     *
     * @param name The name of the user to be banned.
     */
    public static void banUser(String name) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new File(FILE_NAME));

            Node root = doc.getDocumentElement();
            NodeList users = root.getChildNodes();

            for (int i = 0; i < users.getLength(); i++) {
                Node user = users.item(i);
                if (user.getNodeType() == Node.ELEMENT_NODE) {
                    Element userElement = (Element) user;
                    String currentName = userElement.getElementsByTagName("name").item(0).getTextContent();

                    if (currentName.equals(name)) {
                        // Get the "isBanned" node of the current user
                        Element isBannedNode = (Element) userElement.getElementsByTagName("isBanned").item(0);
                        isBannedNode.setTextContent("true");

                        TransformerFactory transformerFactory = TransformerFactory.newInstance();
                        Transformer transformer = transformerFactory.newTransformer();
                        DOMSource source = new DOMSource(doc);

                        StreamResult result = new StreamResult(new File(FILE_NAME));
                        transformer.setOutputProperty(OutputKeys.INDENT, "no");
                        transformer.transform(source, result);
                        return;
                    }
                }
            }
            System.out.println("User " + name + " not found.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void unbanUser(String name) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new File(FILE_NAME));

            Node root = doc.getDocumentElement();
            NodeList users = root.getChildNodes();

            for (int i = 0; i < users.getLength(); i++) {
                Node user = users.item(i);
                if (user.getNodeType() == Node.ELEMENT_NODE) {
                    Element userElement = (Element) user;
                    String currentName = userElement.getElementsByTagName("name").item(0).getTextContent();

                    if (currentName.equals(name)) {
                        // Get the "isBanned" node of the current user
                        Element isBannedNode = (Element) userElement.getElementsByTagName("isBanned").item(0);
                        isBannedNode.setTextContent("false");

                        TransformerFactory transformerFactory = TransformerFactory.newInstance();
                        Transformer transformer = transformerFactory.newTransformer();
                        DOMSource source = new DOMSource(doc);

                        StreamResult result = new StreamResult(new File(FILE_NAME));
                        transformer.setOutputProperty(OutputKeys.INDENT, "no");
                        transformer.transform(source, result);
                        return;
                    }
                }
            }
            System.out.println("User " + name + " not found.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isUserBanned(String name){
        try {
            File file = new File(FILE_NAME);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);

            NodeList userList = doc.getElementsByTagName("user");
            for (int i = 0; i < userList.getLength(); i++) {
                Element user = (Element) userList.item(i);
                String userName = user.getElementsByTagName("name").item(0).getTextContent();
                Element banned = (Element) user.getElementsByTagName("isBanned").item(0);
                if (userName.equals(name) && banned.getTextContent().equals("true")) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Find the user node with the matching username, remove the user node, and re-write the XML file
     *
     * @param name The username of the user to delete
     */
    public static void deleteUser(String name) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new File(FILE_NAME));

            // Goes through the list of users
            NodeList users = doc.getElementsByTagName("user");
            for (int i = 0; i < users.getLength(); i++) {
                Element user = (Element) users.item(i);
                String userName = user.getElementsByTagName("name").item(0).getTextContent();
                if (userName.equals(name)) {
                    // Remove the user node
                    user.getParentNode().removeChild(user);
                    break;
                }
            }
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);

            StreamResult result = new StreamResult(new File(FILE_NAME));
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            transformer.transform(source, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

