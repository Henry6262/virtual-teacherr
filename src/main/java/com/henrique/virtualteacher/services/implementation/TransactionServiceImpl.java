package com.henrique.virtualteacher.services.implementation;

import com.henrique.virtualteacher.entities.*;
import com.henrique.virtualteacher.exceptions.EntityNotFoundException;
import com.henrique.virtualteacher.exceptions.UnauthorizedOperationException;
import com.henrique.virtualteacher.models.TransactionModel;
import com.henrique.virtualteacher.models.TransactionStatus;
import com.henrique.virtualteacher.models.TransactionType;
import com.henrique.virtualteacher.repositories.TransactionRepository;
import com.henrique.virtualteacher.services.interfaces.TransactionService;
import com.henrique.virtualteacher.services.interfaces.UserService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final Logger logger;


    @Override
    public Transaction getById(int id, User loggedUser) {

        Transaction transaction = transactionRepository.getById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Transaction with id: %d, does not exist", id)));

        checkUserIsAuthorized(loggedUser, transaction);
        return transaction;
    }

    @Override
    public Page<Transaction> getWalletTransactionPage(Pageable pageable, int walletId) {
        return transactionRepository.findAllByRecipientWalletIdOrSenderWalletIdOrderByCreationTimeDesc(walletId, walletId, pageable);
    }

    private void checkUserIsAuthorized(User loggedUser, Transaction transaction) {

        User transactionRecipient = transaction.getRecipientWallet().getOwner();
        User transactionSender = transaction.getSenderWallet().getOwner();

        if (transactionRecipient.getId() != loggedUser.getId() && transactionSender.getId() != loggedUser.getId())
        if (loggedUser.isNotTeacherOrAdmin()) {
            throw new UnauthorizedOperationException(String.format("User with id: %d, is not authorized to access Transaction with id: %d", loggedUser.getId(), transaction.getId()));
        }
    }

    @Override
    public List<Transaction> getAllForUser(User loggedUser, int toGetId) {

        User userToGet = userService.getById(toGetId, loggedUser);
        checkUserIsAuthorized(loggedUser, toGetId);

        return transactionRepository.getAllBySenderWalletOwnerId(loggedUser.getId()); //fixme wrong
    }

    private void checkUserIsAuthorized(User loggedUser, int toGetId) {
        if (loggedUser.getId() != toGetId && loggedUser.isNotTeacherOrAdmin()) {
            throw new UnauthorizedOperationException(String.format("User with id: %d, is not authorized to access transactions of user with id: %d", loggedUser.getId(), toGetId));
        }
    }

    private void checkUserIsAuthorized(User loggedUser, Course course) {
        if (loggedUser.getId() != course.getCreator().getId() && loggedUser.isNotTeacherOrAdmin()) {
            throw new UnauthorizedOperationException(String.format("User with id: %d, is not authorized to access transactions for course with id: %d", loggedUser.getId(), course.getId()));
        }
    }

    @Override
    public List<Transaction> getAllForUser(User user, int toGetId, LocalDate minDate, LocalDate maxDate) {
        User userToGet = userService.getById(toGetId, user);

        checkUserIsAuthorized(user, toGetId);
        return transactionRepository.getAllBySenderWalletOwnerIdAndCreationTimeBetween(toGetId, minDate, maxDate);
    }

    @Override
    public List<Transaction> getAllForCourse(Course course, User loggedUser) {
        checkUserIsAuthorized(loggedUser, course);
        return transactionRepository.getAllByPurchasedCourseId(course.getId());
    }

    @Override
    public List<Transaction> getAllByStatus(TransactionStatus status, User loggedUser) {
        if (loggedUser.isNotTeacherOrAdmin()) {
            throw new UnauthorizedOperationException(String.format("User with id: %d, is not teacher or admin", loggedUser.getId()));
        }
        return transactionRepository.getAllByStatus(status);
    }

    @Override
    public List<Transaction> getAllByWallet(Wallet wallet, User loggedUser) {
        checkUserIsAuthorized(loggedUser, wallet.getOwner().getId());
        return transactionRepository.getAllByRecipientWalletIdOrSenderWalletId(wallet.getId(), wallet.getId());
    }


    @Override
    public void create(Transaction transaction, User loggedUser) {

        checkUserHasAccessToTransaction(loggedUser, transaction);
        Transaction createdTransaction = transactionRepository.save(transaction);
        logger.info(String.format("Transaction with id: %d, has been created successfully by user with id: %d", createdTransaction.getId(),loggedUser.getId()));
    }

    @Override
    public void createExchangeTransaction(Wallet initiatorWallet, Wallet ownerWallet,BigDecimal offer, NFT courseToMakeOffer) {

        Transaction transaction = new Transaction(initiatorWallet, ownerWallet, offer, courseToMakeOffer);
        transaction.setTransactionType(TransactionType.EXCHANGE);
        Transaction createdTransaction = transactionRepository.save(transaction);
    }

    private void checkUserHasAccessToTransaction(User loggedUser, Transaction transaction) {
        User sender = userService.getByEmail(transaction.getSenderWallet().getOwner().getEmail());
        User recipient = userService.getByEmail(transaction.getRecipientWallet().getOwner().getEmail());

        if (loggedUser.getId() != sender.getId() && loggedUser.getId() != recipient.getId()){
            if (!loggedUser.isAdmin()) {
                throw new UnauthorizedOperationException(String.format("User with id: %d, is not authorized to access transaction with id: %d", loggedUser.getId(), transaction.getId()));
            }
        }
    }

    @Override
    public void update(int transactionId, TransactionModel transactionModel, User loggedUser) {
        Transaction toUpdate = getById(transactionId, loggedUser);
        if (!loggedUser.isAdmin()){
            throw new UnauthorizedOperationException(String.format("User with id: %d, is not authorized to delete transaction with id: %d", loggedUser.getId(), transactionId));
        }

        mapModelToEntity(transactionModel, toUpdate);
        transactionRepository.save(toUpdate);
        logger.info(String.format("User with id: %d, has successfully updated transaction with id; %d", loggedUser.getId(), transactionId));
    }

    private void mapModelToEntity(TransactionModel model, Transaction transaction) {
        transaction.setRecipientWallet(model.getRecipientWallet());
        transaction.setSenderWallet(model.getSenderWallet());
        transaction.setCreationTime(model.getCreationTime());
        transaction.setPurchasedCourse(model.getPurchasedCourse());
        transaction.setAmount(model.getAmount());
    }

    @Override
    public void delete(int transactionId, User loggedUser) {
        Transaction toDelete = getById(transactionId, loggedUser);
        if (!loggedUser.isAdmin()){
            throw new UnauthorizedOperationException(String.format("User with id: %d, is not authorized to delete transaction with id: %d", loggedUser.getId(), transactionId));
        }
        transactionRepository.delete(toDelete);
        logger.info(String.format("Transaction with id: %d, has been deleted from the database", transactionId));
    }
}
