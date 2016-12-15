package com.bankir.mgs.jersey.model;

import java.util.List;

public class UserObject {

    private Long id;
    private String login;
    private String userName;
    private boolean locked;
    private List<String> roles;
    private String password;

    public UserObject() {}

    public UserObject(Long id, String login, String userName, List<String> roles, boolean locked) {
        this.id = id;
        this.login = login;
        this.userName = userName;
        this.roles = roles;
        this.locked = locked;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public List<String> getRoles() {
        return roles;
    }

    public String getLogin() {
        return login;
    }

    public boolean isLocked() {
        return locked;
    }
}
