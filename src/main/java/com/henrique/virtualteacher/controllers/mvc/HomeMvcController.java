package com.henrique.virtualteacher.controllers.mvc;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.security.auth.Subject;
import java.security.Principal;

@Controller
@AllArgsConstructor
@RequestMapping("/")
public class HomeMvcController {

    @GetMapping
    public String showHomePage() {
        return "index";
    }

    @GetMapping(path = "admins")
    public String showAdminPage() {
        return "admin-page";
    }

}
