package io.magics.notethis.utils.models;

import java.util.HashMap;

public class User {

    private String email;

    public User() {}

    public User(String email) {
        this.email = email;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

}
