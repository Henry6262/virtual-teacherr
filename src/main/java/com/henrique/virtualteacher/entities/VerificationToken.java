package com.henrique.virtualteacher.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "verification_tokens")
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "verifier_id")
    private User verifier;

    @OneToOne
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    @Column(name = "token")
    private String token;

    @Column(name = "expiration_time")
    private LocalDateTime expirationTime;

    public VerificationToken(User verifier) {
        this.verifier = verifier;
        this.token = UUID.randomUUID().toString();
        this.expirationTime = LocalDateTime.now().plusMinutes(10);
    }

    public VerificationToken(Transaction transaction) {
        this.verifier = transaction.getSenderWallet().getOwner();
        this.token = UUID.randomUUID().toString();
        this.transaction = transaction;
        this.expirationTime = LocalDateTime.now().plusMinutes(10);
    }
}
