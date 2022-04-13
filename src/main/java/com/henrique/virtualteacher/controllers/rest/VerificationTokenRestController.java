package com.henrique.virtualteacher.controllers.rest;

import com.henrique.virtualteacher.entities.Transaction;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.models.VerificationTokenModel;
import com.henrique.virtualteacher.services.interfaces.TransactionService;
import com.henrique.virtualteacher.services.interfaces.UserService;
import com.henrique.virtualteacher.services.interfaces.VerificationTokenService;
import com.henrique.virtualteacher.services.interfaces.WalletService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@AllArgsConstructor
@RequestMapping("api/auth/verify")
public class VerificationTokenRestController {

    private final VerificationTokenService tokenService;
    private final UserService userService;
    private final TransactionService transactionService;
    private final WalletService walletService;

    @GetMapping
    public ResponseEntity<HttpStatus> claimToken(Principal principal,
                                                 @RequestParam("token") String token) {


        VerificationTokenModel verificationToken = tokenService.getVerificationToken(token);

        if (principal == null) {     //no user is logged in
            userService.enableUser(verificationToken.getVerifierId());

        } else {
            User loggedUser = userService.getLoggedUser(principal);
            Transaction transaction = transactionService.getById(verificationToken.getTransactionId(), loggedUser);
            walletService.handleTransactionVerification(loggedUser, transaction, verificationToken);
        }
        tokenService.delete(verificationToken);

        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

}
