package com.henrique.virtualteacher.controllers.rest;

import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.exceptions.EntityNotFoundException;
import com.henrique.virtualteacher.exceptions.ImpossibleOperationException;
import com.henrique.virtualteacher.models.*;
import com.henrique.virtualteacher.services.interfaces.UserService;
import com.henrique.virtualteacher.services.interfaces.VerificationTokenService;
import com.henrique.virtualteacher.services.interfaces.WalletService;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserRestController {

    private final UserService userService;
    private final WalletService walletService;
    private final VerificationTokenService tokenService;
    private final Logger logger;

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "User successfully Retrieved"),
            @ApiResponse(code = 400, message = "The resource you were trying to retrieve, was not found")
    })

    @GetMapping
    public ResponseEntity<Model> getAll(Principal principal,
                                            Model model) {

        User loggedUser = userService.getByEmail(principal.getName());
        List<UserModel> dtoList = userService.getAllUserModels(loggedUser);

        model.addAttribute("allUsers", dtoList);
        return new ResponseEntity<>(model, HttpStatus.OK);
    }

    @PostMapping("/apply/teacher")
    public boolean applyForTeacherRole(Principal principal, Optional<String> username) {

        User affectedUser;
        if (username.isPresent()) {
            affectedUser = userService.getByUsername(username.get());
        } else {
            affectedUser = userService.getLoggedUser(principal);
        }
        userService.grantTeacherRole(principal, affectedUser);
        return true;
    }

    @GetMapping("/{id}")
    public UserModel getById(@PathVariable int id,
                          Principal principal,
                          Model model) {

        User loggedUser = userService.getByEmail(principal.getName());
        User userToGet = userService.getById(id, loggedUser);
        return new UserModel(userToGet);
    }


    @GetMapping("/search")
    public Boolean searchByUsername(@RequestParam("keyword") SearchDto searchDto,
                                      Model model){

        model.addAttribute("name",searchDto);
        try {
            userService.getByEmail(searchDto.getKeyword());
        } catch (EntityNotFoundException e) {
            return false;
        }
        return true;
    }

    @GetMapping("/verify/username/{username}/")
    public boolean verifyNewUsernameIsUnique(@PathVariable("username") String newUsername,
                                             Principal principal,
                                             Model model) {

        User loggedUser = userService.getLoggedUser(principal);
        String currentUsername = loggedUser.getUsername();

        return userService.checkUsernameIsUnique(loggedUser, newUsername);
    }

    @GetMapping("/login")
    public ResponseEntity<String> verifyLoginInfo(@RequestParam("keyword")SearchDto email,
                                   @RequestParam("password") SearchDto password,
                                   Model model) {

        try {
            userService.verifyLoginInfo(email.getKeyword(), password.getKeyword());
        } catch (ImpossibleOperationException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
         return new ResponseEntity<>("success", HttpStatus.ACCEPTED);
    }


    @PostMapping("/register")
    public ResponseEntity<Boolean> create(@RequestBody RegisterUserModel registerUserModel,
                                          HttpServletRequest request,
                                          Model model) {

        User created = userService.create(registerUserModel);
        walletService.create(created);          //TODO: Create wallet when user has verified account -> implement
        tokenService.create(created, request);

        return new ResponseEntity<>(true,HttpStatus.CREATED);
    }

    @PutMapping("/update")
    public void update(@RequestBody @Valid UserUpdateModel updateModel,
                       Principal principal) {

        userService.updateProfileInfo(principal, updateModel);
        logger.info(String.format("api/users/update rest method was called by: %s", principal.getName()));
    }

    @PutMapping("/update/password")
    public void updatePassword(@RequestParam("newPassword") String newPassword,
                               @RequestParam("passwordConfirm") String passwordConfirm,
                               Principal principal) {

        userService.updatePassword(principal, newPassword, passwordConfirm);
        logger.info(String.format("api/users/update/password rest method was called by: %s", principal.getName()));
    }

    @DeleteMapping()
    public String delete(Principal principal) {

        User loggedUser = userService.getByEmail(principal.getName());

        userService.delete(loggedUser, loggedUser);
        logger.info(String.format("User with email: {%s}, has been deleted",principal.getName()));

        return "redirect:/auth/logout";
        //todo: needs testing , not tested yet, need to delete all the info related to the user in the
        // foreign key tables, and also all assignments realated to the user
    }

}
