package sn.edu.ept.security.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import sn.edu.ept.security.dtos.UserProfilDTO;

import java.util.List;

// communication via OpenFeign pour recuperer les donnees
@FeignClient(
        name     = "user-service",
        url      = "http://localhost:8071",
        fallback = UserServiceClientFallback.class   // fallback si user-service est down
)
public interface UserServiceClient {

    @PostMapping("/api/users/internal/users")
    List<UserProfilDTO> getProfilsByAuthIds(@RequestBody List<Long> authIds);

    @GetMapping("/api/users/internal/users/{authId}")
    UserProfilDTO getProfilByAuthId(@PathVariable Long authId);
}
