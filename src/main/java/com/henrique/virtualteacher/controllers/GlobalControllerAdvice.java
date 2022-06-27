package com.henrique.virtualteacher.controllers;

import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.services.interfaces.UserService;
import lombok.AllArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice ("com.henrique.virtualteacher")
@AllArgsConstructor
public class GlobalControllerAdvice {

    private final UserService userService;

    private final static String ANONYMOUS_USER_PICTURE_URL = "https://res.cloudinary.com/henrique-mk/image/upload/v1646919621/anonymous-user-flat-icon-vector-18958259_avueme.jpg";

    @ModelAttribute("user_is_anonymous")
    public boolean userIsAuthenticated(Principal principal,
                                   Model model) {

        return principal == null;
    }

    @ModelAttribute("user_profile_picture")
    private String addUserProfilePicture(Principal principal) {
        if (principal == null) {
            return ANONYMOUS_USER_PICTURE_URL;
        }
        User loggedUser = userService.getLoggedUser(principal);

        return loggedUser.getProfilePicture() == null ?
                ANONYMOUS_USER_PICTURE_URL : loggedUser.getProfilePicture();
    }


    private boolean checkUserIsAuthenticated(Principal principal) {
        return principal == null;
    }





}
