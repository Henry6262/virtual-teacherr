package com.henrique.virtualteacher.services.interfaces;

import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.entities.VerificationToken;
import com.henrique.virtualteacher.models.VerificationTokenModel;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface VerificationTokenService {

    VerificationTokenModel getById(int id, User loggedUser);

    VerificationTokenModel getByTransactionId(User loggedUser, int id);

    VerificationTokenModel getVerificationToken(String verificationToken);

    VerificationTokenModel getTransactionVerificationToken(String verificationToken, User loggedUser);

    List<VerificationTokenModel> getAllForUser(User loggedUser, int userToGetId);

    VerificationTokenModel create(User loggedUser, HttpServletRequest request);

    void delete(VerificationToken toDelete, User initiator);
}
