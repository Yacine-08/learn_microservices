package sn.edu.ept.security.event;

public record UserDeletedEvent(
    Long authId,
    String email,
    String reason
) {
    public UserDeletedEvent {
        if (authId == null) {
            throw new IllegalArgumentException("authId cannot be null");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email cannot be null or blank");
        }
    }
}
