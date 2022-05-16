package com.henrique.virtualteacher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "wallets")
public class Wallet {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Id
    private int id;

    @OneToOne()
    @JoinColumn(name = "user_id")
    private User owner;

    @Column(name = "balance")
    private BigDecimal balance;

    @ManyToMany
    @JsonIgnore
    @JoinTable(name = "transactions",
    joinColumns = @JoinColumn(name = "recipient_wallet_id"),
    inverseJoinColumns = @JoinColumn(name = "id"))
    private List<Transaction> recipientTransactions;

    @ManyToMany
    @JsonIgnore
    @JoinTable(name = "transactions",
    joinColumns = @JoinColumn(name = "sender_wallet_id"),
    inverseJoinColumns = @JoinColumn(name = "id"))
    private List<Transaction> senderTransactions;

    public void addToWallet(BigDecimal amount) {
        balance = balance.add(amount);
    }

    public void retrieveFromWallet(BigDecimal amount) {
        balance = balance.subtract(amount);
    }

    public Wallet(User walletOwner) {
        this.owner = walletOwner;
        this.balance = BigDecimal.ZERO;
    }

}








