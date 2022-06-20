package com.henrique.virtualteacher.controllers.mvc;

import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.models.RegisterUserModel;
import com.henrique.virtualteacher.services.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationMvcController {

    private final UserService userService;

    @GetMapping("/login")
    public String showLoginAndRegisterPage(Model model) {
        RegisterUserModel registerModel = new RegisterUserModel();
        return "login";
    }

    @GetMapping("/register/verify")
    public String showEmailSentPage(Model model,
                                    @RequestParam("email") String email) {

        model.addAttribute("email", email);
        return "registration-verification";
    }

    @GetMapping("/transactions/verify")
    public String showVerifyTransactionPage(Principal principal,
                                            Model model) {

        User loggedUser = userService.getLoggedUser(principal);

        model.addAttribute("isTransaction", true);
        return "registration-verification";
    }

}
