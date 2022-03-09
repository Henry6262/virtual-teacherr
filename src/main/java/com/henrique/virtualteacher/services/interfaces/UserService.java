package com.henrique.virtualteacher.services.interfaces;

import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.models.RegisterUserModel;
import com.henrique.virtualteacher.models.UserUpdateModel;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.security.Principal;
import java.util.List;

public interface UserService extends UserDetailsService {

    boolean UserIsLogged(Principal principal);

    List<User> getAll(User loggedUser);

    User getById(int id, User loggedUser);

    User getByEmail(String email);

    List<User> getAllByVerification(boolean areVerified, User loggedUser);

    User create(RegisterUserModel register);

    void verifyLoginInfo(String email, String password);

    void update(UserUpdateModel updateModel, User loggedUser);

    void delete(User toDelete,User loggedUser );

    @Override
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
}
