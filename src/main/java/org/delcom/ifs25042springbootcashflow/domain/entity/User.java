package org.delcom.ifs25042springbootcashflow.domain.entity;

import java.io.Serial;
import java.io.Serializable;

/**
 * Entity pengguna aplikasi Cashflow (admin atau pengguna biasa).
 */
public class User implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int id;
    private final String username;
    private String passwordHash;
    private String fullName;
    private final Role role;
    private String photoPath;

    public User(int id, String username, String passwordHash, String fullName, Role role, String photoPath) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.photoPath = photoPath;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public Role getRole() {
        return role;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public void changeFullName(String fullName) {
        this.fullName = fullName;
    }

    public void changePasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void changePhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }
}
