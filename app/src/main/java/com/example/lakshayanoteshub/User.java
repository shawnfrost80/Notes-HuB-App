package com.example.lakshayanoteshub;

public class User {

    String email;
    String password;

    User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    String getEmail() {
        return email;
    }

    String getPassword() {
        return password;
    }

}
