package sn.edu.ept.user_service.dto;

import lombok.Data;
import sn.edu.ept.user_service.profile.Role;

@Data
public class UpdateProfileRequest {
    private String firstname;
    private String lastname;
    private String phone;
}
