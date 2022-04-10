package com.henrique.virtualteacher.models;

import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.Wallet;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class TransactionModel {

    @NotBlank
    private Wallet senderWallet;

    @NotBlank
    private Wallet recipientWallet;

   @NotBlank
    private Course purchasedCourse;

   @NotBlank
    private BigDecimal amount;

    @NotBlank
    private TransactionStatus status;

   @NotBlank
    private LocalDate creationTime;


    public TransactionModel(Wallet senderWallet, Wallet recipientWallet, Course purchasedCourse) {
        this.senderWallet = senderWallet;
        this.recipientWallet = recipientWallet;
        this.amount = purchasedCourse.getPrice();
        this.purchasedCourse = purchasedCourse;
        this.creationTime = LocalDate.now();
    }

}
