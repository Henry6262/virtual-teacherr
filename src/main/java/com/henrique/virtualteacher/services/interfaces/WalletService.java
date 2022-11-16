package com.henrique.virtualteacher.services.interfaces;

import com.henrique.virtualteacher.entities.*;
import com.henrique.virtualteacher.models.TransactionStatus;
import com.henrique.virtualteacher.models.VerificationTokenModel;

import java.math.BigDecimal;

public interface WalletService {

    Wallet getById(int walletId, User loggedUser);

    Wallet getLoggedUserWallet(User loggedUser);

    Wallet create(User walletOwner);

    void delete(User walletOwner);

    TransactionStatus deposit(User loggedUser, BigDecimal amount);

    void send(User sender, User recipient, BigDecimal amount);

    NFT mintNFT(Course course, User loggedUser);

    void createExchangeRequest(User initiator, BigDecimal offer, NFT nftToBuy); //todo: this can be course for course or money for course;

    void createExchangeRequest(User initiator, NFT courseOffered, NFT courseWanted);

    void verifyPendingDepositOrTransfer(User loggedUser, Transaction transaction, VerificationTokenModel verificationToken);


}
