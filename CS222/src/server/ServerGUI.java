package server;

import javax.swing.*;
import javax.swing.BorderFactory;
import java.awt.*;
import java.io.File;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ServerGUI extends JFrame{
    private JList<String> usersList;
    private DefaultListModel<String> usersListModel;
    private JButton addButton;
    private JButton deleteButton;
    private JButton banButton;
    private JScrollPane scrollPane;
    private JTextField searchField;
    private JTextField nameField;
    private JTextField usernameField;
    private JPasswordField passwordField;

    public ServerGUI() {
        super("List Users");

        // Create a JPanel to hold the content
        JPanel content = new JPanel();
        content.setLayout(new BorderLayout());

        setContentPane(content);
        Border border = BorderFactory.createLineBorder(Color.WHITE, 12);
        content.setBorder(border);

        // Create the list model and JList
        usersListModel = new DefaultListModel<String>();
        usersList = new JList<String>(usersListModel);
        scrollPane = new JScrollPane(usersList);
        add(scrollPane);

        searchField = new JTextField();
        add(searchField, BorderLayout.NORTH);
        // The code is the listener for when a user is selected
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                search();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                search();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                search();
            }
        });

        addButton = new JButton("Add");
        deleteButton = new JButton("Delete");
        banButton = new JButton("Ban");

        usersList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                // get the selected user
                String selectedUser = usersList.getSelectedValue();
                // check if the user is banned
                boolean isBanned = UserXMLWriter.isUserBanned(selectedUser);
                // update the ban button text
                if (isBanned) {
                    banButton.setText("Unban User");
                } else {
                    banButton.setText("Ban User");
                }
            }
        });

        // The code is for adding new users. It makes a JPanel and adds it to the JFrame, is has fields for the user to input
        // the name and password and calls the saveUser method.
        addButton.addActionListener(e -> {
            JPanel addButtonContent = new JPanel();
            addButtonContent.setLayout(new BorderLayout());

            setContentPane(addButtonContent);
            Border addButtonBorder = BorderFactory.createLineBorder(Color.WHITE, 12);
            addButtonContent.setBorder(addButtonBorder);

            nameField = new JTextField(1);
            passwordField = new JPasswordField(1);

            JLabel nameLabel = new JLabel("Name:");
            JLabel passwordLabel = new JLabel("Password:");

            JButton backButton = new JButton("Back");
            backButton.addActionListener(s -> {
                SwingUtilities.invokeLater(() -> {
                    dispose();
                    new ServerGUI().setVisible(true);
                });
            });

            JButton saveButton = new JButton("Save");
            saveButton.addActionListener(s1 -> {
                saveUserData();
                SwingUtilities.invokeLater(() -> {
                    dispose();
                    new ServerGUI().setVisible(true);
                });
            });
            JPanel addPanel = new JPanel(new GridLayout(8, 2, 1, 8));
            addPanel.add(nameLabel);
            addPanel.add(nameField);
            addPanel.add(passwordLabel);
            addPanel.add(passwordField);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(backButton);
            buttonPanel.add(saveButton);

            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            mainPanel.add(addPanel, BorderLayout.CENTER);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            add(mainPanel);
            setSize(250, 350);
        });

        deleteButton.addActionListener(e -> {
            // removes the user from the xml file
            Object selectedUser = usersList.getSelectedValue();
            if (selectedUser == null) {
                JOptionPane.showMessageDialog(content, "Please select a user to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                int option = JOptionPane.showOptionDialog(content, "Are you sure you want to delete " + selectedUser, "Confirmation", JOptionPane.INFORMATION_MESSAGE,  // option type
                        JOptionPane.WARNING_MESSAGE,  // message type
                        null,  // icon
                        new String[]{"Yes", "No"},  // options
                        "No"  // default option
                );
                if (option == JOptionPane.YES_OPTION) {
                    String selectedItem = selectedUser.toString();
                    UserXMLWriter.deleteUser(selectedItem);
                    JOptionPane.showMessageDialog(content, "User " + selectedUser + " has been deleted.", "User Deleted", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    dispose();
                    new ServerGUI().setVisible(true);
                }
            }
            // updates the JList to remove the user
            int selectedIndex = usersList.getSelectedIndex();
            if (selectedIndex != -1) {
                usersListModel.remove(selectedIndex);
                usersList.setSelectedIndex(-1);
                usersList.setModel(usersListModel);
            }
        });

        banButton.addActionListener(e -> {
            String selectedUser = usersList.getSelectedValue();
            boolean isBanned = UserXMLWriter.isUserBanned(selectedUser);

            if (selectedUser == null) {
                JOptionPane.showMessageDialog(content, "Please select a user to ban.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                //unban
                if (isBanned) {
                    int option = JOptionPane.showOptionDialog(content, "Are you sure you want to unban " + selectedUser, "Confirmation", JOptionPane.INFORMATION_MESSAGE,  // option type
                            JOptionPane.WARNING_MESSAGE,  // message type
                            null,  // icon
                            new String[]{"Yes", "No"},  // options
                            "No"  // default option
                    );
                    if (option == JOptionPane.YES_OPTION) {
                        UserXMLWriter.unbanUser(selectedUser);
                        JOptionPane.showMessageDialog(content, "User " + selectedUser + " has been unbanned.", "User Banned", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        dispose();
                        new ServerGUI().setVisible(true);
                    }

                    // ban
                } else {
                    int option = JOptionPane.showOptionDialog(content, "Are you sure you want to ban " + selectedUser, "Confirmation", JOptionPane.INFORMATION_MESSAGE,  // option type
                            JOptionPane.WARNING_MESSAGE,  // message type
                            null,  // icon
                            new String[]{"Yes", "No"},  // options
                            "No"  // default option
                    );
                    if (option == JOptionPane.YES_OPTION) {
                        UserXMLWriter.banUser(selectedUser);
                        JOptionPane.showMessageDialog(content, "User " + selectedUser + " has been banned.", "User Banned", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        dispose();
                        new ServerGUI().setVisible(true);
                    }
                }
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(banButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setSize(325, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        populateListFromXML("res/usersList");
    }

    /**
     * This function gets the text from the fields from the JFrame and saves the user to the xml file
     */
    private void saveUserData() {
        try {
            String name = nameField.getText();
            String password = new String(passwordField.getPassword());

            User user = new User(name, password, false);
            UserXMLWriter.registerUser(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * It creates a DocumentBuilderFactory, creates a DocumentBuilder, parses the XML file, gets the root element, gets all
     * the name elements, and adds them to the list model
     *
     * @param filename The name of the XML file to read from.
     */
    private void populateListFromXML(String filename) {
        try {
            // Create the DocumentBuilderFactory and DocumentBuilder
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            // Parse the XML file and get the root element
            Document doc = builder.parse(new File(filename));
            Element root = doc.getDocumentElement();

            // Get all the name elements and add them to the list model
            NodeList nameNodes = root.getElementsByTagName("name");
            for (int i = 0; i < nameNodes.getLength(); i++) {
                Element nameElement = (Element) nameNodes.item(i);
                String name = nameElement.getTextContent();
                usersListModel.addElement(name);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * If the user's name contains the search string, add it to the filtered list.
     */
    private void search() {
        String search = searchField.getText();
        DefaultListModel<String> filteredListModel = new DefaultListModel<String>();
        for (int i = 0; i < usersListModel.size(); i++) {
            String user = usersListModel.get(i);
            if (user.toLowerCase().contains(search.toLowerCase())) {
                filteredListModel.addElement(user);
            }
        }
        usersList.setModel(filteredListModel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ServerGUI().setVisible(true);
        });

    }
}

