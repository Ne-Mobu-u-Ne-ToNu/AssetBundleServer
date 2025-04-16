package com.usachevsergey.AssetBundleServer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByUsername(String username);
    Optional<User> findUserByEmail(String email);
    Optional<User> findUserByApiKey(String apiKey);
    Boolean existsUserByUsername(String username);
    Boolean existsUserByEmail(String email);
}
