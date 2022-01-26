package com.henrique.virtualteacher.services.interfaces;

import com.henrique.virtualteacher.entities.Lecture;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.models.RegisterUserModel;
import com.henrique.virtualteacher.models.UserUpdateModel;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Set;

public interface UserService extends UserDetailsService {

    List<User> getAll(User loggedUser);

    User getById(int id, User loggedUser);

    User getByEmail(String email);

    List<User> getAllByVerification(boolean areVerified, User loggedUser);

    User create(RegisterUserModel register);

    boolean verifyLoginInfo(String email, String password);

    void update(UserUpdateModel updateModel, User loggedUser);

    void delete(User toDelete,User loggedUser );

    @Override
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
}
