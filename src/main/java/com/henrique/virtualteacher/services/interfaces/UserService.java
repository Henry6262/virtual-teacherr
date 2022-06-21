package com.henrique.virtualteacher.services.interfaces;

import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.models.RegisterUserModel;
import com.henrique.virtualteacher.models.UserModel;
import com.henrique.virtualteacher.models.UserUpdateModel;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.security.Principal;
import java.util.List;

public interface UserService extends UserDetailsService {

    void enableUser(int userToVerifyId);

    void verifyLoginInfo(String email, String password);

    void update(UserUpdateModel updateModel, User loggedUser);

    void delete(User toDelete,User loggedUser );

    boolean UserIsLogged(Principal principal);

    UserModel getModelByUsername(String username);

    UserModel getModelById(int id);

    User getById(int id, User loggedUser);

    User getLoggedUser(Principal principal);

    User getByEmail(String email);

    User create(RegisterUserModel register);

    String getMostStudiedCourseTopic(User loggedUser);

    String getMostStudiedCourseTopic(UserModel userModel);

    List<User> getAll(User loggedUser);

    List<User> getAllByVerification(boolean areVerified, User loggedUser);

    @Override
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
}
