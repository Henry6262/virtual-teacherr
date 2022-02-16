package com.henrique.virtualteacher.controllers.rest;

import com.henrique.virtualteacher.entities.Assignment;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.services.interfaces.AssignmentService;
import com.henrique.virtualteacher.services.interfaces.LectureService;
import com.henrique.virtualteacher.services.interfaces.UserService;

import io.swagger.models.Response;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;
import java.util.List;

@RestController
@Controller
@RequestMapping("/api/assignments")
@AllArgsConstructor
public class AssignmentRestController {

    private final AssignmentService assignmentService;
    private final UserService userService;
    private final LectureService lectureService;
    private final Logger logger;


    @GetMapping("/{id}")
    public ResponseEntity<Model> getById(@PathVariable int id,
                                         Principal principal,
                                         Model model) {

        User loggedUser = userService.getByEmail(principal.getName());

        Assignment assignment = assignmentService.getById(id, loggedUser);
        model.addAttribute("assignment", assignment);

        return new ResponseEntity<>(model, HttpStatus.ACCEPTED);
    }

    @PostMapping("{id}/grade")
    public ResponseEntity<Model> gradeAssignment(@PathVariable int id,
                                                 @RequestParam int grade,
                                                 Principal principal,
                                                 Model model) {

        User loggedUser = userService.getByEmail(principal.getName());
        Assignment toGrade = assignmentService.getById(id, loggedUser);

        assignmentService.grade(toGrade, loggedUser, grade);

        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @GetMapping("/pending")                       //todo: this controller will be used by teachers to review all the assignments
    public ResponseEntity<Model> getAllPending(Principal principal,
                                               Model model) {

        User loggedUser = userService.getByEmail(principal.getName());

        List<Assignment> pendingAssignments = assignmentService.getAllPending(loggedUser);
        model.addAttribute("pendingAssignments", pendingAssignments);

        return new ResponseEntity<>(model,HttpStatus.ACCEPTED);
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<Model> update(@PathVariable int id,
                                        @RequestParam String newContent,
                                        Principal principal,
                                        Model model) {

        User loggedUser = userService.getByEmail(principal.getName());
        Assignment assignment = assignmentService.getById(id, loggedUser);

        assignmentService.update(newContent, assignment, loggedUser);
        model.addAttribute("Status", "success");

        return new ResponseEntity<>(model, HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Model> delete(@PathVariable int id,
                                        Principal principal,
                                        Model model) {

        User loggedUser = userService.getByEmail(principal.getName());
        Assignment assignment = assignmentService.getById(id, loggedUser);

        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}
