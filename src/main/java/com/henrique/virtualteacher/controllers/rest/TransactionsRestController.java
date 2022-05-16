package com.henrique.virtualteacher.controllers.rest;

import com.henrique.virtualteacher.entities.Transaction;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.entities.Wallet;
import com.henrique.virtualteacher.services.interfaces.TransactionService;
import com.henrique.virtualteacher.services.interfaces.UserService;
import com.henrique.virtualteacher.services.interfaces.WalletService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/transactions")
@AllArgsConstructor
public class TransactionsRestController {

    private final TransactionService transactionService;
    private final UserService userService;
    private final WalletService walletService;


//    @GetMapping("/my-wallet")
//    public Page<Transaction> getWalletTransactionsPage(@RequestParam("size")int size,
//                                                       @RequestParam("page") int page,
//                                                       Principal principal) {
//
//        User loggedUser = userService.getLoggedUser(principal);
//        Wallet loggedUserWallet = walletService.getLoggedUserWallet(loggedUser);
//        Pageable pageable = PageRequest.of(0, size);
//
//        Page<Transaction> transactionPage = transactionService.getWalletTransactionPage(pageable, loggedUserWallet.getId());
//
//        return transactionPage;
//    }

    @GetMapping("/my-wallet")
    public Page<Transaction> getWalletTransactionsPage (@RequestParam("page") int page,
                                                       @RequestParam("size") int size,
                                                       Principal principal) {

        User loggedUser = userService.getLoggedUser(principal);
        Wallet loggedUserWallet = walletService.getLoggedUserWallet(loggedUser);
        int pageSize = 10;
        Pageable pageable = PageRequest.of(page -1, pageSize);

        Page<Transaction> transactionPage = transactionService.getWalletTransactionPage(pageable, loggedUserWallet.getId());

        return transactionPage;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable int id,
                                                          Principal principal,
                                                          Model model) {

        User loggedUser = userService.getLoggedUser(principal);
        Transaction transaction = transactionService.getById(id, loggedUser);

        return new ResponseEntity<>(transaction, HttpStatus.ACCEPTED);
    }

}
