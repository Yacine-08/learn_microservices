package sn.edu.ept.security.dtos;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserDTO {

    private Long   authId;
    private String firstname;
    private String lastname;
    private String phone;
    private String email;
    private String role;
    private LocalDateTime createdAt;
}


