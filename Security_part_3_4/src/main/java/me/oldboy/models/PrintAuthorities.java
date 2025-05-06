package me.oldboy.models;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PrintAuthorities {
    String username;
    String authority;

    public PrintAuthorities(String username, String authority) {
        this.username = username;
        this.authority = authority;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }
}
