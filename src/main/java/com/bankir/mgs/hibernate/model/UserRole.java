package com.bankir.mgs.hibernate.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name="USERROLES")
public class UserRole implements Serializable {

    @Id
    @Column(name="roleName")
    private String roleName;

    @Id
    @Column(name="userId")
    private Long userId;

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String role) {
        this.roleName = role;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
