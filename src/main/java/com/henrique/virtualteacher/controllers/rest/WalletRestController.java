package com.henrique.virtualteacher.controllers.rest;

import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.entities.Wallet;
import com.henrique.virtualteacher.exceptions.AuthenticationException;
import com.henrique.virtualteacher.models.WalletModel;
import com.henrique.virtualteacher.services.interfaces.UserService;
import com.henrique.virtualteacher.services.interfaces.WalletService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;

@RestController
@RequestMapping("/api/wallets")
@AllArgsConstructor
public class WalletRestController {

    private final WalletService walletService;
    private final UserService userService;
    private final ModelMapper mapper;

    @GetMapping("/my-wallet")
     public ResponseEntity<WalletModel> getLoggedUserWallet(Principal principal) {
         if(principal == null) {
                throw new AuthenticationException("User needs to login before accessing own wallet");
            }
            User loggedUser = userService.getByEmail(principal.getName());
            Wallet userWallet = walletService.getLoggedUserWallet(loggedUser);

            WalletModel walletModel = mapper.map(userWallet, new TypeToken<WalletModel>() {}.getType());
            return new ResponseEntity<>(walletModel, HttpStatus.OK);
        }

        @PostMapping("/deposit")
        public ResponseEntity<HttpStatus> deposit(Principal principal,
                                                  @RequestParam("amount") BigDecimal amount) {

            User loggedUser = userService.getLoggedUser(principal);
            Wallet userWallet = walletService.getLoggedUserWallet(loggedUser);

            walletService.deposit(loggedUser, amount);
            return new ResponseEntity<>(HttpStatus.OK);
        }

        @PostMapping("/transfer")
        public ResponseEntity<HttpStatus> transfer(Principal principal,
                                               @RequestParam("amount") BigDecimal amount,
                                               @RequestParam("email") String recipientEmail,
                                               Model model) {

            User loggedUser = userService.getLoggedUser(principal);
            User recipient = userService.getByEmail(recipientEmail);

            walletService.send(loggedUser, recipient, amount);
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        }

}
