package com.henrique.virtualteacher.controllers.mvc;

import com.henrique.virtualteacher.services.interfaces.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@AllArgsConstructor
@RequestMapping("/courses")
public class CourseMvc {

    private final UserService userService;


    @GetMapping("/browse")
    public String showAllEnabledCourses() {
        return "courses-browse";
    }

    @GetMapping("/create")
    public String showCreateCoursePage() {
        return "courses-create";
    }

}
