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

//    void update(UserUpdateModel updateModel, User loggedUser);

    void grantTeacherRole(Principal initiator, User affectedUser);

    void updatePassword(Principal loggedUser, String newPassword, String passwordConfirmation);

    void updateProfileInfo(Principal loggedUser, UserUpdateModel updateModel);

    void delete(User toDelete,User loggedUser );

    boolean UserIsLogged(Principal principal);

    boolean checkUsernameIsUnique(User loggedUser, String username);

    UserModel getModelByUsername(String username);

    UserModel getModelById(int id);

    String getMostStudiedCourseTopic(User loggedUser);

    String getMostStudiedCourseTopic(UserModel userModel);

    User getById(int id, User loggedUser);

    User getLoggedUser(Principal principal);

    User getByEmail(String email);

    User getByUsername(String username);

    User create(RegisterUserModel register);

    List<User> getAll(User loggedUser);

    List<UserModel> getAllUserModels(User loggedUser);

    List<User> getAllByVerification(boolean areVerified, User loggedUser);

    @Override
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
}
