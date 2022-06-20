package com.henrique.virtualteacher.controllers.mvc;

import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.models.UserModel;
import com.henrique.virtualteacher.services.interfaces.AssignmentService;
import com.henrique.virtualteacher.services.interfaces.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
       addUserInformationToModel(model, loggedUser.getId());

       //todo, add additional functionality like: Change picture, Change password

        return "user-profile";
    }

    @GetMapping("/{id}/profile")
    public String showUserProfilePage(@PathVariable int id,
                                      Principal principal,
                                      Model model) {

        User loggedUser = userService.getLoggedUser(principal);
        UserModel userToGet = userService.getModelById(id);

        addUserInformationToModel(model, id);
        return "user-profile";
    }

    private void addUserInformationToModel(Model model, int id) {
        UserModel userToGet = userService.getModelById(id);
        model.addAttribute("profilePicture", userToGet.getProfilePicture());
        model.addAttribute("totalCompletedCourses", userToGet.getCompletedCourses().size());
        model.addAttribute("totalCompletedLectures", userToGet.getCompletedLectures().size());
        model.addAttribute("totalComments", userToGet.getComments());
        model.addAttribute("averageGradeForAllCourses", assignmentService.getStudentAverageGradeForAllCourses(id, userToGet));
        model.addAttribute("mostStudiedCourseTopic", userService.getMostStudiedCourseTopic(userToGet));

        //inventory
        model.addAttribute("nftCourseInventory", userToGet.getNFTCours());
    }

}
