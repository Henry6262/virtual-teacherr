package com.henrique.virtualteacher.services.implementation;

import com.henrique.virtualteacher.entities.*;
import com.henrique.virtualteacher.exceptions.DuplicateEntityException;
import com.henrique.virtualteacher.exceptions.EntityNotFoundException;
import com.henrique.virtualteacher.exceptions.ImpossibleOperationException;
import com.henrique.virtualteacher.exceptions.UnauthorizedOperationException;
import com.henrique.virtualteacher.models.TransactionStatus;
import com.henrique.virtualteacher.models.VerificationTokenModel;
import com.henrique.virtualteacher.repositories.WalletRepository;
import com.henrique.virtualteacher.services.interfaces.NFTCourseService;
import com.henrique.virtualteacher.services.interfaces.TransactionService;
import com.henrique.virtualteacher.services.interfaces.WalletService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class WalletServiceImpl implements WalletService{

    private final WalletRepository walletRepository;
    private final TransactionService transactionService;
    private final NFTCourseService nftCourseService;
    private final Logger logger;


    @Override
    public Wallet getById(int walletId, User loggedUser) {

        Wallet wallet = walletRepository.getById(walletId)
                .orElseThrow(() -> new EntityNotFoundException("Wallet", "id", String.valueOf(walletId)));

        checkUserIsWalletOwner(loggedUser, wallet);
        return wallet;
    }

    @Override
    public Wallet getLoggedUserWallet(User loggedUser) {

        Wallet wallet =  walletRepository.getByOwnerEmail(loggedUser.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("Wallet", "Owner", loggedUser.getEmail()));

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
    public void verifyPendingDepositOrTransfer(User loggedUser, Transaction transaction, VerificationTokenModel verificationToken) {

        if (transaction.isDeposit()) {
            verifyPendingDeposit(loggedUser, verificationToken);
        }
        else if (transaction.isTransfer()) {
            verifyPendingSendTransaction(loggedUser, verificationToken);
        }
    }

    @Override
    @Transactional
    public TransactionStatus deposit(User loggedUser, BigDecimal amount) {
        verifyDepositAmountIsAboveMinimum(amount);

        Wallet userWallet = getLoggedUserWallet(loggedUser);
        TransactionStatus status = prepareDepositTransaction(userWallet, amount);

        if (status.equals(TransactionStatus.PENDING)) {
            return TransactionStatus.PENDING;
        }

        addFundsToUserWallet(userWallet, amount);
        return TransactionStatus.COMPLETED;
    }

    private void checkSenderIsNotRecipient(User sender, User recipient) {
        if (sender.getEmail().equals(recipient.getEmail())) {
            throw new ImpossibleOperationException(String.format("User with email: %s, cannot send money to himself !!", sender.getEmail()));
        }
    }

    @Override
    public void send(User sender, User recipient, BigDecimal amount) {

        checkSenderIsNotRecipient(sender, recipient);
        Wallet senderWallet = getLoggedUserWallet(sender);
        Wallet recipientWallet = getLoggedUserWallet(recipient);
        Transaction transaction = prepareTransactionBetweenUsers(senderWallet, recipientWallet, amount);

        if (transaction.getStatus().equals(TransactionStatus.PENDING)) {
            return; }                                       //for verification verifyPendingSendTransaction method will be used

        retrieveFromWallets(transaction);
    }

    @Override
    public NFT purchaseCourse(Course course, User loggedUser) {

        Wallet userWallet = getLoggedUserWallet(loggedUser);

        checkUserWalletHasEnoughFunds(course.getPrice(), userWallet);
        userWallet.retrieveFromWallet(course.getPrice());
        NFT nft = nftCourseService.purchase(loggedUser, course);

        logger.info(String.format("User with id: %d, has successfully purchased course with id %d",loggedUser.getId(), course.getId()));
        return nft;
    }

    @Override
    public void createExchangeRequest(User initiator, BigDecimal offer, NFT wantedMintedCourse) {

        Wallet initiatorWallet = getLoggedUserWallet(initiator);
        Wallet nftOwnerWallet = getLoggedUserWallet(wantedMintedCourse.getOwner());
        checkUserWalletHasEnoughFunds(offer, nftOwnerWallet);

        transactionService.createExchangeTransaction(initiatorWallet, nftOwnerWallet,offer, wantedMintedCourse);
    }

    @Override
    public void createExchangeRequest(User initiatorW, NFT courseOffered, NFT courseWanted) {
        //todo;
    }

    public void handleExchangeRequestResponse(User loggedUser, TransactionStatus response, Transaction transaction) {

        if (loggedUser.getId() != transaction.getPurchasedCourse().getOwner().getId())

        if (response == TransactionStatus.REJECTED) {
            transaction.setStatus(TransactionStatus.REJECTED);
            return;
        }

        Wallet buyerWallet = transaction.getSenderWallet();
        Wallet sellerWallet = transaction.getRecipientWallet();
        NFT boughtCourse = transaction.getPurchasedCourse();

        transaction.setStatus(TransactionStatus.COMPLETED);
        boughtCourse.setOwner(buyerWallet.getOwner());
        retrieveFromWallets(transaction);
    }


    private void verifyPendingSendTransaction(User loggedUser, VerificationTokenModel tokenModel) {

        if (loggedUser.getId() != tokenModel.getVerifierId()) {
            throw new UnauthorizedOperationException(String.format("User with id: %d, is not authorized to verify transaction with id: %d", loggedUser.getId(), tokenModel.getTransactionId()));
        }

        Transaction pendingTransaction = transactionService.getById(tokenModel.getTransactionId(), loggedUser);
        pendingTransaction.completeTransaction();

        Wallet senderWallet = pendingTransaction.getSenderWallet();
        Wallet recipientWallet = pendingTransaction.getRecipientWallet();
        retrieveFromWallets(pendingTransaction);
    }

    private void retrieveFromWallets(Transaction transaction) {
        BigDecimal amount = transaction.getAmount();
        Wallet senderWallet = transaction.getSenderWallet();
        Wallet recipientWallet = transaction.getRecipientWallet();
        senderWallet.retrieveFromWallet(amount);
        recipientWallet.addToWallet(amount);
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


    private Transaction prepareTransactionBetweenUsers(Wallet senderWallet, Wallet recipientWallet, BigDecimal transactionAmount) {
        checkUserWalletHasEnoughFunds(transactionAmount, senderWallet);
        Transaction transaction = new Transaction(senderWallet, recipientWallet, transactionAmount);
        transactionService.create(transaction, senderWallet.getOwner());
        return transaction;
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
            throw new DuplicateEntityException(String.format("User with id: %d, already has a wallet", user.getId()));
        } catch (EntityNotFoundException ignored) {}
    }

    private void checkUserIsWalletOwner(User user, Wallet wallet) {
        if (user.getId() != wallet.getOwner().getId()) {
            throw new UnauthorizedOperationException(String.format("User with id: %d, is not the owner of wallet with id: %d", user.getId(), wallet.getId()));
        }
    }

}
