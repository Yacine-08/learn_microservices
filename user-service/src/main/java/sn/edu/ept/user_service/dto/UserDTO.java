package sn.edu.ept.user_service.dto;

import lombok.Builder;
import lombok.Data;
import sn.edu.ept.user_service.profile.Role;

import java.time.LocalDateTime;

@Data
@Builder
public class UserDTO {
    private Long authId;
    private String email;
    private String firstname;
    private String lastname;
    private String phone;
    private String role;
    private LocalDateTime createdAt;
}
