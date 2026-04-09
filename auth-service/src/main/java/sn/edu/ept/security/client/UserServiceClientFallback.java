package sn.edu.ept.security.client;

import org.springframework.stereotype.Component;
import sn.edu.ept.security.dtos.UserProfilDTO;

import java.util.List;

@Component
public class UserServiceClientFallback implements UserServiceClient{
    @Override
    public List<UserProfilDTO> getProfilsByAuthIds(List<Long> authIds) {
        return List.of();
    }

    @Override
    public UserProfilDTO getProfilByAuthId(Long authId) {
        return null;
    }
}
