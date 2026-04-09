package sn.edu.ept.security.dtos;

import lombok.Data;

@Data
public class UserProfilDTO {
    private Long   authId;
    private String firstname;
    private String lastname;
    private String phone;
}
