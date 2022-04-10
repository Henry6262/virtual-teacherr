package com.henrique.virtualteacher.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class WalletModel {

    private int id;

    private int userId;

    private BigDecimal balance;

}
