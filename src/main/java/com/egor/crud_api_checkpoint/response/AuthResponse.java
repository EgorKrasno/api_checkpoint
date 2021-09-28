package com.egor.crud_api_checkpoint.response;

import com.egor.crud_api_checkpoint.model.User;

public class AuthResponse {
    private boolean authenticated;
    private User user;

    public AuthResponse(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public AuthResponse(boolean authenticated, User user) {
        this.authenticated = authenticated;
        this.user = user;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
