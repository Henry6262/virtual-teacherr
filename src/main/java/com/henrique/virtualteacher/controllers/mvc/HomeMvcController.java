package com.henrique.virtualteacher.controllers.mvc;

import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.models.CourseModel;
import com.henrique.virtualteacher.models.EnumTopic;
import com.henrique.virtualteacher.services.interfaces.CourseService;
import com.henrique.virtualteacher.services.interfaces.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.security.auth.Subject;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@AllArgsConstructor
@RequestMapping("/")
public class HomeMvcController {

    private final CourseService courseService;
    private final UserService userService;

    @GetMapping()
    public String showHomePage(Principal principal,
            Model model) {

        Optional<User> loggedUser;
        boolean userIsAnonymous = true;

        if (principal == null) {
            loggedUser = Optional.empty();
            model.addAttribute("anonymous_user_picture","https://res.cloudinary.com/henrique-mk/image/upload/v1646919621/anonymous-user-flat-icon-vector-18958259_avueme.jpg");
        } else {
            userIsAnonymous = false;
            loggedUser = Optional.of(userService.getByEmail(principal.getName()));
            model.addAttribute("loggedUser",loggedUser.get());
        }

        List<CourseModel> courses = courseService.getAllByEnabled(true, loggedUser);
        model.addAttribute("user_is_anonymous", userIsAnonymous);
        model.addAttribute("top_three_courses", courseService.getTopTheeCoursesByRating());
        model.addAttribute("courses", courses);
        model.addAttribute("java_courses", courseService.getAllByTopic(EnumTopic.JAVA));


        return "browse-courses-ultimate";
    }

    @GetMapping(path = "admins")
    public String showAdminPage() {
        return "admin-page";
    }

}
