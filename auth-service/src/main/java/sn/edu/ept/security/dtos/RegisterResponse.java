package sn.edu.ept.security.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterResponse {
    private String message;
    private Long authId;

    public static RegisterResponse of(String message, Long authId) {
        RegisterResponse r = new RegisterResponse();
        r.message      = message;
        r.authId       = authId;
        return r;
    }
}
