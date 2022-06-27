package com.henrique.virtualteacher.services.implementation;

import com.henrique.virtualteacher.entities.NFT;
import com.henrique.virtualteacher.entities.Role;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.exceptions.*;
import com.henrique.virtualteacher.models.*;
import com.henrique.virtualteacher.repositories.UserRepository;
import com.henrique.virtualteacher.services.interfaces.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final String UNAUTHORIZED_MESSAGE = "Unauthorized operation";

    private final UserRepository userRepository;
    private final ModelMapper mapper;
    private final PasswordEncoder encoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           ModelMapper mapper,
                           BCryptPasswordEncoder encoder) {

        this.userRepository = userRepository;
        this.mapper = mapper;
        this.encoder = encoder;
    }

    @Override
    public List<User> getAll(User loggedUser) {

        if (loggedUser.isNotTeacherOrAdmin()){
            throw new UnauthorizedOperationException(String.format("User with id: {%d} does not have permissions to get all Users", loggedUser.getId()));
        }
        return userRepository.findAll();
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
        usermodel.setAssignments(user.getAssignments());
        usermodel.setCompletedLectures(user.getCompletedLectures());
        usermodel.setCompletedCourses(user.getCompletedCourses());
        usermodel.setOwnedNftCourses(user.getNftCours());
        usermodel.setProfilePicture(user.getProfilePicture());
        return usermodel;
    }

    @Override
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User", "Email", email));
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
    public void update(UserUpdateModel updateModel, User loggedUser) {

        User userBeingUpdated = getByEmail(updateModel.getEmail());

        verifyUserIsAllowed(userBeingUpdated, loggedUser);
        checkUpdateEmailIsUnique(userBeingUpdated, loggedUser);
        checkPasswordMeetsRequirements(updateModel.getPassword());
        checkPasswordsAreEqual(updateModel.getPassword(), updateModel.getPasswordConfirm());

        mapFromUserUpdateModel(updateModel, userBeingUpdated);

        userRepository.save(userBeingUpdated);
    }


    @Override
    public void delete(User toDelete, User loggedUser) {
        verifyUserIsAllowed(toDelete, loggedUser);

        toDelete.getCompletedLectures().clear();
        toDelete.getNftCours().clear();

        //fixme -> will need to delete also the comments, ratings and assignments
        userRepository.delete(toDelete);
    }

    public String getMostStudiedCourseTopic(User loggedUser) {

        if (loggedUser.getNftCours().size() == 0){
            return "";
        }

        List<NFT> sortedEnrolledCourses = loggedUser.getNftCours().stream().
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
        mapper.map(register, newUser);
        newUser.setPassword(encoder.encode(newUser.getPassword()));
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
        user.setPassword(encoder.encode(updateModel.getPassword()));
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
}
