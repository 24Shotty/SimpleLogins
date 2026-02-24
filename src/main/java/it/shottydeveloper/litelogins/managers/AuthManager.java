package it.shottydeveloper.litelogins.managers;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AuthManager {
    private final Set<UUID> unauthenticatedPlayers = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public void addPlayer(UUID uuid) {
        unauthenticatedPlayers.add(uuid);
    }

    public void removePlayer(UUID uuid) {
        unauthenticatedPlayers.remove(uuid);
    }

    public boolean isPending(UUID uuid) {
        return unauthenticatedPlayers.contains(uuid);
    }
}