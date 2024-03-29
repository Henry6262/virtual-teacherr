package com.henrique.virtualteacher.controllers.rest;


import com.henrique.virtualteacher.entities.Lecture;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.exceptions.UnauthorizedOperationException;
import com.henrique.virtualteacher.models.CourseModel;
import com.henrique.virtualteacher.models.LectureModel;
import com.henrique.virtualteacher.models.SearchDto;
import com.henrique.virtualteacher.services.interfaces.LectureService;
import com.henrique.virtualteacher.services.interfaces.UserService;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/lectures")
@AllArgsConstructor
public class LectureRestController {

    private final LectureService lectureService;
    private final UserService userService;

    @GetMapping()
    public ResponseEntity<Model> getAll(Principal principal, Model model){

        User loggedUser = userService.getByEmail(principal.getName());
        List<LectureModel> dtoList = lectureService.mapAllToModel(lectureService.getAll());
        model.addAttribute("allLectures", dtoList);
        return new ResponseEntity<>(model, HttpStatus.ACCEPTED);
    }

    @GetMapping("/course/{id}")
    public List<LectureModel> getAllByCourseId(@PathVariable int id,
                                          Principal principal) {

        User loggedUser = userService.getByEmail(principal.getName());

        if (loggedUser.isNotTeacherOrAdmin()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "user is not authorized");
        }

         return lectureService.mapAllToModel(lectureService.getAllByCourseId(id));
    }


    @GetMapping("/{id}")
    public LectureModel getById(@PathVariable int id,
                                Principal principal) {

        User loggedUser = userService.getByEmail(principal.getName());
        Lecture lecture = lectureService.getById(id);
        return new LectureModel(lecture);
    }

    @PutMapping("/{id}/update")
    public void update(@PathVariable int id,
                       @RequestBody LectureModel lectureModel,
                       Principal principal) {

        User loggedUser = userService.getByEmail(principal.getName());
        Lecture lecture = lectureService.getById(id);

        lectureService.update(lectureModel, lecture, loggedUser);

        //todo: test
    }

    @DeleteMapping("/{id}/delete")
    public void delete(@PathVariable int id,
                       Principal principal) {

        User loggedUser = userService.getByEmail(principal.getName());
        Lecture lectureToDelete = lectureService.getById(id);
        lectureService.delete(lectureToDelete, loggedUser);

        //todo: test
    }

}
