package com.henrique.virtualteacher.services;

import com.henrique.virtualteacher.Helpers;
import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.Lecture;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.exceptions.EntityNotFoundException;
import com.henrique.virtualteacher.exceptions.ImpossibleOperationException;
import com.henrique.virtualteacher.exceptions.UnauthorizedOperationException;
import com.henrique.virtualteacher.models.RegisterUserModel;
import com.henrique.virtualteacher.repositories.UserRepository;
import com.henrique.virtualteacher.services.implementation.UserServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

    @Mock
    UserRepository userRepository;
    @Mock
    ModelMapper modelMapper =  new ModelMapper();
    @Mock
    BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    UserServiceImpl userService;

    @Test
    void completeLecture_should_throwException_when_userIsNotEnrolledInCourse() {
        User mockUser = Helpers.createMockUser();
        Course mockCourse = Helpers.createMockCourse();
        Lecture mockLecture = Helpers.createMockLecture(mockCourse);

        Assertions.assertThrows(ImpossibleOperationException.class, () -> mockUser.completeLecture(mockLecture));
    }

    @Test
    void completeLecture_shouldThrowException_when_userHasCompletedLecture() {
        User mockUser = Helpers.createMockUser();
        Course mockCourse = Helpers.createMockCourse();
        Lecture mockLecture = Helpers.createMockLecture(mockCourse);
        mockUser.enrollToCourse(mockCourse);
        mockUser.completeLecture(mockLecture);

        Assertions.assertThrows(ImpossibleOperationException.class,() -> mockUser.completeLecture(mockLecture));
    }

    @Test
    public void getById_shouldThrowException_when_UserIsNotOwner_nor_admin_or_teacher() {

        User user = Helpers.createMockUser();
        User initiator = Helpers.createMockUser();
        initiator.setId(5);

        Assertions.assertThrows(UnauthorizedOperationException.class, () -> userService.getById(user.getId(), initiator));
    }

    @Test
    public void getById_shouldThrowException_when_repo_doesNotFindId() {

        User user = Helpers.createMockUser();
        User initiator = Helpers.createMockUser();

        Assertions.assertThrows(EntityNotFoundException.class, () -> userService.getById(user.getId(), initiator));
    }

    @Test
    public void getById_should_returnUser_when_userExists() {
        User mockUser = Helpers.createMockUser();
        User mockTeacher = Helpers.createMockTeacher();

        Mockito.when(userRepository.findById(mockUser.getId())).thenReturn(Optional.of(mockUser));

        User result = userService.getById(mockUser.getId(), mockTeacher);

        Assertions.assertAll(
                () -> Assertions.assertEquals(mockUser.getId(), result.getId()),
                () -> Assertions.assertEquals(mockUser.getEmail(), result.getEmail()),
                () -> Assertions.assertEquals(mockUser.getPassword(), result.getPassword()),
                () -> Assertions.assertEquals(mockUser.getFirstName(), result.getFirstName()),
                () -> Assertions.assertEquals(mockUser.getLastName(), result.getLastName())
        );
    }

    @Test
    public void getById_shouldReturnUser_when_userIs_admin() {

        User user = Helpers.createMockUser();
        User admin = Helpers.createMockAdmin();

        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        User result = userService.getById(user.getId(), admin);

        Assertions.assertAll(
                () ->Assertions.assertEquals(user.getId() , result.getId()),
                () -> Assertions.assertEquals(user.getFirstName(), result.getFirstName()),
                () -> Assertions.assertEquals(user.getLastName(), result.getLastName()),
                () -> Assertions.assertEquals(user.getEmail(), result.getEmail()),
                () ->Assertions.assertEquals(user.getPassword(), result.getPassword())
        );
    }

    @Test
    public void getByID_shouldReturnUser_when_InitiatorIs_teacher() {
        User user = Helpers.createMockUser();
        User teacher = Helpers.createMockTeacher();

        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        User result = userService.getById(user.getId(), teacher);

        Assertions.assertAll(
                () ->Assertions.assertEquals(user.getId(), result.getId()),
                () -> Assertions.assertEquals(user.getFirstName(), result.getFirstName()),
                () -> Assertions.assertEquals(user.getLastName(), result.getLastName()),
                () -> Assertions.assertEquals(user.getEmail(), result.getEmail()),
                () ->Assertions.assertEquals(user.getPassword(), result.getPassword())
        );
    }


    @Test
    public void create_shouldThrow_exception_when_passwords_doesNot_meet_requirements(){

        RegisterUserModel registerModel = Helpers.createUserRegisterModel();
        registerModel.setPassword("123@a");
        registerModel.setPasswordConfirm("123@a");

        Assertions.assertThrows(ImpossibleOperationException.class, () -> userService.create(registerModel));
    }

    //todo: getByEmailTest
    // create, delete, update tests

    @Test
    public void getByEmail_should_callRepository_when_emailFound() {

        User user = Helpers.createMockUser();

        Mockito.when(userRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(user));
        userService.getByEmail(user.getEmail());

        Mockito.verify(userRepository, Mockito.times(1))
                .findByEmail(user.getEmail());
    }





}
