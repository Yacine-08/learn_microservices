package sn.edu.ept.security.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class ConnectedUserService {

    private final Map<String, LocalDateTime> connectedUsers = new ConcurrentHashMap<>();

    public void addUserConnection(String email) {
        connectedUsers.put(email, LocalDateTime.now());
        log.info("User connected: {} at {}", email, LocalDateTime.now());
    }

    public List<String> getConnectedUsers() {
        return new ArrayList<>(connectedUsers.keySet());
    }

    public int getConnectedUsersCount() {
        return connectedUsers.size();
    }

    public void removeUserConnection(String email) {
        connectedUsers.remove(email);
        log.info("User disconnected: {} at {}", email, LocalDateTime.now());
    }
}
