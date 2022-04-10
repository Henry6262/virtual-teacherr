package com.henrique.virtualteacher.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class VerificationTokenModel {

    @NotBlank
    private int id;

    @NotBlank
    private int verifierId;

    @NotBlank
    private int transactionId;

    @NotBlank
    private String token;

    @NotBlank
    private LocalDateTime expirationTime;

}
