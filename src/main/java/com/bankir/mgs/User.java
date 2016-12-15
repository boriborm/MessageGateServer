package com.bankir.mgs;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

public class User implements Principal {

    public static final String ROLE_ANONYMOUS ="public";
    public static final  String ROLE_ADMIN = "admin";
    public static final  String ROLE_READER = "reader";
    public static final  String ROLE_EDITOR = "editor";
    public static final  String ROLE_SENDER = "sender";
    public static final  String ROLE_RESTSERVICE = "restservice";

    private String userName;
    private List<String> roles;
    private Long id;

    public User(Long id, String userName, List<String> roles){
        this.id = id;
        this.userName=userName;
        if (roles == null) {
            this.roles = new ArrayList<>();
            this.roles.add(ROLE_ANONYMOUS);
        } else this.roles = roles;
    }

    @Override
    public String getName() {
        return userName;
    }

    public boolean userWithRole(String role) {
        return role.equalsIgnoreCase(ROLE_ANONYMOUS) || (roles.contains(role));
    }

    public Long getId() {
        return id;
    }

    public boolean isAnonymous (){ return id==-1L;}
}
