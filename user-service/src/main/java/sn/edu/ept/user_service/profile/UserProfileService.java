package sn.edu.ept.user_service.profile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.edu.ept.user_service.dto.UpdateProfileRequest;
import sn.edu.ept.user_service.dto.UserProfileResponse;
import sn.edu.ept.user_service.event.UserRegisteredEvent;
import sn.edu.ept.user_service.event.UserUpdatedEvent;
import sn.edu.ept.user_service.event.UserDeletedEvent;
import sn.edu.ept.user_service.event.UserDeletedEventPublisher;
import sn.edu.ept.user_service.exception.ProfilAlreadyExistException;
import sn.edu.ept.user_service.exception.ProfilNotFoundException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileService {

    private final UserProfileRepository userRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final UserDeletedEventPublisher userDeletedEventPublisher;

    private static final String USER_UPDATED_TOPIC = "user-updated";

    @Transactional
    public UserProfile createUser(UserRegisteredEvent event) {

        if (userRepository.existsByAuthId(event.authId())) {
            throw new ProfilAlreadyExistException("User with authId " + event.authId() + " already exists");
        }

        // Validate and provide fallback values for null fields
        String firstname = event.firstname() != null ? event.firstname() : "Unknown";
        String lastname = event.lastname() != null ? event.lastname() : "User";

        var profil = UserProfile.builder()
                .authId(event.authId())
                .firstname(firstname)
                .lastname(lastname)
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

        UserProfile updatedProfile = userRepository.save(profil);

        // Publier l'événement de mise à jour pour auth-service
        UserUpdatedEvent event = new UserUpdatedEvent(
                updatedProfile.getAuthId(),
                updatedProfile.getFirstname(),
                updatedProfile.getLastname(),
                updatedProfile.getEmail(),
                updatedProfile.getPhone(),
                updatedProfile.getRole() != null ? updatedProfile.getRole().name() : null
        );

        kafkaTemplate.send(USER_UPDATED_TOPIC, event);
        log.info("Published user updated event for authId: {}", authId);

        return UserProfileResponse.from(updatedProfile);
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

    @Transactional
    public void deleteUser(Long authId) {
        log.debug("Deleting user with authId: {}", authId);

        UserProfile user = userRepository.findByAuthId(authId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with authId: " + authId));

        // Publish deletion event to auth-service
        UserDeletedEvent deletedEvent = new UserDeletedEvent(
                user.getAuthId(),
                user.getEmail(),
                "Deleted from user-service"
        );
        userDeletedEventPublisher.publishUserDeleted(deletedEvent);

        // Delete user from user-service
        userRepository.deleteByAuthId(authId);
        log.info("Successfully deleted user with authId: {}", authId);
    }

}
