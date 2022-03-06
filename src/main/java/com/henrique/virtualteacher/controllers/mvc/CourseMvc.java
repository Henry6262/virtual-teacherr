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


    @GetMapping("/browse")
    public String showAllEnabledCourses() {
        return "courses-browse";
    }

    @GetMapping("/create")
    public String showCreateCoursePage() {
        return "courses-create";
    }

    @GetMapping("/test")
    public String showStickyNavPage(Principal principal,
                                    Model model){

        Optional<User> loggedUser;
        if (principal == null) {
            loggedUser = Optional.empty();
        } else {
            loggedUser = Optional.of(userService.getByEmail(principal.getName()));
        }

        List<CourseModel> courses = courseService.getAllByEnabled(true, loggedUser);

        model.addAttribute("courses", courses);
        model.addAttribute("one-course", courses.get(0));

        return "browse-courses-ultimate";
    }

}
