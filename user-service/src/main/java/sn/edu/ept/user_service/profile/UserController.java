package sn.edu.ept.user_service.profile;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.edu.ept.user_service.config.ConnectedUser;
import sn.edu.ept.user_service.config.CurrentUser;
import sn.edu.ept.user_service.dto.UpdateProfileRequest;
import sn.edu.ept.user_service.dto.UserProfileResponse;

import java.util.List;

/**
    contrôleur principal exposé aux clients via le gateway.
    le gateway valide le JWT et injecte les headers suivants avant de router :
    X-Auth-Id   → identifiant de l'utilisateur connecté (Long)
    X-User-Role → rôle de l'utilisateur (CLIENT, ADMIN, DRIVER)
    X-User-Email→ email de l'utilisateur
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserProfileService userService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> myProfile(
            @CurrentUser ConnectedUser user) {
        return ResponseEntity.ok(userService.getUserByAuthId(user.getId()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateMyProfile(
            @CurrentUser ConnectedUser user,
            @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateUser(user.getId(), request));
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserProfile>> getAllUsers(
            @CurrentUser ConnectedUser user) {
        if (!user.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponse> getUserById(
            @PathVariable Long id,
            @CurrentUser ConnectedUser user) {
        if (!user.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("auth/{authId}")
    public ResponseEntity<UserProfileResponse> getUserByAuthId(
            @PathVariable Long authId,
            @CurrentUser ConnectedUser user) {
        if (!user.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(userService.getUserByAuthId(authId));
    }

    @DeleteMapping("/{authId}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long authId,
            @CurrentUser ConnectedUser user) {
        if (!user.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            userService.deleteUser(authId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
