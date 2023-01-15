package com.henrique.virtualteacher.services.implementation;

import com.henrique.virtualteacher.entities.NFT;
import com.henrique.virtualteacher.entities.Role;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.exceptions.*;
import com.henrique.virtualteacher.models.*;
import com.henrique.virtualteacher.repositories.UserRepository;
import com.henrique.virtualteacher.services.interfaces.UserService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String UNAUTHORIZED_MESSAGE = "Unauthorized operation";

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final Logger logger;

    @Override
    public List<User> getAll(User loggedUser) {

        if (loggedUser.isNotTeacherOrAdmin()){
            throw new UnauthorizedOperationException(String.format("User with id: {%d} does not have permissions to get all Users", loggedUser.getId()));
        }
        return userRepository.findAll();
    }

    public List<UserModel> getAllUserModels(User loggedUser) {
        List<UserModel> modelList = new ArrayList<>();

        for (User current : getAll(loggedUser)) {
            modelList.add(new UserModel(loggedUser));
        }
        return modelList;
    }

    public void enableUser(int userToVerifyId) {
        User toVerify = getById(userToVerifyId);
        toVerify.setEnabled(true);
        userRepository.save(toVerify);
    }

    @Override
    public User getById(int id, User loggedUser) {

        if (id != loggedUser.getId() && (loggedUser.isNotTeacherOrAdmin())){
            throw new UnauthorizedOperationException("");
        }
         return userRepository.findById(id)
                 .orElseThrow(() -> new EntityNotFoundException("User", "Id", String.valueOf(id)));
    }

    @Override
    public UserModel getModelByUsername(String username) {
        User userToGet = getByEmail(username);
        return mapToModel(userToGet);
    }

    @Override
    public UserModel getModelById(int id) {
        User userToGet = getById(id);
        return mapToModel(userToGet);
    }

    private UserModel mapToModel(User user) {
        UserModel usermodel = new UserModel();
        usermodel.setRolesList(user.getRoles());
        usermodel.setLastname(user.getLastName());
        usermodel.setFirstname(user.getFirstName());
        usermodel.setEmail(user.getEmail());
        usermodel.setUsername(user.getUsername());
        usermodel.setAssignments(user.getAssignments());
        usermodel.setCompletedLectures(user.getCompletedLectures());
        usermodel.setCompletedCourses(user.getCompletedCourses());
        usermodel.setOwnedNftCourses(user.getNftCourses());
        usermodel.setProfilePicture(user.getProfilePicture());
        return usermodel;
    }

    @Override
    public boolean checkUsernameIsUnique(User loggedUser, String newUsername) {
        try {
            Optional<User> existingUser = userRepository.findByUsername(newUsername);
            return loggedUser.getUsername().equalsIgnoreCase(newUsername);
        } catch (EntityNotFoundException e) {
            return true;
        }
    }

    @Override
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User", "Email", email));
    }


    @Override
    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User", "Username", username));
    }

    @Override
    public List<User> getAllByVerification(boolean verification, User loggedUser) {
        if (loggedUser.isAdmin() || loggedUser.isTeacher()) {
            return userRepository.findAllByEnabled(verification);
        }
        throw new UnauthorizedOperationException("User","Id", loggedUser.getId(),"get","Users", "enabled");
    }


    @Override
    public User create(RegisterUserModel register) {
        checkRegistrationMailIsUnique(register.getEmail());
        checkPasswordMeetsRequirements(register.getPassword());
        checkPasswordsAreEqual(register.getPassword(), register.getPasswordConfirm());

       User user = mapFromRegisterModel(register);
       return userRepository.save(user);
    }


    public void verifyLoginInfo(String email, String password) {
        User existingUser = getByEmail(email);
        if (!encoder.matches(password, existingUser.getPassword())) {
            throw new ImpossibleOperationException("Password is incorrect");
        }
        else if (!existingUser.isEnabled()) {
            throw new AuthenticationException(String.format("User with id: %d, is not email verified", existingUser.getId()));
        }
    }

    @Override
    public void grantTeacherRole(Principal initiator, User affectedUser) {
        if (affectedUser.isTeacher()) {
            throw new ImpossibleOperationException(String.format("User %s already contains the teacher role", affectedUser.getUsername()));
        }
        verifyUserIsAllowed(affectedUser, getLoggedUser(initiator));
        addTeacherRoleToUser(affectedUser);
        userRepository.save(affectedUser);
        logger.info(String.format("TEACHER ROLE has been granted to user with id %d", affectedUser.getId()));
    }

    private void addTeacherRoleToUser(User roleReceiver) {
        roleReceiver.getRoles().add(new Role(2, EnumRoles.TEACHER));
    }


