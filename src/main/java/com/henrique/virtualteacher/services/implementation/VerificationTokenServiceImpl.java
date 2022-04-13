package com.henrique.virtualteacher.services.implementation;

import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.entities.VerificationToken;
import com.henrique.virtualteacher.exceptions.EntityNotFoundException;
import com.henrique.virtualteacher.exceptions.UnauthorizedOperationException;
import com.henrique.virtualteacher.models.VerificationTokenModel;
import com.henrique.virtualteacher.repositories.VerificationTokenRepository;
import com.henrique.virtualteacher.services.interfaces.UserService;
import com.henrique.virtualteacher.services.interfaces.VerificationTokenService;
import com.henrique.virtualteacher.utils.MailSender;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Service
public class VerificationTokenServiceImpl implements VerificationTokenService {

    private final VerificationTokenRepository tokenRepository;
    private final UserService userService;
    private final Logger logger;
    private final ModelMapper mapper;
    private final MailSender mailSender;

    @Autowired
    public VerificationTokenServiceImpl(VerificationTokenRepository tokenRepository,
                                        UserService userService,
                                        Logger logger,
                                        ModelMapper modelMapper,
                                        MailSender mailSender) {
        this.tokenRepository  = tokenRepository;
        this.userService = userService;
        this.logger = logger;
        this.mapper = modelMapper;
        this.mailSender = mailSender;
    }

    @Override
    public VerificationTokenModel getByTransactionId(User loggedUser, int id) {
        VerificationToken token = tokenRepository.findByTransactionId(id)
                .orElseThrow(() -> new EntityNotFoundException("VerificationToken", "transactionId", String.valueOf(id)));
        return mapToModel(token);
        //todo: make test
    }

    @Override
    public VerificationTokenModel getById(int id, User loggedUser) {
        VerificationToken verificationToken = tokenRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Verification token with id %d, does not exist",id)));
        checkUserIsAuthorized(loggedUser, verificationToken);
        return mapToModel(verificationToken);
    }

    private VerificationTokenModel mapToModel(VerificationToken token) {
        VerificationTokenModel tokenModel = new VerificationTokenModel();
        tokenModel.setVerifierId(token.getVerifier().getId());
        tokenModel.setToken(token.getToken());
        tokenModel.setId(token.getId());
        tokenModel.setExpirationTime(token.getExpirationTime());

        return tokenModel;
    }

    private List<VerificationTokenModel> mapAllToModel(List<VerificationToken> tokens) {
        List<VerificationTokenModel> modelList = new ArrayList<>();

        for (VerificationToken current : tokens) {
             VerificationTokenModel model = mapToModel(current);
             modelList.add(model);
        }
        return modelList;
    }

    private void checkUserIsAuthorized(User loggedUser, VerificationToken verificationToken) {
        User tokenVerifier = verificationToken.getVerifier();
        if (tokenVerifier.getId() != loggedUser.getId()) {
            throw new UnauthorizedOperationException(String.format("User with id: %d, is not designated to access Verification token with id: %d", loggedUser.getId(), verificationToken.getId()));
        }
    }

    @Override
    public VerificationTokenModel getVerificationToken(String verificationToken) {
        VerificationToken token = tokenRepository.findByToken(verificationToken)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Verification token with value: %s, does not exist", verificationToken)));
        return mapToModel(token);
    }

    @Override
    public VerificationTokenModel getTransactionVerificationToken(String verificationToken, User loggedUser) {
        VerificationToken token = tokenRepository.findByToken(verificationToken)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Verification token with value: %s, does not exist", verificationToken)));
        checkUserIsAuthorized(loggedUser, token);
        return mapToModel(token);
    }

    @Override
    public List<VerificationTokenModel> getAllForUser(User loggedUser, int userToGetId) {
        User userToGet = userService.getById(userToGetId, loggedUser);
        return mapAllToModel(tokenRepository.findAllByVerifierId(userToGetId));
    }

    @Override
    public VerificationTokenModel create(User loggedUser, HttpServletRequest request) {

        VerificationToken verificationToken = new VerificationToken(loggedUser);
        verificationToken = tokenRepository.save(verificationToken);

        sendVerificationMail(loggedUser, verificationToken, request);
        logger.info(String.format("Verification token has been create for user with id: %d", loggedUser.getId()));
        return mapToModel(verificationToken);
    }

    private void sendVerificationMail(User recipient, VerificationToken token, HttpServletRequest request) {
        SimpleMailMessage mailMessage = MailSender.createRegistrationMail(recipient, token, request);
        mailSender.sendMail(mailMessage);
        logger.info(String.format("Email has been sent successfully to the user mail: {%s}", recipient.getEmail()));
    }

    @Override
    public void delete(VerificationTokenModel toDelete) {
        VerificationToken token = getByToken(toDelete.getToken());
        tokenRepository.delete(token);
        logger.info(String.format("Verification token with id: %s, has been deleted", toDelete.getToken()));
    }

    private VerificationToken getByToken(String token) {
        return tokenRepository.findByToken(token)
                .orElseThrow(() -> new EntityNotFoundException("Verification Token", "token", token));
    }

}
