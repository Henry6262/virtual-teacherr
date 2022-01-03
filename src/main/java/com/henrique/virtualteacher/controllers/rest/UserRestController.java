package com.henrique.virtualteacher.controllers.rest;

import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.models.RegisterUserModel;
import com.henrique.virtualteacher.models.SearchDto;
import com.henrique.virtualteacher.services.interfaces.UserService;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserRestController {

    private final UserService userService;

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "User successfully Retrieved"),
            @ApiResponse(code = 400, message = "The resource you were trying to retrieve, was not found")
    })

//    @PreAuthorize("@userSecurity.hasUserId(authentication,#id)")
    @GetMapping("/{id}")
    public User getById(@PathVariable int id, Principal principal) {

        User logged = userService.getByEmail(principal.getName());
        return userService.getById(id, logged);
    }

    @GetMapping("/search")
    public User searchByUsername(@RequestParam("keyword") SearchDto searchDto,
                                 Model model){

        model.addAttribute("name",searchDto);
        System.out.println("searchinnggg");
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

}
