package sn.edu.ept.user_service.profile;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.edu.ept.user_service.dto.UserProfileResponse;

import java.util.List;

@RestController
@RequestMapping("/api/users/internal")
@RequiredArgsConstructor
public class UserInternalController {

    private final UserProfileRepository repository;

    @PostMapping("/users")
    public ResponseEntity<List<UserProfileResponse>> getByAuthIds(
            @RequestBody List<Long> authIds) {

        List<UserProfileResponse> profils = repository
                .findAllByAuthIdIn(authIds)
                .stream()
                .map(UserProfileResponse::from)
                .toList();

        return ResponseEntity.ok(profils);
    }

    @GetMapping("/users/{authId}")
    public ResponseEntity<UserProfileResponse> getByAuthId(
            @PathVariable Long authId) {

        return repository.findByAuthId(authId)
                .map(UserProfileResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
