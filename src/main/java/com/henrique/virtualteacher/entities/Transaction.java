package com.henrique.virtualteacher.entities;

import com.henrique.virtualteacher.models.TransactionStatus;
import com.henrique.virtualteacher.models.TransactionType;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "transactions")
public class Transaction {

    private static final int PENDING_STATUS_THRESHOLD = 1500;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "sender_wallet_id")
    private Wallet senderWallet;

    @ManyToOne

    @JoinColumn(name = "recipient_wallet_id")
    private Wallet recipientWallet;

    @ManyToOne
    @JoinColumn(name = "purchased_nft_course_id")
    private NFT purchasedCourse;

    @Column(name = "amount")
    private BigDecimal amount;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "type")
    private TransactionType transactionType;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status")
    private TransactionStatus status;

    @Column(name = "creation_time")
    private LocalDate creationTime;

    /**
     * @Description Constructor for Exchange Transaction
     * @param senderWallet
     * @param offer
     * @param purchasedCourse
     */
    public Transaction (Wallet senderWallet,Wallet recipientWallet , BigDecimal offer, NFT purchasedCourse) {
        this.senderWallet = senderWallet;
        this.recipientWallet = recipientWallet;
        this.amount = offer;
        this.purchasedCourse = purchasedCourse;
        this.creationTime = LocalDate.now();
        this.status = TransactionStatus.PENDING;
        initializeTransactionType(TransactionType.EXCHANGE);
    }

    /**
     * @Description Constructor for Purchase Transaction
     * @param senderWallet
     * @param purchasedCourse
     */
    public Transaction (Wallet senderWallet,Wallet recipientWallet , NFT purchasedCourse) {
        this.senderWallet = senderWallet;
        this.recipientWallet = recipientWallet;
        this.amount = purchasedCourse.getCourse().getMintPrice();
        this.purchasedCourse = purchasedCourse;
        this.creationTime = LocalDate.now();
        this.status = TransactionStatus.COMPLETED;
        initializeTransactionType(TransactionType.PURCHASE);
    }

    /**
     * @Description Constructor for Transfer transactions (wallet to wallet)
     * @param senderWallet
     * @param recipientWallet
     * @param amount
     */

    public Transaction (Wallet senderWallet, Wallet recipientWallet, BigDecimal amount) {
        this.senderWallet = senderWallet;
        this.recipientWallet = recipientWallet;
        this.amount = amount;
        this.purchasedCourse = null;
        this.creationTime = LocalDate.now();
        setStatus();
        initializeTransactionType(TransactionType.TRANSFER);
    }

    /**
     * @Description constructor for deposit transactions
     * @param depositorWallet
     * @param amount
     */
    public Transaction (Wallet depositorWallet, BigDecimal amount) {
        this.senderWallet = depositorWallet;
        this.recipientWallet = depositorWallet;
        this.amount = amount;
        this.purchasedCourse = null;
        this.creationTime = LocalDate.now();
        setStatus();
        initializeTransactionType(TransactionType.DEPOSIT);
    }

    private void setStatus() {
        if (this.amount != null) {
            checkAmountIsAboveThreshold();
        }
    }

    private void checkAmountIsAboveThreshold() {
        if (this.amount.doubleValue() > PENDING_STATUS_THRESHOLD) {
            status = TransactionStatus.PENDING;
        }else{
            status = TransactionStatus.COMPLETED;
        }
    }

    public void completeTransaction() {
        this.status = TransactionStatus.COMPLETED;
    }

    public boolean isDeposit() {  // user adds funds to wallet -> sender == recipient
        return getTransactionType() == TransactionType.DEPOSIT;
    }

    public boolean isExchange() { // money for course or the other way around
        return getTransactionType() == TransactionType.EXCHANGE;
    }

    public boolean isTransfer() { // user sends money to another user, not expecting anything back.
        return getTransactionType() == TransactionType.TRANSFER;
    }

    public boolean isPurchase() { //when user has minted a course
        return getTransactionType() == TransactionType.PURCHASE;
    }

    private void initializeTransactionType(TransactionType type) {
        this.transactionType = type;
    }



}
