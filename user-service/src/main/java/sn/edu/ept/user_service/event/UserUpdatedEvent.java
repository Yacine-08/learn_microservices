package sn.edu.ept.user_service.event;

public record UserUpdatedEvent(
        Long authId,
        String firstname,
        String lastname,
        String email,
        String phone,
        String role
) {

}
