package com.henrique.virtualteacher.controllers.rest;

import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.exceptions.EntityNotFoundException;
import com.henrique.virtualteacher.models.RegisterUserModel;
import com.henrique.virtualteacher.models.SearchDto;
import com.henrique.virtualteacher.models.UserModel;
import com.henrique.virtualteacher.models.UserUpdateModel;
import com.henrique.virtualteacher.services.interfaces.UserService;
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

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

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

    @GetMapping
    public ResponseEntity<Model> getAll(Principal principal,
                                            Model model) {

        User loggedUser = userService.getByEmail(principal.getName());
        List<UserModel> dtoList = mapper.map(userService.getAll(loggedUser), new TypeToken<List<UserModel>>() {}.getType());

        model.addAttribute("allUsers", dtoList);
        return new ResponseEntity<>(model, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public UserModel getById(@PathVariable int id,
                          Principal principal,
                          Model model) {

        User loggedUser = userService.getByEmail(principal.getName());

        User userToGet = userService.getById(id, loggedUser);

        UserModel userModel = new UserModel();
        mapper.map(userToGet, userModel);

        return userModel;
    }


    @GetMapping("/search")
    public UserModel searchByUsername(@RequestParam("keyword") SearchDto searchDto,
                                      Model model){

        model.addAttribute("name",searchDto);

        User toFind = userService.getByEmail(searchDto.getKeyword());

        return mapper.map(toFind, new TypeToken<UserModel>() {}.getType());
    }

    @GetMapping("/login")
    public ResponseEntity<Model> verifyLoginInfo(@RequestParam("keyword")SearchDto email,
                                   @RequestParam("password") SearchDto password,
                                   Model model) {

        model.addAttribute("success", "success");

         userService.verifyLoginInfo(email.getKeyword(), password.getKeyword());
         return new ResponseEntity<>(model, HttpStatus.ACCEPTED);
    }


    @PostMapping("/register")
    public ResponseEntity<Model> create(@RequestBody RegisterUserModel registerUserModel,
                                        Model model) {

        userService.create(registerUserModel);

        model.addAttribute("success", "success");
        return new ResponseEntity<>(model,HttpStatus.OK);
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
        // foreign key tables, and also all assignments realated to the user
    }

}
