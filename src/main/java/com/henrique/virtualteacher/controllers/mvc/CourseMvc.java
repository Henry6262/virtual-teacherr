package com.henrique.virtualteacher.controllers.mvc;

import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.models.CourseModel;
import com.henrique.virtualteacher.services.interfaces.CourseService;
import com.henrique.virtualteacher.services.interfaces.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@AllArgsConstructor
@RequestMapping("/courses")
public class CourseMvc {

    private final UserService userService;
    private final CourseService courseService;




    @GetMapping("/create")
    public String showCreateCoursePage() {
        return "courses-create";
    }

    @GetMapping("/browse")
    public String showBrowseCourses(Principal principal,
                                    Model model){

        Optional<User> loggedUser;
        boolean userIsAnonymous = true;

        if (principal == null) {
            loggedUser = Optional.empty();
            model.addAttribute("anonymous_user_picture","https://res.cloudinary.com/henrique-mk/image/upload/v1646573717/13-136710_anonymous-browsing-user_t9wm22.jpg");
        } else {
            userIsAnonymous = false;
            loggedUser = Optional.of(userService.getByEmail(principal.getName()));
            model.addAttribute("loggedUser",loggedUser.get());
        }


        List<CourseModel> courses = courseService.getAllByEnabled(true, loggedUser);
        model.addAttribute("courses", courses);
        model.addAttribute("user_is_anonymous", userIsAnonymous);

        return "browse-courses-ultimate";
    }

}
