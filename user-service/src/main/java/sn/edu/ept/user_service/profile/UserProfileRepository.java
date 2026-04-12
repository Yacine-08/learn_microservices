package sn.edu.ept.user_service.profile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    
    Optional<UserProfile> findByAuthId(Long authId);
    
    boolean existsByAuthId(Long authId);

    List<UserProfile> findAllByAuthIdIn(List<Long> authIds);

    void deleteByAuthId(Long authId);
}
