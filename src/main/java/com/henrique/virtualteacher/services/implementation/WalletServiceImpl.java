package com.henrique.virtualteacher.services.implementation;

import com.henrique.virtualteacher.entities.*;
import com.henrique.virtualteacher.exceptions.EntityNotFoundException;
import com.henrique.virtualteacher.exceptions.ImpossibleOperationException;
import com.henrique.virtualteacher.exceptions.UnauthorizedOperationException;
import com.henrique.virtualteacher.models.TransactionStatus;
import com.henrique.virtualteacher.models.VerificationTokenModel;
import com.henrique.virtualteacher.repositories.WalletRepository;
import com.henrique.virtualteacher.services.interfaces.TransactionService;
import com.henrique.virtualteacher.services.interfaces.WalletService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class WalletServiceImpl implements WalletService{

    private final WalletRepository walletRepository;
    private final TransactionService transactionService;
    private final Logger logger;


    @Override
    public Wallet getById(int walletId, User loggedUser) {

        Wallet wallet = walletRepository.getById(walletId)
                .orElseThrow(() -> new EntityNotFoundException("Wallet", "id", String.valueOf(walletId)));

        checkWalletExists(wallet, loggedUser);
        checkUserIsWalletOwner(loggedUser, wallet);

        return wallet;
    }

    @Override
    public Wallet getLoggedUserWallet(User loggedUser) {

        Wallet wallet =  walletRepository.getByOwnerEmail(loggedUser.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("Wallet", "Owner", loggedUser.getEmail()));

        checkWalletExists(wallet, loggedUser);
        checkUserIsWalletOwner(loggedUser, wallet);

        return wallet;
    }

    @Override
    public Wallet create(User userToCreateWalletFor) {

        checkUserDoesNotHaveWallet(userToCreateWalletFor);

        Wallet newWallet = walletRepository.save(new Wallet(userToCreateWalletFor));
        logger.info("Wallet for user with id: %d, has been created");
        return newWallet;
    }

    @Override
    public void delete(User walletOwner) {
        //todo: delete wallet only when user decides to close account
    }

    @Override
    public void handleTransactionVerification(User loggedUser, Transaction transaction, VerificationTokenModel verificationToken) {

        if (transaction.isDeposit()) {
            verifyPendingDeposit(loggedUser, verificationToken);
        } else {
            verifyPendingSendTransaction(loggedUser, verificationToken);
        }
        //todo: test
    }

    @Override
    public void deposit(User loggedUser, BigDecimal amount) {
        verifyDepositAmountIsAboveMinimum(amount);

        Wallet userWallet = getLoggedUserWallet(loggedUser);
        TransactionStatus status = prepareDepositTransaction(userWallet, amount);

        if (status.equals(TransactionStatus.PENDING)) {
            return; }

        addFundsToUserWallet(userWallet, amount);
    }

    public void send(User sender, User recipient, BigDecimal amount) {
        Wallet senderWallet = sender.getWallet();
        Wallet recipientWallet = recipient.getWallet();
        TransactionStatus status = prepareTransactionBetweenUsers(senderWallet, recipientWallet, amount);

        if (status.equals(TransactionStatus.PENDING)) {
            return; }                                       //for verification verifyPendingSendTransaction method will be used

        retrieveFromWallets(senderWallet, recipientWallet, amount);
        //todo: test
    }

    @Override
    public void makePurchase(Course course, User loggedUser) {

        Wallet userWallet = getLoggedUserWallet(loggedUser);

        checkUserWalletHasEnoughFunds(course.getPrice(), userWallet);
        userWallet.retrieveFromWallet(course.getPrice());

        logger.info(String.format("User with id: %d, has successfully purchased course with id %d",loggedUser.getId(), course.getId()));
    }

    private void verifyPendingSendTransaction(User loggedUser, VerificationTokenModel tokenModel) {

        if (loggedUser.getId() != tokenModel.getVerifierId()) {
            throw new UnauthorizedOperationException(String.format("User with id: %d, is not authorized to verify transaction with id: %d", loggedUser.getId(), tokenModel.getTransactionId()));
        }

        Transaction pendingTransaction = transactionService.getById(tokenModel.getTransactionId(), loggedUser);
        pendingTransaction.completeTransaction();

        Wallet senderWallet = pendingTransaction.getSenderWallet();
        Wallet recipientWallet = pendingTransaction.getRecipientWallet();
        retrieveFromWallets(senderWallet, recipientWallet, pendingTransaction.getAmount());
        // todo : test
    }

    private void retrieveFromWallets(Wallet senderWallet, Wallet recipientWallet, BigDecimal amount) {
        senderWallet.retrieveFromWallet(amount);
        recipientWallet.retrieveFromWallet(amount);
        updateWallet(senderWallet);
        updateWallet(recipientWallet);
    }

    private void verifyPendingDeposit(User loggedUser, VerificationTokenModel tokenModel) {

        if (loggedUser.getId() != tokenModel.getVerifierId()) {
            throw new UnauthorizedOperationException(String.format("User with id: %d, is not authorized to verify deposit requests of other users", loggedUser.getId()));
        }

        Wallet loggedUserWallet = getLoggedUserWallet(loggedUser);
        Transaction pendingTransaction = transactionService.getById(tokenModel.getTransactionId(), loggedUser);

        pendingTransaction.completeTransaction();
        addFundsToUserWallet(loggedUserWallet, pendingTransaction.getAmount());
        //TODO: TEST
    }

    private void updateWallet(Wallet walletToUpdate) {
        walletRepository.save(walletToUpdate);
    }

    private void addFundsToUserWallet(Wallet userWallet, BigDecimal amount) {
        userWallet.addToWallet(amount);

        walletRepository.save(userWallet);
        logger.info(String.format("User with id %d, has successfully added %f", userWallet.getOwner().getId(), amount.doubleValue()));
    }

    private void verifyDepositAmountIsAboveMinimum(BigDecimal amount) {
        if (amount.doubleValue() < 10.00) {
            throw new ImpossibleOperationException("Minimum Deposit is 10");
        }
    }

    private TransactionStatus prepareTransactionBetweenUsers(Wallet senderWallet, Wallet recipientWallet, BigDecimal transactionAmount) {
        checkUserWalletHasEnoughFunds(transactionAmount, senderWallet);
        Transaction transaction = new Transaction(senderWallet, recipientWallet, transactionAmount);
        transactionService.create(transaction, senderWallet.getOwner());
        return transaction.getStatus();
    }

    private TransactionStatus prepareDepositTransaction(Wallet userWallet, BigDecimal amount) {
        Transaction transaction = new Transaction(userWallet, amount);
        transactionService.create(transaction, userWallet.getOwner());
        return transaction.getStatus();
    }

    private void checkUserWalletHasEnoughFunds(BigDecimal amount, Wallet userWallet) {

        BigDecimal walletBalance = userWallet.getBalance();

        if (walletBalance.doubleValue() < amount.doubleValue()) {
            throw new ImpossibleOperationException(String.format
                    ("Wallet of user with id: %d, does not have sufficient funds %f", userWallet.getOwner().getId(), amount.doubleValue()));
        }
    }

    private void checkUserDoesNotHaveWallet(User user){

        Wallet userWallet;
        try{
            userWallet =  getLoggedUserWallet(user);
            throw new ImpossibleOperationException(String.format("User with id: %d, already has a wallet", user.getId()));
        } catch (EntityNotFoundException ignored) {}
    }

    private void checkWalletExists(Wallet wallet, User loggedUser) {
        if (wallet == null) {
            throw new EntityNotFoundException(String.format("User with id: %d: does not have a wallet", loggedUser.getId()));
        }
    }

    private void checkUserIsWalletOwner(User user, Wallet wallet) {
        if (user.getId() != wallet.getOwner().getId()) {
            throw new UnauthorizedOperationException(String.format("User with id: %d, is not the owner of wallet with id: %d", user.getId(), wallet.getId()));
        }
    }

}
