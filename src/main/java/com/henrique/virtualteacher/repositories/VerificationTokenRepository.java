package com.henrique.virtualteacher.repositories;

import com.henrique.virtualteacher.entities.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Integer> {

    Optional<VerificationToken> findById(int id);

    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByTransactionId(int transactionId);

    List<VerificationToken> findAllByVerifierId(int verifierId);

}
