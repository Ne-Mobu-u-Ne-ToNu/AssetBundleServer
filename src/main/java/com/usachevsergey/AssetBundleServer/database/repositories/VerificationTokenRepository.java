package com.usachevsergey.AssetBundleServer.database.repositories;

import com.usachevsergey.AssetBundleServer.database.enumerations.TokenType;
import com.usachevsergey.AssetBundleServer.database.tables.User;
import com.usachevsergey.AssetBundleServer.database.tables.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByTokenAndType(String token, TokenType type);
    VerificationToken findByUser(User user);
}
