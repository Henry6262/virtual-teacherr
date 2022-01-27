package com.henrique.virtualteacher.controllers.rest;

import com.henrique.virtualteacher.entities.CourseRating;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.services.interfaces.RatingService;
import com.henrique.virtualteacher.services.interfaces.UserService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/rating")
@AllArgsConstructor
public class RatingRestController {

    private RatingService ratingService;
    private UserService userService;
    private ModelMapper modelMapper;
    private Logger logger;


    @GetMapping("/{id}")
    public ResponseEntity<Model> getById(@PathVariable int id,
                                         Principal principal,
                                         Model model) {

        User loggedUser = userService.getByEmail(principal.getName());

        CourseRating rating = ratingService.getById(id);
        model.addAttribute("rating",rating);
        return new ResponseEntity<>(model, HttpStatus.ACCEPTED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Model> update(@PathVariable int id,
                                        @RequestParam("newRating") int newRating,
                                        Principal principal) {

        User loggedUser = userService.getByEmail(principal.getName());
        CourseRating courseRating = ratingService.getById(id);

        ratingService.update(courseRating, newRating, loggedUser);

        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Model> delete(@PathVariable int id,
                                        Principal principal){

        User loggedUser = userService.getByEmail(principal.getName());
        CourseRating courseRating = ratingService.getById(id);

        ratingService.
    }


}
