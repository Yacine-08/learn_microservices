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

    public static AuthenticationResponse of(String message, String access, String refresh) {
        AuthenticationResponse r = new AuthenticationResponse();
        r.message      = message;
        r.accessToken  = access;
        r.refreshToken = refresh;
        return r;
    }
}
