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
    private final ModelMapper mapper;

    @GetMapping()
    public ResponseEntity<Model> getAll(Principal principal, Model model){

        User loggedUser = userService.getByEmail(principal.getName());
        List<LectureModel> dtoList = mapper.map(lectureService.getAll(), new TypeToken<List<LectureModel>>() {}.getType());

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

        List<LectureModel> dtoList = mapper.map(lectureService.getAllByCourseId(id), new TypeToken<List<LectureModel>>() {}.getType());
        return dtoList;
    }

    @GetMapping("/{id}")
    public LectureModel getById(@PathVariable int id,
                           Principal principal) {

        User loggedUser = userService.getByEmail(principal.getName());
        Lecture lecture = lectureService.getById(id);
        LectureModel lectureModel = new LectureModel();

        mapper.map(lecture, lectureModel);

        return lectureModel;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id,
                       Principal principal) {

        User loggedUser = userService.getByEmail(principal.getName());
        Lecture lectureToDelete = lectureService.getById(id);
        lectureService.delete(lectureToDelete, loggedUser);
    }








//    @PostMapping()
//    public SearchDto create(@RequestBody LectureModel lectureModel,
//                            Principal principal) {
//
//        User loggedUser = userService.getByEmail(principal.getName());
//
////        if (loggedUser.isNotTeacherOrAdmin()) {
////            throw new UnauthorizedOperationException("User", "id", String.valueOf(loggedUser.getId()), "create", "Lecture", "Title", lectureModel.getTitle());
////        }
//
//        lectureService.create(lectureModel, loggedUser);
//        return new SearchDto(lectureModel.getTitle());
//
//
//    }





}
