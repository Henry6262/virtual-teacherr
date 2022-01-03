package com.henrique.virtualteacher.controllers.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.security.auth.Subject;
import java.security.Principal;

@Controller
@RequestMapping("/")
public class HomeMvcController {


    @Autowired
    public HomeMvcController() {

    }

    @GetMapping
    public String showHomePage() {

        String hello = "hello";

        return "index";
    }

    @GetMapping(path = "admins")
    public String showAdminPage() {
        return "admin-page";
    }

}
