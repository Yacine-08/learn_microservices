package sn.edu.ept.user_service.dto;

import lombok.Builder;
import lombok.Data;
import sn.edu.ept.user_service.profile.Role;
import sn.edu.ept.user_service.profile.UserProfile;

import java.time.LocalDateTime;

@Data
@Builder
public class UserProfileResponse {
    private Long   id;
    private Long   authId;
    private String firstname;
    private String lastname;
    private String email;
    private String phone;
    private Role role;
    private LocalDateTime createdAt;

    public static UserProfileResponse from(UserProfile p) {
        return UserProfileResponse.builder()
                .id(p.getId())
                .authId(p.getAuthId())
                .firstname(p.getFirstname())
                .lastname(p.getLastname())
                .email(p.getEmail())
                .phone(p.getPhone())
                .role(p.getRole())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
