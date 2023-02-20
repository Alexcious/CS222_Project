package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GCreate extends JFrame implements ActionListener {
    private JLabel gcNameLabel;
    private JTextArea chatArea;
    private JList<String> gcList;
    private JButton createButton;
    private JButton cancelButton;

    public GCreate() {
        super("Messenger Chat");

        // set up main window
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // create GC Name label
        gcNameLabel = new JLabel("GC Name");
        add(gcNameLabel, BorderLayout.NORTH);

        // create chat area
        chatArea = new JTextArea();
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        // create GC list
        gcList = new JList<String>(new String[]{"GC 1", "GC 2", "GC 3"});
        add(new JScrollPane(gcList), BorderLayout.EAST);

        // create button
        createButton = new JButton("Create");
        createButton.addActionListener(this);

        // create cancel button
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);

        // create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);

        // add button panel to main window
        add(buttonPanel, BorderLayout.SOUTH);

        // show main window
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == createButton) {
            // show create group chat window
            JFrame createGCWindow = new JFrame("Create Group Chat");
            createGCWindow.setSize(300, 100);
            createGCWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            createGCWindow.setLayout(new BorderLayout());

            // create label and text field for group chat name
            JLabel nameLabel = new JLabel("Name:");
            JTextField nameField = new JTextField();
            JPanel namePanel = new JPanel(new BorderLayout());
            namePanel.add(nameLabel, BorderLayout.WEST);
            namePanel.add(nameField, BorderLayout.CENTER);

            // create add button
            JButton addButton = new JButton("Add");
            addButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String newGroupChatName = gcNameLabel.getText();
                    if (newGroupChatName.isEmpty()) {
                        JOptionPane.showMessageDialog(createGCWindow, "Group chat name cannot be empty.");
                        return;
                    }
                    // create a new DefaultListModel instance
                    DefaultListModel<String> model = new DefaultListModel<>();

                    // get the current list model
                    ListModel<String> listModel = gcList.getModel();

                    // add the current items to the new model
                    for (int i = 0; i < listModel.getSize(); i++) {
                        model.addElement(listModel.getElementAt(i));
                    }

                    // add the new group chat name to the new model
                    model.addElement(newGroupChatName);

                    // set the new model to the JList
                    gcList.setModel(model);

                    // close the create group chat window
                    createGCWindow.dispose();
                }
            });

            // create button panel for add and cancel buttons
            JPanel gcButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            gcButtonPanel.add(addButton);
            gcButtonPanel.add(new JButton("Cancel") {{
                addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        createGCWindow.dispose();
                    }
                });
            }});

            // add components to create group chat window
            createGCWindow.add(namePanel, BorderLayout.CENTER);
            createGCWindow.add(gcButtonPanel, BorderLayout.SOUTH);

            // show create group chat window
            createGCWindow.setVisible(true);
        } else if (e.getSource() == cancelButton) {
            // close main window
            dispose();
        }
    }

    public static void main(String[] args) {
        new GCreate();
    }
}
