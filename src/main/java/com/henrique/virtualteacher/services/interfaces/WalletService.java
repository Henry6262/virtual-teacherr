package com.henrique.virtualteacher.services.interfaces;

import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.Transaction;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.entities.Wallet;
import com.henrique.virtualteacher.models.VerificationTokenModel;

import java.math.BigDecimal;

public interface WalletService {

    Wallet getById(int walletId, User loggedUser);

    Wallet getLoggedUserWallet(User loggedUser);

    Wallet create(User walletOwner);

    void delete(User walletOwner);

    void deposit(User loggedUser, BigDecimal amount);

    void send(User sender, User recipient, BigDecimal amount);

    void purchaseCourse(Course course, User loggedUser);

    void handleTransactionVerification(User loggedUser, Transaction transaction, VerificationTokenModel verificationToken);

}
