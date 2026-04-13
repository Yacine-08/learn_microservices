package sn.edu.ept.security.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import sn.edu.ept.security.config.JwtService;
import sn.edu.ept.security.config.TokenBlacklistService;
import sn.edu.ept.security.dtos.*;
import sn.edu.ept.security.service.ConnectedUserService;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final ConnectedUserService connectedUserService;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(authenticationService.login(request));
    }

    @PutMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@RequestBody ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        try {
            authenticationService.changePassword(userEmail, request);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Mot de passe modifié avec succès");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader("Authorization") String authHeader) {
        // Extract token from Authorization header
        String token = authHeader.substring(7); // Remove "Bearer " prefix

        // Blacklist the token
        Date expirationDate = jwtService.extractExpiration(token);
//        tokenBlacklistService.blacklistToken(token,
//                expirationDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
        tokenBlacklistService.blacklistToken(token,
                LocalDateTime.now());

        // Remove user from connected users list
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        connectedUserService.removeUserConnection(userEmail);

        // Clear security context
        SecurityContextHolder.clearContext();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Déconnexion réussie");
        return ResponseEntity.ok(response);
    }



}
