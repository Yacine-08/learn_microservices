package sn.edu.ept.user_service.profile;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import sn.edu.ept.user_service.config.CustomUserDetails;
import sn.edu.ept.user_service.dto.UpdateProfileRequest;
import sn.edu.ept.user_service.dto.UserProfileResponse;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserProfileService userService;

    @GetMapping("/all")
    public ResponseEntity<List<UserProfile>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> myProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(userService.getUserByAuthId(userDetails.getAuthId()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateUser(
            @RequestBody UpdateProfileRequest request
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(userService.updateUser(userDetails.getAuthId(), request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/auth/{authId}")
    public ResponseEntity<UserProfileResponse> getUserByAuthId(@PathVariable Long authId) {
        return ResponseEntity.ok(userService.getUserByAuthId(authId));
    }


    @DeleteMapping("/{authId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long authId) {
        try {
            userService.deleteUser(authId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
