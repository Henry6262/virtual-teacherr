package com.henrique.virtualteacher.services.Implementation;

import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.Transaction;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.entities.Wallet;
import com.henrique.virtualteacher.exceptions.DuplicateEntityException;
import com.henrique.virtualteacher.exceptions.EntityNotFoundException;
import com.henrique.virtualteacher.exceptions.ImpossibleOperationException;
import com.henrique.virtualteacher.exceptions.UnauthorizedOperationException;
import com.henrique.virtualteacher.models.VerificationTokenModel;
import com.henrique.virtualteacher.repositories.NFTCourseRepository;
import com.henrique.virtualteacher.repositories.WalletRepository;
import com.henrique.virtualteacher.services.Helpers;
import com.henrique.virtualteacher.services.implementation.WalletServiceImpl;
import com.henrique.virtualteacher.services.interfaces.NFTCourseService;
import com.henrique.virtualteacher.services.interfaces.TransactionService;
import com.henrique.virtualteacher.services.interfaces.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class WalletServiceImplTests {

    @Mock
    UserService userService;
    @Mock
    WalletRepository walletRepository;
    @Mock
    Logger logger;
    @Mock
    TransactionService transactionService;
    @Mock
    NFTCourseRepository NFTCourseRepository;
    @Mock
    NFTCourseService NFTCourseService;

    @InjectMocks
    WalletServiceImpl walletService;


    @Test
    public void getById_should_throwException_when_walletDoesNotExist(){
        User mockUser = Helpers.createMockUser();
        Wallet mockWallet = Helpers.createMockWallet(mockUser);

        Assertions.assertThrows(EntityNotFoundException.class, () -> walletService.getById(mockWallet.getId(), mockUser));
    }

    @Test
    public void getById_shouldReturnCorrectEntity() {
        User initiator = Helpers.createMockUser();
        Wallet mockWallet = Helpers.createMockWallet(initiator);

        Mockito.when(walletRepository.getById(mockWallet.getId())).thenReturn(Optional.of(mockWallet));

        Wallet result = walletService.getById(mockWallet.getId(), initiator);

        Assertions.assertAll(
                () -> Assertions.assertEquals(result.getId(), mockWallet.getId()),
                () -> Assertions.assertEquals(result.getOwner().getId(), mockWallet.getOwner().getId()),
                () -> Assertions.assertEquals(result.getBalance(), mockWallet.getBalance())
        );
    }

    @Test
    public void getById_should_throwException_when_userIsNot_walletOwner() {
        User walletOwner = Helpers.createMockUser();
        Wallet mockWallet = Helpers.createMockWallet(walletOwner);

        User initiator = Helpers.createMockUser();
        initiator.setId(21);

        Mockito.when(walletRepository.getById(mockWallet.getId())).thenReturn(Optional.of(mockWallet));

        Assertions.assertThrows(UnauthorizedOperationException.class, () -> walletService.getById(mockWallet.getId(),initiator));
    }

    @Test
    public void getLoggedUserWallet_should_throwException_when_walletDoesNotExist() {
        Assertions.assertThrows(EntityNotFoundException.class, () -> walletService.getLoggedUserWallet(Helpers.createMockUser()));
    }

    @Test
    public void getLoggedUserWallet_should_throwException_when_initiatorIsNot_walletOwner() {
        Wallet mockWallet = Helpers.createMockWallet(Helpers.createMockUser());

        User initiator = Helpers.createMockUser();
        initiator.setId(21);

        Mockito.when(walletRepository.getByOwnerEmail(initiator.getEmail())).thenReturn(Optional.of(mockWallet));

        Assertions.assertThrows(UnauthorizedOperationException.class, () -> walletService.getLoggedUserWallet(initiator));
    }

    @Test
    public void create_shouldThrowException_when_userAlreadyHas_existingWallet() {
        User mockUser = Helpers.createMockUser();
        Wallet mockWallet = Helpers.createMockWallet(mockUser);

        Mockito.when(walletRepository.getByOwnerEmail(mockUser.getEmail())).thenReturn(Optional.of(mockWallet));

        Assertions.assertThrows(DuplicateEntityException.class, () -> walletService.create(mockUser));
    }

    @Test
    public void create_shouldReturnCreatedEntity() {
        User mockUser = Helpers.createMockUser();
        Wallet mockWallet = Helpers.createMockWallet(mockUser);

        Mockito.when(walletRepository.save(Mockito.any(Wallet.class))).thenReturn(mockWallet);
        Wallet result = walletService.create(mockUser);

        Assertions.assertAll(
                () -> Assertions.assertEquals(mockWallet.getId(), result.getId()),
                () -> Assertions.assertEquals(mockWallet.getOwner().getId(), result.getOwner().getId())
        );
    }

    @Test
    public void deposit_shouldThrowException_when_depositIsBelowMinimum() {
        User mockUser = Helpers.createMockUser();
        Wallet mockWallet = Helpers.createMockWallet(mockUser);

        Assertions.assertThrows(ImpossibleOperationException.class, () -> walletService.deposit(mockUser, BigDecimal.valueOf(7)));
    }

    @Test
    public void deposit_shouldAddFundsToWallet_when_AmountIsValid() {

        User mockUser = Helpers.createMockUser();
        Wallet mockWallet = Helpers.createMockWallet(mockUser);

        Mockito.when(walletRepository.getByOwnerEmail(mockUser.getEmail())).thenReturn(Optional.of(mockWallet));

        walletService.deposit(mockUser, BigDecimal.valueOf(30));

        Assertions.assertEquals(BigDecimal.valueOf(30),mockWallet.getBalance());
    }

    @Test
    public void purchaseCourse_shouldThrowException_when_UserDoesNot_haveEnoughFunds() {
        User mockUser = Helpers.createMockUser();
        Wallet mockWallet = Helpers.createMockWallet(mockUser);
        Course course = Helpers.createMockCourse();

        Mockito.when(walletRepository.getByOwnerEmail(mockUser.getEmail())).thenReturn(Optional.of(mockWallet));

        Assertions.assertThrows(ImpossibleOperationException.class, () -> walletService.mintNFT(course, mockUser));
    }

    @Test
    public void purchaseCourse_shouldRetrieveFromWallet_andPurchaseCourse() {
        User mockUser = Helpers.createMockUser();
        Wallet userWallet = Helpers.createMockWallet(mockUser, BigDecimal.valueOf(300));
        Course mockCourse = Helpers.createMockCourse();

        mockCourse.setMintPrice(BigDecimal.valueOf(50));

        Mockito.when(walletRepository.getByOwnerEmail(mockUser.getEmail())).thenReturn(Optional.of(userWallet));

        walletService.mintNFT(mockCourse, mockUser);

        Assertions.assertEquals(250.0, userWallet.getBalance().doubleValue());
    }

    @Test
    public void handleTransactionVerification_shouldThrowException_whenInitiator_isNotTokenVerifier() {
        User initiator = Helpers.createMockUser(21);

        User tokenOwner = Helpers.createMockUser(99);
        Transaction transaction = Helpers.createMockTransaction(tokenOwner,Helpers.createMockUser(2));
        VerificationTokenModel tokenModel = Helpers.createVerificationTokenModel(tokenOwner);

        Assertions.assertThrows(UnauthorizedOperationException.class, () ->
                walletService.verifyPendingDepositOrTransfer(initiator, transaction, tokenModel));
    }

    @Test
    public void handleTransactionVerification_shouldAddFundsToRecipientWallet_and_retrieveFromSenderWallet() {
        User initiator = Helpers.createMockUser(21);
        Wallet senderWallet = Helpers.createMockWallet(initiator);
        senderWallet.setBalance(BigDecimal.valueOf(100));

        User recipient = Helpers.createMockUser(1);
        Wallet recipientWallet = Helpers.createMockWallet(recipient);
        recipientWallet.setBalance(BigDecimal.valueOf(100));

        double senderWalletInitialFunds = senderWallet.getBalance().doubleValue();
        double recipientWalletInitialFunds = recipientWallet.getBalance().doubleValue();

        Transaction transaction = Helpers.createTransaction(senderWallet, recipientWallet, BigDecimal.valueOf(25));
        VerificationTokenModel tokenModel = Helpers.createTransactionTokenModel(transaction);

        Mockito.when(transactionService.getById(transaction.getId(), initiator)).thenReturn(transaction);

        walletService.verifyPendingDepositOrTransfer(initiator,transaction, tokenModel);

        Assertions.assertAll(
                () -> Assertions.assertEquals(75.0, senderWallet.getBalance().doubleValue()),
                () -> Assertions.assertEquals(125.0, recipientWallet.getBalance().doubleValue())
        );
    }

    @Test
    public void handleDepositVerification_shouldThrowExceptionWhen_initiatorIsNotTokenVerifier() {
        User initiator = Helpers.createMockUser(21);

        User tokenOwner = Helpers.createMockUser(99);
        Wallet tokenOwnerWallet = Helpers.createMockWallet(tokenOwner);
        Transaction transaction = Helpers.createDepositTransaction(tokenOwnerWallet, BigDecimal.valueOf(50));
        VerificationTokenModel tokenModel = Helpers.createVerificationTokenModel(tokenOwner);

        Assertions.assertThrows(UnauthorizedOperationException.class, () ->
                walletService.verifyPendingDepositOrTransfer(initiator, transaction, tokenModel));
    }

    @Test
    public void handleDepositVerification_shouldDepositFunds_toWallet() {
        User initiator = Helpers.createMockUser(21);
        Wallet userWallet = Helpers.createMockWallet(initiator);
        Transaction transaction = Helpers.createDepositTransaction(userWallet, BigDecimal.valueOf(50));
        VerificationTokenModel tokenModel = Helpers.createTransactionTokenModel(transaction);

        userWallet.setBalance(BigDecimal.ZERO);
        double initialWalletBalance = userWallet.getBalance().doubleValue();

        Mockito.when(transactionService.getById(transaction.getId(), initiator)).thenReturn(transaction);
        Mockito.when(walletRepository.getByOwnerEmail(initiator.getEmail())).thenReturn(Optional.of(userWallet));

        walletService.verifyPendingDepositOrTransfer(initiator, transaction, tokenModel);

        Assertions.assertEquals(50.0, userWallet.getBalance().doubleValue());
    }

    @Test
    public void deposit_shouldNotAddFunds_ifTransactionIsPending() {
        User mockUser = Helpers.createMockUser(21);
        Wallet mockWallet = Helpers.createMockWallet(mockUser);
        mockUser.setWallet(mockWallet);
        double initialAmount = mockWallet.getBalance().doubleValue();

        Mockito.when(walletRepository.getByOwnerEmail(mockUser.getEmail())).thenReturn(Optional.of(mockWallet));

        walletService.deposit(mockUser, BigDecimal.valueOf(300));

        Assertions.assertEquals(initialAmount, mockWallet.getBalance().doubleValue());
    }

    @Test
    public void send_shouldThrowException_whenSenderDoesNotHave_enoughFunds() {
        User sender = Helpers.createMockUser(21);
        Wallet senderWallet = Helpers.createMockWallet(sender);
        sender.setWallet(senderWallet);

        User recipient = Helpers.createMockUser(99);
        Wallet recipientWallet = Helpers.createMockWallet(recipient);
        recipient.setWallet(recipientWallet);

        Assertions.assertThrows(ImpossibleOperationException.class, () -> walletService.send(sender, recipient, BigDecimal.valueOf(300)));
    }

    @Test
    public void send_shouldNot_retrieveAndAddFunds_IfTransactionIsPending() {
        User sender = Helpers.createMockUser(21);
        Wallet senderWallet = Helpers.createMockWallet(sender);
        senderWallet.setBalance(BigDecimal.valueOf(500));
        sender.setWallet(senderWallet);

        User recipient = Helpers.createMockUser(99);
        Wallet recipientWallet = Helpers.createMockWallet(recipient);
        recipient.setWallet(recipientWallet);

        double senderInitialAmount = senderWallet.getBalance().doubleValue();
        double recipientInitialAmount = recipientWallet.getBalance().doubleValue();

        walletService.send(sender, recipient, BigDecimal.valueOf(200));

        Assertions.assertEquals(senderInitialAmount, senderWallet.getBalance().doubleValue());
        Assertions.assertEquals(recipientInitialAmount, recipientWallet.getBalance().doubleValue());
    }

    @Test
    public void send_shouldRetrieveFromSenderWallet_andAddToRecipientWallet() {
        User sender = Helpers.createMockUser(21);
        Wallet senderWallet = Helpers.createMockWallet(sender);
        senderWallet.setBalance(BigDecimal.valueOf(200));
        sender.setWallet(senderWallet);

        User recipient = Helpers.createMockUser(99);
        Wallet recipientWallet = Helpers.createMockWallet(recipient);
        recipient.setWallet(recipientWallet);
        recipientWallet.setBalance(BigDecimal.valueOf(50));

        walletService.send(sender, recipient, BigDecimal.valueOf(100));

        Assertions.assertEquals(100, senderWallet.getBalance().doubleValue());
        Assertions.assertEquals(150, recipientWallet.getBalance().doubleValue());
    }




}
