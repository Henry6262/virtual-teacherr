package com.henrique.virtualteacher.controllers.mvc;

import com.henrique.virtualteacher.entities.Transaction;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.entities.Wallet;
import com.henrique.virtualteacher.exceptions.AuthenticationException;
import com.henrique.virtualteacher.models.WalletModel;
import com.henrique.virtualteacher.services.interfaces.TransactionService;
import com.henrique.virtualteacher.services.interfaces.UserService;
import com.henrique.virtualteacher.services.interfaces.WalletService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/wallets")
@AllArgsConstructor
public class WalletMvcController {

    private final WalletService walletService;
    private final UserService userService;
    private final ModelMapper mapper;
    private final TransactionService transactionService;

    @GetMapping("/my-wallet")
    public String showUserWalletPage(Principal principal,
                                     Model model) {

        if (principal == null) {
            throw new AuthenticationException("User must be logged in to access wallet information");
        }
        User loggedUser = userService.getLoggedUser(principal);
        Wallet loggedUserWallet = walletService.getLoggedUserWallet(loggedUser);

        WalletModel walletModel = new WalletModel();
        mapper.map(loggedUserWallet, walletModel);

        List<Transaction> walletTransactions = transactionService.getAllByWallet(loggedUserWallet, loggedUser);

        model.addAttribute("defaultProfilePic", "https://res.cloudinary.com/henrique-mk/image/upload/v1646573717/13-136710_anonymous-browsing-user_t9wm22.jpg");
        model.addAttribute("userPicture", loggedUser.getProfilePicture());
        model.addAttribute("userId", loggedUser.getId());
        model.addAttribute("loggedUserWallet", walletModel);
        model.addAttribute("walletTransactions", walletTransactions);

        return "user-wallet";
    }

    @GetMapping("/deposit")
    public String showDepositPage(@RequestParam("amount") int depositAmount,
                                  Principal principal,
                                   Model model){
        
        model.addAttribute("depositAmount", depositAmount);
        model.addAttribute("loggedUserEmail", principal.getName());

        return "deposit-credit-card";
    }









}
