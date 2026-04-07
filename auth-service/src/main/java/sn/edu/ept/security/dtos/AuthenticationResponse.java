package sn.edu.ept.security.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {
    public String  message;
    public String  accessToken;
    public String  refreshToken;
    public String  role;
    public Long    authId;

    public static AuthenticationResponse of(String message, String access, String refresh,
                                  String role, Long authId) {
        AuthenticationResponse r = new AuthenticationResponse();
        r.message      = message;
        r.accessToken  = access;
        r.refreshToken = refresh;
        r.role         = role;
        r.authId       = authId;
        return r;
    }
}
