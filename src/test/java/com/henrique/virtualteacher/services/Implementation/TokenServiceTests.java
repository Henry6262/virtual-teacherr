package com.henrique.virtualteacher.services.Implementation;

import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.entities.VerificationToken;
import com.henrique.virtualteacher.exceptions.EntityNotFoundException;
import com.henrique.virtualteacher.exceptions.UnauthorizedOperationException;
import com.henrique.virtualteacher.models.VerificationTokenModel;
import com.henrique.virtualteacher.repositories.VerificationTokenRepository;
import com.henrique.virtualteacher.services.Helpers;
import com.henrique.virtualteacher.services.implementation.VerificationTokenServiceImpl;
import com.henrique.virtualteacher.services.interfaces.UserService;
import com.henrique.virtualteacher.utils.MailSender;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class TokenServiceTests {

    @Mock
    VerificationTokenRepository tokenRepository;
    @Mock
    UserService userService;
    @Mock
    Logger logger;
    @Mock
    ModelMapper modelMapper;
    @Mock
    HttpServletRequest request;
    @Mock
    MailSender mailSender;

    @InjectMocks
    VerificationTokenServiceImpl tokenService;


    @Test
    public void getById_shouldThrowException_whenEntityNotFound() {
        VerificationToken mockToken = Helpers.createMockVerificationToken();

        Assertions.assertThrows(EntityNotFoundException.class, () -> tokenService.getById(mockToken.getId(), Helpers.createMockUser(21)));
    }

    @Test
    public void getById_shouldThrowException_whenInitiatorIsNotAuthorized() {
        User initiator = Helpers.createMockUser();
        VerificationToken verificationToken = Helpers.createMockVerificationToken(Helpers.createMockUser(21));

        Mockito.when(tokenRepository.findById(verificationToken.getId())).thenReturn(Optional.of(verificationToken));

        Assertions.assertThrows(UnauthorizedOperationException.class, () -> tokenService.getById(verificationToken.getId(), initiator));
    }

    @Test
    public void getById_shouldReturnEntity_whenInitiatorIsVerifier() {
        User initiator = Helpers.createMockUser(21);
        VerificationToken verificationToken = Helpers.createMockVerificationToken(initiator);

        Mockito.when(tokenRepository.findById(verificationToken.getId())).thenReturn(Optional.of(verificationToken));

        VerificationTokenModel result = tokenService.getById(verificationToken.getId(), initiator);
        Assertions.assertEquals(result.getClass().getName() , VerificationTokenModel.class.getName());
    }

    @Test
    public void getByVerificationToken_should_throwException_when_entityNotFound() {
        User initiator = Helpers.createMockUser(21);
        VerificationToken verificationToken = Helpers.createMockVerificationToken(initiator);

        Assertions.assertThrows(EntityNotFoundException.class, () -> tokenService.getVerificationToken(verificationToken.getToken()));
    }

    @Test
    public void getByVerificationToken_shouldCallRepository_whenExisting() {
        User initiator = Helpers.createMockUser();
        VerificationToken token = Helpers.createMockVerificationToken(initiator);

        Mockito.when(tokenRepository.findByToken(token.getToken())).thenReturn(Optional.of(token));

        tokenService.getVerificationToken(token.getToken());

        Mockito.verify(tokenRepository, Mockito.times(1)).findByToken(token.getToken());
    }

    @Test
    public void getByTransactionVerificationToken_shouldThrowException_when_entityNotFound() {
        User initiator = Helpers.createMockUser();
        VerificationToken token = Helpers.createMockVerificationToken(initiator);

        Assertions.assertThrows(EntityNotFoundException.class, () -> tokenService.getTransactionVerificationToken(token.getToken(), initiator));
    }

    @Test
    public void getByTransactionVerificationToken_shouldThrowException_when_initiatorIsNotVerifier() {
        User initiator = Helpers.createMockUser();
        VerificationToken verificationToken = Helpers.createMockVerificationToken(Helpers.createMockUser(21));

        Mockito.when(tokenRepository.findByToken(verificationToken.getToken())).thenReturn(Optional.of(verificationToken));

        Assertions.assertThrows(UnauthorizedOperationException.class, () -> tokenService.getTransactionVerificationToken
                (verificationToken.getToken(), initiator));
    }


    @Test
    public void getByTransactionVerificationToken_shouldReturnEntity_whenInitiatorIsReceiver() {
        User initiator = Helpers.createMockUser(21);
        VerificationToken verificatioNToken = Helpers.createMockVerificationToken(initiator);

        Mockito.when(tokenRepository.findByToken(verificatioNToken.getToken())).thenReturn(Optional.of(verificatioNToken));

        VerificationTokenModel result = tokenService.getVerificationToken(verificatioNToken.getToken());

        Assertions.assertAll(
                () ->   Assertions.assertEquals(verificatioNToken.getToken(), result.getToken()),
                () ->   Assertions.assertEquals(verificatioNToken.getVerifier().getId(), result.getVerifierId()));
    }

    @Test
    public void getAllForUser_shouldReturnModelList_whenUserIsAuthorized() {
        User initiator = Helpers.createMockUser(21);
        List<VerificationToken> tokenList = Helpers.createMockTokenList(initiator);

        Mockito.when(userService.getById(initiator.getId(), initiator)).thenReturn(initiator);
        Mockito.when(tokenRepository.findAllByVerifierId(initiator.getId())).thenReturn(tokenList);

        List<VerificationTokenModel> resultList = tokenService.getAllForUser(initiator, initiator.getId());

        Assertions.assertAll(
                () -> Assertions.assertEquals(tokenList.get(0).getToken(), resultList.get(0).getToken()),
                () -> Assertions.assertEquals(tokenList.get(1).getToken(), resultList.get(1).getToken())
        );
    }

    @Test
    public void create_shouldCallRepository_whenUserIsAuthenticated() {
        User loggedUser = Helpers.createMockUser(21);
        VerificationToken verificationToken = Helpers.createMockVerificationToken(loggedUser);

        Mockito.when(tokenRepository.save(Mockito.any(VerificationToken.class))).thenReturn(verificationToken);
        Mockito.when(MailSender.createRegistrationMail(loggedUser, verificationToken, null)).thenReturn(Mockito.any());

        VerificationTokenModel result = tokenService.create(loggedUser,null);

        Assertions.assertEquals(verificationToken.getToken(), result.getToken());
    }

    @Test
    public void delete_shouldThrowException_whenUserIsNotAuthorized() {
        User initiator = Helpers.createMockUser();
        VerificationToken verificationToken = Helpers.createMockVerificationToken(Helpers.createMockUser(21));

        Assertions.assertThrows(UnauthorizedOperationException.class, () -> tokenService.delete(verificationToken, initiator));
    }

    @Test
    public void delete_shouldCallRepository_whenInitiator_isAuthorized() {
        User initiator = Helpers.createMockTeacher();
        VerificationToken token = Helpers.createMockVerificationToken(Helpers.createMockUser(12));

        tokenService.delete(token, initiator);

        Mockito.verify(tokenRepository, Mockito.times(1)).delete(token);
    }

}
