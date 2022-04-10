package com.henrique.virtualteacher.services.interfaces;

import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.Transaction;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.entities.Wallet;
import com.henrique.virtualteacher.models.TransactionStatus;

import java.time.LocalDate;
import java.util.List;

public interface TransactionService {

    Transaction getById(int id, User loggedUser);

    List<Transaction> getAllForUser(User loggedUser, int toGetId);

    List<Transaction> getAllForUser(User user,int toGetId, LocalDate minDate, LocalDate maxDate);

    List<Transaction> getAllForCourse(Course course, User loggedUser);

    List<Transaction> getAllByStatus(TransactionStatus status, User loggedUser);

    List<Transaction> getAllByWallet(Wallet wallet, User loggedUser);

    void create(Transaction Transaction, User loggedUser);

    void update(int transactionId, User loggedUser);

    void delete(int transactionId, User loggedUser);


}
