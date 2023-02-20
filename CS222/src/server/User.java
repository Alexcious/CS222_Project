package server;
public class User {
    private String name;
    private String password;
    private boolean isBanned;
    private boolean status;

    //TODO: implementation of status
    public String getName() {
        return name;
    }
    public User(String name, String password, boolean isBanned) {
        super();
        this.name = name;
        this.password = password;
        this.isBanned = isBanned;
    }
    public User(String name) {
        super();
        this.name = name;
    }
    public User() {
        super();
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean getBanned(){
        return isBanned;
    }

}