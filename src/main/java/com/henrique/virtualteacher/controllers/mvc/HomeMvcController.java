package com.henrique.virtualteacher.controllers.mvc;

import com.henrique.virtualteacher.models.CourseModel;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.security.auth.Subject;
import java.security.Principal;

@Controller
@AllArgsConstructor
@RequestMapping("/")
public class HomeMvcController {

    @GetMapping("/")
    public String showHomePage(Model model) {
        CourseModel courseModel = new CourseModel();
        courseModel.setTitle("hello-world");
        model.addAttribute("hello", courseModel);
        return "index";
    }

    @GetMapping(path = "admins")
    public String showAdminPage() {
        return "admin-page";
    }

}
