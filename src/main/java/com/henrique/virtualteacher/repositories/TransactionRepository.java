package com.henrique.virtualteacher.repositories;

import com.henrique.virtualteacher.entities.Transaction;
import com.henrique.virtualteacher.models.TransactionStatus;
import com.henrique.virtualteacher.models.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    Optional<Transaction> getById(int id);

    List<Transaction> getAllBySenderWalletOwnerId(int userId);

    List<Transaction> getAllByPurchasedCourseId(int courseId);

    List<Transaction> getAllByTransactionType(TransactionType transactionType);

    List<Transaction> getAllByTransactionTypeAndRecipientWalletId(TransactionType transactionType, int recipientWalletId);

    List<Transaction> getAllByTransactionTypeAndSenderWalletId(TransactionType transactionType, int senderWalletId);

    List<Transaction> getAllBySenderWalletId(int senderWalletId);

    List<Transaction> getAllByRecipientWalletId(int recipientWalletId);

    List<Transaction> getAllByRecipientWalletOwnerId(int userId);

    List<Transaction> getAllByStatus(TransactionStatus status);

    List<Transaction> getAllByCreationTimeBetween(LocalDate minDate, LocalDate maxDate);

    List<Transaction> getAllBySenderWalletOwnerIdAndCreationTimeBetween(int userId, LocalDate minDate, LocalDate maxDate);


}