//    @Override
//    public void update(UserUpdateModel updateModel, User loggedUser) {
//
//
//        verifyUserIsAllowed(userBeingUpdated, loggedUser);
//        checkUpdateEmailIsUnique(userBeingUpdated, loggedUser);
//        checkPasswordMeetsRequirements(updateModel.getPassword());
//        checkPasswordsAreEqual(updateModel.getPassword(), updateModel.getPasswordConfirm());
//
//        mapFromUserUpdateModel(updateModel, userBeingUpdated);
//
//        userRepository.save(userBeingUpdated);
//    }

    @Override
    public void updatePassword(Principal loggedUser, String newPassword, String passwordConfirmation) {

        User user = getLoggedUser(loggedUser);
        checkPasswordsAreEqual(newPassword, passwordConfirmation);
        checkPasswordMeetsRequirements(newPassword);
        setNewUserPassword(newPassword, user);

        userRepository.save(user);
        logger.info(String.format("User: %s, has changed his password successfully",user.getUsername()));
        //todo test
    }

    @Override
    public void updateProfileInfo(Principal loggedUser, UserUpdateModel updateModel) {

        User user = getLoggedUser(loggedUser);

        checkUsernameIsUnique(user, updateModel.getUsername());
        checkStringDoesNotContainSpecialSymbols(updateModel.getUsername(), Optional.of("Username"));
        checkStringContainsOnlyLetters(updateModel.getFirstname(), Optional.of("Firstname"));
        checkStringContainsOnlyLetters(updateModel.getLastname(), Optional.of("Lastname"));
        mapFromUserUpdateModel(updateModel, user);

        userRepository.save(user);
        logger.info(String.format("User with email: {%s}, has been updated", user.getEmail()));
        //todo test
    }

    @Override
    public void delete(User toDelete, User loggedUser) {
        verifyUserIsAllowed(toDelete, loggedUser);

        toDelete.getCompletedLectures().clear();
        toDelete.getNftCourses().clear();

        //fixme -> will need to delete also the comments, ratings and assignments
        userRepository.delete(toDelete);
    }

    public String getMostStudiedCourseTopic(User loggedUser) {

        if (loggedUser.getNftCourses().size() == 0){
            return "";
        }

        List<NFT> sortedEnrolledCourses = loggedUser.getNftCourses().stream().
                sorted(Comparator.comparing(object -> object.getCourse().getTopic().name())).collect(Collectors.toList());

        int maxSequence = 1;
        EnumTopic mostStudiedTopic = sortedEnrolledCourses.get(0).getCourse().getTopic();

        int currentSequence = 1;
        EnumTopic lastEntry = sortedEnrolledCourses.get(0).getCourse().getTopic();

        for (int i = 1; i < sortedEnrolledCourses.size() ; i++) {

            EnumTopic currentTopic = sortedEnrolledCourses.get(i).getCourse().getTopic();

            if (lastEntry == currentTopic) {
                currentSequence++;
                if (currentSequence > maxSequence) {
                    mostStudiedTopic = currentTopic;
                }
            }
            else {
                currentSequence = 1;
                lastEntry = currentTopic;
            }
        }
        return mostStudiedTopic.name();
    }

    @Override
    public String getMostStudiedCourseTopic(UserModel userModel) {
        User user = getByEmail(userModel.getEmail());
        return getMostStudiedCourseTopic(user);
    }

    private User mapFromRegisterModel(RegisterUserModel register) {
        User newUser = new User();

        newUser.setFirstName(register.getFirstName());
        newUser.setLastName(register.getLastName());
        newUser.setEmail(register.getEmail());
        newUser.setUsername(register.getUsername());
        newUser.setPassword(encoder.encode(register.getPassword()));
        newUser.setRoles(List.of(new Role(1, EnumRoles.STUDENT)));
        newUser.setEnabled(false);

        return newUser;
    }

    private void checkPasswordsAreEqual(String password, String passwordConfirm) {
        if (!password.equals(passwordConfirm)) {
            throw new ImpossibleOperationException("Password and password Confirm must be identical");
        }
    }

    private void checkPasswordMeetsRequirements(String password) {
        Pattern p = Pattern.compile("(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{5,}");
        Matcher m = p.matcher(password);

        if (!m.matches()){
            throw new ImpossibleOperationException("Password does not meet requirements");
        }
    }

    private void verifyUserIsAllowed(User userBeingAccessed, User loggedUser) {
        if (userBeingAccessed.getId() != loggedUser.getId()) {
            if (!loggedUser.isAdmin() && !loggedUser.isTeacher()) {
                throw new UnauthorizedOperationException("User","email", loggedUser.getEmail(),"get", "User","email", userBeingAccessed.getEmail());
            }
        }
    }

    private void checkUpdateEmailIsUnique(User userBeingUpdated, User loggedUser) {

        try {
            User existingUser = getByEmail(userBeingUpdated.getEmail());
            if (existingUser.getId() !=  loggedUser.getId()) {
                throw new DuplicateEntityException("User", "email", userBeingUpdated.getEmail());
            }
        } catch (EntityNotFoundException ignored) {}
    }

    private void checkRegistrationMailIsUnique(String email) {
        try {
            User user = getByEmail(email);
            throw new DuplicateEntityException("User", "email", email);
        } catch (EntityNotFoundException ignored) {}

    }

    private User getById(int id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User", "Id", String.valueOf(id)));
    }

    private void mapFromUserUpdateModel(UserUpdateModel updateModel, User user) {
        user.setFirstName(updateModel.getFirstname());
        user.setLastName(updateModel.getLastname());
        user.setUsername(updateModel.getUsername());
    }

    private void setNewUserPassword(String password, User user) {
        user.setPassword(encoder.encode(password));
    }

    @Override
    public boolean UserIsLogged(Principal principal) {
        return principal != null;
    }

    @Override
    public User getLoggedUser(Principal principal) { // TODO: Need to know how to mock principal
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new AuthenticationException("User is not logged in "));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new EntityNotFoundException("User", "email", username));

        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), mapRolesToAuthorities(user.getRoles()));
    }

    private List<GrantedAuthority> mapRolesToAuthorities(List<Role> roles) {
        return roles.stream().map(role -> new SimpleGrantedAuthority(role.getRole().name()))
                .collect(Collectors.toList());
    }

    private void checkStringContainsOnlyLetters(String value, Optional<String> valueType) {
        for (char c : value.toCharArray()) {
            if (!Character.isLetter(c)) {

                if (valueType.isPresent()) {
                    throw new ImpossibleOperationException(String.format("%s can only contain letters", valueType));
                }
                throw new ImpossibleOperationException("Value can only contain letters");
            }
        }
    }

    private void checkStringDoesNotContainSpecialSymbols(String value, Optional<String> valueType) {

        for (char c : value.toCharArray()) {
            if (!Character.isLetterOrDigit(c) && c == '_') {
                if (valueType.isPresent()) {
                    throw new ImpossibleOperationException(String.format("%s should only contain digits, letters and underscore",valueType));
                }
                throw new ImpossibleOperationException("Value should have only digits, letters and underscore");
            }
        }
    }

}
