package com.henrique.virtualteacher.controllers.rest;

import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.models.RegisterUserModel;
import com.henrique.virtualteacher.models.SearchDto;
import com.henrique.virtualteacher.models.UserModel;
import com.henrique.virtualteacher.models.UserUpdateModel;
import com.henrique.virtualteacher.services.interfaces.UserService;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserRestController {

    private final UserService userService;
    private final ModelMapper mapper;
    private final Logger logger;

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "User successfully Retrieved"),
            @ApiResponse(code = 400, message = "The resource you were trying to retrieve, was not found")
    })

//    @PreAuthorize("@userSecurity.hasUserId(authentication,#id)")
    @GetMapping("/{id}")
    public User getById(@PathVariable int id,
                          Principal principal,
                          Model model) {


        User loggedUser = userService.getByEmail(principal.getName());

        User userToGet = userService.getById(id, loggedUser);
        UserModel userModel = new UserModel();

        mapper.map(userToGet, userModel);

        model.addAttribute("User", userModel);
        return userToGet;
    }

    @GetMapping("/search")
    public User searchByUsername(@RequestParam("keyword") SearchDto searchDto,
                                 Model model){

        model.addAttribute("name",searchDto);
        return userService.getByEmail(searchDto.getKeyword());
    }

    @GetMapping("/login")
    public boolean verifyLoginInfo(@RequestParam("keyword")SearchDto email,
                                   @RequestParam("password") SearchDto password,
                                   Model model) {

        return userService.verifyLoginInfo(email.getKeyword(), password.getKeyword());
    }


    @PostMapping("/register")
    public User create(@RequestBody @Valid RegisterUserModel registerUserModel) {
        return userService.create(registerUserModel);
    }

    @PutMapping()
    public void update(@RequestBody @Valid UserUpdateModel updateModel,
                       Principal principal) {

        User loggedUser = userService.getByEmail(principal.getName());

        userService.update(updateModel, loggedUser);
        logger.info(String.format("User with email: {%s}, has been updated",updateModel.getEmail()));
    }

    @DeleteMapping()
    public String delete(Principal principal) {

        User loggedUser = userService.getByEmail(principal.getName());

        userService.delete(loggedUser, loggedUser);
        logger.info(String.format("User with email: {%s}, has been deleted",principal.getName()));

        return "redirect:/auth/logout";
        //todo: needs testing , not tested yet, need to delete all the info related to the user in the
        // foreign key tables
    }

}
