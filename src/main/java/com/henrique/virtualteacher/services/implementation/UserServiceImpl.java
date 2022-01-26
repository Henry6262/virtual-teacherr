package com.henrique.virtualteacher.services.implementation;

import com.henrique.virtualteacher.entities.Lecture;
import com.henrique.virtualteacher.entities.Role;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.exceptions.DuplicateEntityException;
import com.henrique.virtualteacher.exceptions.EntityNotFoundException;
import com.henrique.virtualteacher.exceptions.ImpossibleOperationException;
import com.henrique.virtualteacher.exceptions.UnauthorizedOperationException;
import com.henrique.virtualteacher.models.EnumRoles;
import com.henrique.virtualteacher.models.RegisterUserModel;
import com.henrique.virtualteacher.models.UserUpdateModel;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
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
    public User getById(int id, User loggedUser) {


        if (id != loggedUser.getId() && (loggedUser.isTeacher() ||loggedUser.isAdmin())){
            throw new UnauthorizedOperationException("");
        }

         return userRepository.findById(id)
                 .orElseThrow(() -> new EntityNotFoundException("User", "Id", String.valueOf(id)));
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
        checkEmailIsUnique(register.getEmail(), Optional.empty());
        checkPasswordMeetsRequirements(register.getPassword());
        checkPasswordsAreEqual(register.getPassword(), register.getPasswordConfirm());

       User user = mapFromRegisterModel(register);

       userRepository.save(user);
       return user;
    }

    public boolean verifyLoginInfo(String email, String password) {
        User existingUser = getByEmail(email);
        return encoder.matches(password, existingUser.getPassword());
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

    private void checkEmailIsUnique(String email, Optional<String> loggedUserEmail) {

        try {
            userRepository.findByEmail(email)
                    .orElseThrow(() -> new EntityNotFoundException("User", "email", email));
        } catch (EntityNotFoundException e) {
            return;
        }

        if (loggedUserEmail.isPresent()) {
            if (email.equals(loggedUserEmail.get())) {
                return;
            }
        }
        throw new DuplicateEntityException("User", "email", email);
    }

    @Override
    public void update(UserUpdateModel updateModel, User loggedUser) {

        User userBeingUpdated = getByEmail(updateModel.getEmail());

        verifyUserIsAllowed(userBeingUpdated, loggedUser);
        checkPasswordMeetsRequirements(updateModel.getPassword());
        checkPasswordsAreEqual(updateModel.getPassword(), updateModel.getPasswordConfirm());

        mapFromUserUpdateModel(updateModel, userBeingUpdated);

        userRepository.save(userBeingUpdated);
    }

    private void mapFromUserUpdateModel(UserUpdateModel updateModel, User user) {

        user.setPassword(encoder.encode(updateModel.getPassword()));
    }

    private void verifyUserIsAllowed(User userBeingAccessed, User loggedUser) {
        if (!userBeingAccessed.getEmail().equals(loggedUser.getEmail())
                && (!loggedUser.isAdmin() || !loggedUser.isTeacher())) {
            throw new UnauthorizedOperationException("User","email", loggedUser.getEmail(),"get", "User","email", userBeingAccessed.getEmail());
        }
    }


    @Override
    @Transactional
    public void delete(User toDelete, User loggedUser) {
        verifyUserIsAllowed(toDelete, loggedUser);

        userRepository.delete(toDelete);
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
