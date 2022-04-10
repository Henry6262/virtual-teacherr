package com.henrique.virtualteacher.controllers.mvc;

import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.entities.Wallet;
import com.henrique.virtualteacher.exceptions.AuthenticationException;
import com.henrique.virtualteacher.models.WalletModel;
import com.henrique.virtualteacher.services.interfaces.UserService;
import com.henrique.virtualteacher.services.interfaces.WalletService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/wallets")
@AllArgsConstructor
public class WalletMvcController {

    private final WalletService walletService;
    private final UserService userService;
    private final ModelMapper mapper;

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
        model.addAttribute("loggedUserWallet", walletModel);
        return "user-wallet";
    }



}
