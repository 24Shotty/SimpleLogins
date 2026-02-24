package it.shottydeveloper.litelogins.models;

import java.util.UUID;

public class AuthUser {

    private final UUID uuid;
    private final String name;
    private volatile String passwordHash;
    private volatile boolean authenticated = false;

    public AuthUser(UUID uuid, String name, String passwordHash) {
        this.uuid = uuid;
        this.name = name;
        this.passwordHash = passwordHash;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }
}