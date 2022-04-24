package com.henrique.virtualteacher.entities;

import com.henrique.virtualteacher.models.TransactionStatus;
import com.henrique.virtualteacher.models.TransactionType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "transactions")
public class Transaction {

    private static final int PENDING_STATUS_THRESHOLD = 150;

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
    @JoinColumn(name = "purchased_course_id")
    private Course purchasedCourse;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "type")
    private TransactionType transactionType;

    @Column(name = "pending")
    private TransactionStatus status;

    @Column(name = "creation_time")
    private LocalDate creationTime;


    public Transaction (Wallet senderWallet, Wallet recipientWallet, Course purchasedCourse) {
        this.senderWallet = senderWallet;
        this.recipientWallet = recipientWallet;
        this.amount = purchasedCourse.getPrice();
        this.purchasedCourse = purchasedCourse;
        this.creationTime = LocalDate.now();
        setStatus(amount);
    }

    public Transaction (Wallet senderWallet, Wallet recipientWallet, BigDecimal amount) {
        this.senderWallet = senderWallet;
        this.recipientWallet = recipientWallet;
        this.amount = amount;
        this.purchasedCourse = null;
        this.creationTime = LocalDate.now();
        setStatus(amount);
    }


    public Transaction (Wallet depositorWallet, BigDecimal amount) {
        this.senderWallet = depositorWallet;
        this.recipientWallet = depositorWallet;
        this.amount = amount;
        this.purchasedCourse = null;
        this.creationTime = LocalDate.now();
        setStatus(amount);
    }

    private void setStatus(BigDecimal amount) {
        if (amount.doubleValue() > PENDING_STATUS_THRESHOLD) {
            this.status = TransactionStatus.PENDING;
        }
        else {
            this.status = TransactionStatus.COMPLETED;
        }
    }

    public void completeTransaction() {
        this.status = TransactionStatus.COMPLETED;
    }

    public boolean isDeposit() {
        return getRecipientWallet().getId() == getSenderWallet().getId();
    }

    public boolean isBetweenUsers() {
        return getRecipientWallet().getOwner().getId() == getSenderWallet().getOwner().getId();
    }

}
