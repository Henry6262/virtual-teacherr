package com.henrique.virtualteacher.models;

import com.henrique.virtualteacher.entities.NFT;
import com.henrique.virtualteacher.entities.Wallet;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private NFT purchasedCourse;

   @NotBlank
    private BigDecimal amount;

    @NotBlank
    private TransactionStatus status;

    private TransactionType type;

   @NotBlank
    private LocalDate creationTime;


    public TransactionModel(Wallet senderWallet, Wallet recipientWallet, NFT purchasedCourse) {
        this.senderWallet = senderWallet;
        this.recipientWallet = recipientWallet;
        this.amount = purchasedCourse.getCourse().getMintPrice();
        this.purchasedCourse = purchasedCourse;
        this.creationTime = LocalDate.now();
    }

}
