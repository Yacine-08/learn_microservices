package sn.edu.ept.security.event;


public record UserRegisteredEvent(
        Long authId,
        String firstname,
        String lastname,
        String email,
        String phone,
        String role
) {

}
