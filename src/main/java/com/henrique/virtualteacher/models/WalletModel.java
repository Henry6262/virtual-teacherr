package com.henrique.virtualteacher.models;

import com.henrique.virtualteacher.entities.Wallet;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class WalletModel {

    public WalletModel(Wallet wallet) {
        setId(wallet.getId());
        setUserId(wallet.getOwner().getId());
        setBalance(wallet.getBalance());
    }

    private int id;

    private int userId;

    private BigDecimal balance;

}
