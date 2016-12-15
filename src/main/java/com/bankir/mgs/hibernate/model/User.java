package com.bankir.mgs.hibernate.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name="USERS")
public class User implements Serializable {

    @Id
    @SequenceGenerator(name="SEQ_USERID", sequenceName="SEQ_USERID",allocationSize=1)
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="SEQ_USERID")
    @Column(name="id")
    private Long id;

    @Column(name="login")
    private String login;

    @Column(name="hashInfo")
    private String hashInfo;

    @Type(type="yes_no")
    @Column(name="locked")
    private boolean locked;

    @Column(name="userName")
    private String userName;

    @Column(name="userRoles")
    private String userRoles;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getHashInfo() {
        return hashInfo;
    }

    public void setHashInfo(String hashInfo) {
        this.hashInfo = hashInfo;
    }

    public List<String> getRoles() {
        java.lang.reflect.Type listType = new TypeToken<List<String>>() {}.getType();
        return new Gson().fromJson(userRoles, listType);
    }

    public void setRoles(List<String> roles){
        userRoles = new Gson().toJson(roles);
    }

    public User() {}

    public User(Long id) {
        this.id = id;
    }

    public User(String login, String hashInfo, boolean locked, String userName) {
        this.login = login;
        this.hashInfo = hashInfo;
        this.locked = locked;
        this.userName = userName;
    }
}
