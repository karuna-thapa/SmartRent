package com.springbootapp.fyp.smartrent.repository;

import com.springbootapp.fyp.smartrent.model.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    Optional<OtpToken> findTopByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(
            String email, OtpToken.OtpType type);
}
