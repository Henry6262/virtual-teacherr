package com.henrique.virtualteacher.controllers.mvc;

import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.services.interfaces.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/users")
@AllArgsConstructor
public class UserMvc {

    private final UserService userService;


    @GetMapping("/profile")
    public String showProfilePage(Principal principal,
                                  Model model) {

        if (principal == null) {
            //todo: show error page telling to login
            // maybe make the page have a overlay with a lock -> would be cool af
        }

        User loggedUser = userService.getByEmail(principal.getName());
        model.addAttribute("loggedUser", loggedUser);
        return "user-profile";
    }

}
