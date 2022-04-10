package com.henrique.virtualteacher.repositories;

import com.henrique.virtualteacher.entities.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Integer> {

    Optional<Wallet> getById(int id);

    Optional<Wallet> getByOwnerId(int id);

    Optional<Wallet> getByOwnerEmail(String email);

}
