package com.henrique.virtualteacher.controllers.mvc;

import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.services.interfaces.AssignmentService;
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
    private final AssignmentService assignmentService;


    @GetMapping("/profile")
    public String showProfilePage(Principal principal,
                                  Model model) {

        if (principal == null) {
            return "login";
        }

        User loggedUser = userService.getByEmail(principal.getName());
        model.addAttribute("profilePicture", loggedUser.getProfilePicture());
        model.addAttribute("totalCompletedCourses", loggedUser.getCompletedCourses().size());
        model.addAttribute("totalCompletedLectures", loggedUser.getCompletedLectures().size());
        model.addAttribute("totalComments", loggedUser.getComments());
        model.addAttribute("averageGradeForAllCourses", assignmentService.getStudentAverageGradeForAllCourses(loggedUser));
        model.addAttribute("mostStudiedCourseTopic", userService.mostStudiedCourseTopic(loggedUser));

        return "user-profile";
    }

}
