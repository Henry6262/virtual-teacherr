package com.henrique.virtualteacher.services.interfaces;

import com.henrique.virtualteacher.entities.*;
import com.henrique.virtualteacher.models.TransactionModel;
import com.henrique.virtualteacher.models.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionService {

    Transaction getById(int id, User loggedUser);

    Page<Transaction> getWalletTransactionPage(Pageable pageable, int recipientWalletId);

    List<Transaction> getAllForUser(User loggedUser, int toGetId);

    List<Transaction> getAllForUser(User user,int toGetId, LocalDate minDate, LocalDate maxDate);

    List<Transaction> getAllForCourse(Course course, User loggedUser);

    List<Transaction> getAllByStatus(TransactionStatus status, User loggedUser);

    List<Transaction> getAllByWallet(Wallet wallet, User loggedUser);

    void createExchangeTransaction(Wallet initiatorWallet, Wallet ownerWallet, BigDecimal offer, NFT mintedCourse);

    void create(Transaction Transaction, User loggedUser);

    void update(int transactionId, TransactionModel model, User loggedUser);

    void delete(int transactionId, User loggedUser);


}
