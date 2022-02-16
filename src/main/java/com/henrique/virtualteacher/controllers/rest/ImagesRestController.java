package com.henrique.virtualteacher.controllers.rest;

import com.henrique.virtualteacher.configurations.CloudinaryConfig;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.services.interfaces.UserService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping("/api/images/")
@AllArgsConstructor
public class ImagesRestController {

    private final UserService userService;
    private final CloudinaryConfig cloudinaryConfig;

    @PostMapping("/upload")
    public ResponseEntity<String> submitPicture(MultipartFile file,
                                                Principal principal) throws IOException {


        User loggedUser = userService.getByEmail(principal.getName());

        String url = cloudinaryConfig.upload(file);
        return new ResponseEntity<>(url, HttpStatus.CREATED);
    }

    @PostMapping("/destroy")
    public ResponseEntity<String> deletePicture(String url,
                                                Principal principal) throws Exception {

        User loggedUser = userService.getByEmail(principal.getName());

        cloudinaryConfig.destroy(url);
        return new ResponseEntity<>("success", HttpStatus.ACCEPTED);
    }

}
