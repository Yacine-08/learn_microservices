package sn.edu.ept.user_service.profile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.edu.ept.user_service.dto.UpdateProfileRequest;
import sn.edu.ept.user_service.dto.UserProfileResponse;
import sn.edu.ept.user_service.event.UserRegisteredEvent;
import sn.edu.ept.user_service.exception.ProfilAlreadyExistException;
import sn.edu.ept.user_service.exception.ProfilNotFoundException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileService {

    private final UserProfileRepository userRepository;

    @Transactional
    public UserProfile createUser(UserRegisteredEvent event) {

        if (userRepository.existsByAuthId(event.authId())) {
            throw new ProfilAlreadyExistException("User with authId " + event.authId() + " already exists");
        }

        var profil = UserProfile.builder()
                .authId(event.authId())
                .firstname(event.firstname())
                .lastname(event.lastname())
                .email(event.email())
                .phone(event.phone())
                .role(Role.valueOf(event.role()))
                .build();

        UserProfile saved = userRepository.save(profil);
        return saved;
    }

    @Transactional
    public UserProfileResponse getUserByAuthId(Long authId) {
        UserProfile profil = userRepository.findByAuthId(authId)
                .orElseThrow(() -> new ProfilNotFoundException("User not found with authId: " + authId));
        return UserProfileResponse.from(profil);
    }

    @Transactional
    public UserProfileResponse updateUser(Long authId, UpdateProfileRequest request) {
        UserProfile profil = userRepository.findByAuthId(authId)
                .orElseThrow(() -> new ProfilNotFoundException("User not found with authId: " + authId));

        if(request.getFirstname() != null) {
            profil.setFirstname(request.getFirstname());
        }
        if(request.getLastname() != null) {
            profil.setLastname(request.getLastname());
        }
        if(request.getPhone() != null) {
            profil.setPhone(request.getPhone());
        }

        return UserProfileResponse.from(userRepository.save(profil));
    }

    @Transactional
    public UserProfileResponse getUserById(Long id) {
        UserProfile profil = userRepository.findById(id)
                .orElseThrow(() -> new ProfilNotFoundException("User not found with id: " + id));
        return UserProfileResponse.from(profil);
    }


    public List<UserProfile> getAllUsers() {
        log.debug("Fetching all users");
        return userRepository.findAll();
    }


    public Optional<UserProfile> getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    public void deleteUser(Long id) {
        log.debug("Deleting user with id: {}", id);
        
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found with id: " + id);
        }
        
        userRepository.deleteById(id);
        log.info("Successfully deleted user with id: {}", id);
    }

    public boolean existsByAuthId(Long authId) {
        return userRepository.existsByAuthId(authId);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
