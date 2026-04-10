package sn.edu.ept.security.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdatedEvent {
    private Long authId;
    private String email;
    private String firstname;
    private String lastname;
    private String phone;
    private String role;
}
