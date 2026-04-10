package sn.edu.ept.user_service.profile;



import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.edu.ept.user_service.dto.UpdateProfileRequest;
import sn.edu.ept.user_service.dto.UserProfileResponse;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserProfileService userService;

    @GetMapping
    public ResponseEntity<List<UserProfile>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> myProfile(
            @RequestHeader("X-User-Id") Long authId
    ) {
        return ResponseEntity.ok(userService.getUserByAuthId(authId));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateUser(
            @RequestHeader("X-User-Id") Long authId,
            @RequestBody UpdateProfileRequest request
    ) {
        return ResponseEntity.ok(userService.updateUser(authId, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponse> getUserById(
            @PathVariable Long id,
            @RequestHeader("X-User-Role") String role
    ) {
        if (!role.equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/auth/{authId}")
    public ResponseEntity<UserProfileResponse> getUserByAuthId(
            @RequestHeader("X-User-Id") Long authId) {
        return ResponseEntity.ok(userService.getUserByAuthId(authId));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserProfile> getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
