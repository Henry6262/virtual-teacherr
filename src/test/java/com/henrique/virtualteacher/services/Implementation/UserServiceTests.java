package com.henrique.virtualteacher.services.Implementation;

import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.NFT;
import com.henrique.virtualteacher.entities.Lecture;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.exceptions.DuplicateEntityException;
import com.henrique.virtualteacher.exceptions.EntityNotFoundException;
import com.henrique.virtualteacher.exceptions.ImpossibleOperationException;
import com.henrique.virtualteacher.exceptions.UnauthorizedOperationException;
import com.henrique.virtualteacher.models.EnumTopic;
import com.henrique.virtualteacher.models.RegisterUserModel;
import com.henrique.virtualteacher.models.UserUpdateModel;
import com.henrique.virtualteacher.repositories.UserRepository;
import com.henrique.virtualteacher.services.Helpers;
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

import java.util.ArrayList;
import java.util.List;
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
        mockUser.purchaseCourse(mockCourse);
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
    public void getAll_shouldThrowException_whenInitiator_isNotTeacherOrAdmin() {
        User initiator = Helpers.createMockUser(21);
        Assertions.assertThrows(UnauthorizedOperationException.class, () -> userService.getAll(initiator));
    }

    @Test
    public void getAll_shouldCallRepository_whenInitiator_isTeacherOrAdmin() {
        User mockTeacher = Helpers.createMockTeacher();

        Mockito.when(userService.getAll(mockTeacher)).thenReturn(Helpers.createMockUserList());
        userService.getAll(mockTeacher);
        Mockito.verify(userRepository, Mockito.times(1)).findAll();
    }

    @Test
    public void create_shouldThrowException_whenEmail_isAlreadyInUse() {
        RegisterUserModel registerModel = Helpers.createUserRegisterModel();
        User existingUser = Helpers.createMockUser();
        existingUser.setEmail("maradona@hotmail.com");
        registerModel.setEmail("maradona@hotmail.com");

        Mockito.when(userRepository.findByEmail(registerModel.getEmail())).thenReturn(Optional.of(existingUser));

        Assertions.assertThrows(DuplicateEntityException.class, () -> userService.create(registerModel));
    }

    @Test
    public void create_shouldThrow_exception_when_passwords_doesNot_meet_requirements(){

        RegisterUserModel registerModel = Helpers.createUserRegisterModel();
        registerModel.setPassword("123@a");
        registerModel.setPasswordConfirm("123@a");

        Assertions.assertThrows(ImpossibleOperationException.class, () -> userService.create(registerModel));
    }

    @Test
    public void create_shouldThrowException_whenPasswords_areNotEqual() {
        RegisterUserModel userModel = Helpers.createUserRegisterModel();
        userModel.setPassword("12345@Ab");
        userModel.setPasswordConfirm("12345@Ba");

        Assertions.assertThrows(ImpossibleOperationException.class, () -> userService.create(userModel));
    }

    @Test
    public void create_shouldSaveAndReturnEntity_whenRegistration_isSuccessful() {
        RegisterUserModel userModel = Helpers.createUserRegisterModel();
        User createdUser = Helpers.createMockUser(21);

        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(createdUser);

        User result = userService.create(userModel);

        Assertions.assertEquals(createdUser.getId(), result.getId());
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

    @Test
    public void getAllByVerification_should_throwException_whenUserIsNot_teacherOrAdmin() {
        User mockUser = Helpers.createMockUser();
        Assertions.assertThrows(UnauthorizedOperationException.class, () -> userService.getAllByVerification(false, mockUser));
    }

    @Test
    public void getAllByVerification_shouldReturnList_whenUserAdminOrTeacher() {
        User mockUser = Helpers.createMockTeacher();

        Mockito.when(userRepository.findAllByEnabled(true)).thenReturn(Helpers.createMockUserList());
        userService.getAllByVerification(true, mockUser);
        Mockito.verify(userRepository, Mockito.times(1)).findAllByEnabled(true);
    }

    @Test
    public void verifyLoginInfo_shouldThrowException_whenPasswordsAreDifferent() {
        User mockUser = Helpers.createMockUser(21);
        mockUser.setPassword("123123");

        Mockito.when(userRepository.findByEmail(mockUser.getEmail())).thenReturn(Optional.of(mockUser));

        Assertions.assertThrows(ImpossibleOperationException.class, () -> userService.verifyLoginInfo(mockUser.getEmail(),"321321"));
    }

    @Test
    public void update_shouldThrowException_whenInitiator_isNotAuthorized() {
        User initiator = Helpers.createMockUser(21);
        User userToUpdate = Helpers.createMockUser(1);
        UserUpdateModel userBeingUpdated = Helpers.mapUserToUpdateModel(userToUpdate);

        Mockito.when(userRepository.findByEmail(userBeingUpdated.getEmail())).thenReturn(Optional.of(userToUpdate));
        Assertions.assertThrows(UnauthorizedOperationException.class, () -> userService.update(userBeingUpdated, initiator));
    }

    @Test
    public void update_shouldThrowException_whenNewPassword_doesNotMeetRequirements() {
        User mockUser = Helpers.createMockUser(21);
        UserUpdateModel updateModel = Helpers.mapUserToUpdateModel(mockUser);
        updateModel.setPassword("12345");

        Mockito.when(userRepository.findByEmail(mockUser.getEmail())).thenReturn(Optional.of(mockUser));
        Assertions.assertThrows(ImpossibleOperationException.class, () -> userService.update(updateModel, mockUser));
    }

    @Test
    public void update_shouldThrowException_whenPasswords_doNotMatch() {
        User mockUser = Helpers.createMockUser(21);
        UserUpdateModel updateModel = Helpers.mapUserToUpdateModel(mockUser);
        updateModel.setPassword("12345@Ab");
        updateModel.setPasswordConfirm("12345@Ba");

        Mockito.when(userRepository.findByEmail(mockUser.getEmail())).thenReturn(Optional.of(mockUser));
        Assertions.assertThrows(ImpossibleOperationException.class, () -> userService.update(updateModel, mockUser));
    }

    @Test
    public void update_shouldSveEntity_when_everythingIsCorrect() {
        User mockUser = Helpers.createMockUser(21);
        UserUpdateModel updateModel = Helpers.mapUserToUpdateModel(mockUser);
        updateModel.setPassword("12345@Ab");
        updateModel.setPasswordConfirm("12345@Ab");

        Mockito.when(userRepository.findByEmail(mockUser.getEmail())).thenReturn(Optional.of(mockUser));
        userService.update(updateModel, mockUser);
        Mockito.verify(userRepository, Mockito.times(1)).save(mockUser);
    }

    @Test
    public void update_shouldThrowException_when_newEmailIsInUse() {
        User mockUser = Helpers.createMockTeacher();
        UserUpdateModel updateModel = Helpers.mapUserToUpdateModel(mockUser);
        User existingEmailUser = Helpers.createMockUser();
        existingEmailUser.setEmail("patricky");
        updateModel.setEmail("patricky");

        Mockito.when(userRepository.findByEmail(updateModel.getEmail())).thenReturn(Optional.of(existingEmailUser));
        Assertions.assertThrows(DuplicateEntityException.class, () -> userService.update(updateModel, mockUser));
    }

    @Test
    public void delete_shouldThrowException_whenInitiator_isNotAuthorized() {
        User initiator = Helpers.createMockUser(21);
        User userToDelete = Helpers.createMockUser(1) ;

        Assertions.assertThrows(UnauthorizedOperationException.class, () -> userService.delete(userToDelete, initiator));
    }

    @Test
    public void delete_shouldCallRepository_whenInitiator_isAuthorized() {
        User initiator = Helpers.createMockTeacher();
        User userToDelete = Helpers.createMockUser(21);

        userService.delete(userToDelete, initiator);
        Mockito.verify(userRepository, Mockito.times(1)).delete(userToDelete);
    }

    @Test
    public void enableUser_shouldEnableUser_whenUserExists() {
        User userToVerify = Helpers.createMockUser();
        userToVerify.setEnabled(false);

        Mockito.when(userRepository.findById(userToVerify.getId())).thenReturn(Optional.of(userToVerify));
        userService.enableUser(userToVerify.getId());

        Assertions.assertTrue(userToVerify.isEnabled());
    }

    @Test
    public void getMostStudiedTopic_shouldReturn_emptyString_whenUserHasNoEnrolledCourses() {
        User mockUser = Helpers.createMockUser();
        mockUser.setNftCours(new ArrayList<>());

        String result = userService.getMostStudiedCourseTopic(mockUser);

        Assertions.assertEquals("", result);
    }

    @Test
    public void getMostStudiedTopic_shouldReturn_mostStudiedCourseTopic() {
        User mockUser = Helpers.createMockUser(21);
        Course javaCourse = Helpers.createMockCourse(EnumTopic.JAVA);
        Course javaScriptCourse = Helpers.createMockCourse(EnumTopic.JAVASCRIPT);
        NFT one = Helpers.createMockCourseEnrollment(javaCourse);
        NFT two = Helpers.createMockCourseEnrollment(javaCourse);
        NFT three = Helpers.createMockCourseEnrollment(javaScriptCourse);
        NFT four = Helpers.createMockCourseEnrollment(javaScriptCourse);
        NFT five = Helpers.createMockCourseEnrollment(javaScriptCourse);
        mockUser.setNftCours(List.of(one, two, three, four, five));

        String result = userService.getMostStudiedCourseTopic(mockUser);
        Assertions.assertEquals(result, "JAVASCRIPT");
    }

}
