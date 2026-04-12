package sn.edu.ept.security.config;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {
    
    // Store blacklisted tokens with their expiration time
    private final ConcurrentHashMap<String, LocalDateTime> blacklistedTokens = new ConcurrentHashMap<>();
    
    public void blacklistToken(String token, LocalDateTime expirationTime) {
        blacklistedTokens.put(token, expirationTime);
    }
    
    public boolean isTokenBlacklisted(String token) {
        LocalDateTime expirationTime = blacklistedTokens.get(token);
        if (expirationTime == null) {
            return false;
        }
        
        // Remove expired tokens from blacklist to save memory
        if (expirationTime.isBefore(LocalDateTime.now())) {
            blacklistedTokens.remove(token);
            return false;
        }
        
        return true;
    }
    
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
    }
}
