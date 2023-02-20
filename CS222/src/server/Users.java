package server;

import java.util.*;

public class Users {
    private List<User> users = new ArrayList<>();

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public void print() {
        for (User user : users) {
            System.out.println("UserName : " + user.getName() +
                    " - Password : " + user.getPassword());
        }
    }
}
//test