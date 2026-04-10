package sn.edu.ept.security.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import sn.edu.ept.security.dtos.AuthenticationRequest;
import sn.edu.ept.security.dtos.AuthenticationResponse;
import sn.edu.ept.security.dtos.ChangePasswordRequest;
import sn.edu.ept.security.dtos.RegisterRequest;
import sn.edu.ept.security.dtos.UserDTO;
import sn.edu.ept.security.service.ConnectedUserService;
import sn.edu.ept.security.user.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final ConnectedUserService connectedUserService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
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

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(authenticationService.getAllUsers());
    }

    @GetMapping("/users/{authId}")
    public ResponseEntity<UserDTO> getUserById(
            @PathVariable Long authId) {

        return ResponseEntity.ok(authenticationService.getUserById(authId));
    }

    @GetMapping("/users/connected")
    public ResponseEntity<Map<String, Object>> getConnectedUsers() {
        Map<String, Object> response = new HashMap<>();
        response.put("connectedUsers", connectedUserService.getConnectedUsers());
        response.put("totalConnected", connectedUserService.getConnectedUsersCount());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> response = new HashMap<>();
        response.put("username", authentication.getName());
        response.put("authorities", authentication.getAuthorities());
        response.put("isAuthenticated", authentication.isAuthenticated());
        
        return ResponseEntity.ok(response);
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

}
