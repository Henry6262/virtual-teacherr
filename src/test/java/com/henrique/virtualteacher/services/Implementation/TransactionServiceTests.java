package com.henrique.virtualteacher.services.Implementation;

import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.Transaction;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.entities.Wallet;
import com.henrique.virtualteacher.exceptions.EntityNotFoundException;
import com.henrique.virtualteacher.exceptions.UnauthorizedOperationException;
import com.henrique.virtualteacher.models.TransactionStatus;
import com.henrique.virtualteacher.repositories.TransactionRepository;
import com.henrique.virtualteacher.services.Helpers;
import com.henrique.virtualteacher.services.implementation.TransactionServiceImpl;
import com.henrique.virtualteacher.services.interfaces.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTests {

    @Mock
    UserService userService;
    @Mock
    TransactionRepository transactionRepository;
    @Mock
    Logger logger;

    @InjectMocks
    TransactionServiceImpl transactionService;


    @Test
    void getById_should_throwException_whenEntityNotFound() {
        Assertions.assertThrows(EntityNotFoundException.class, () -> transactionService.getById(1, Helpers.createMockUser()));
    }

    @Test
    void getById_should_throwException_whenUserIsNotAuthorized() {
        User mockUser = Helpers.createMockUser();
        mockUser.setId(21);
        Transaction mockTransaction = Helpers.createMockTransaction();

        Mockito.when(transactionRepository.getById(mockTransaction.getId())).thenReturn(Optional.of(mockTransaction));

        Assertions.assertThrows(UnauthorizedOperationException.class, () -> transactionService.getById(mockTransaction.getId(), mockUser));
    }

    @Test
    void getById_should_return_whenInitiatorIsTeacher() {
        User initiator = Helpers.createMockUser();
        Transaction mockTransaction = Helpers.createMockTransaction();


        Mockito.when(transactionRepository.getById(mockTransaction.getId())).thenReturn(Optional.of(mockTransaction));

        Transaction response = transactionService.getById(mockTransaction.getId(), initiator);

        Assertions.assertEquals(mockTransaction, response);
    }

    @Test
    void getAllForUser_shouldThrowExceptionWhen_userIsNotAuthorized() {
        User mockUser = Helpers.createMockUser();
        mockUser.setId(21);

        Transaction mockTransaction = Helpers.createMockTransaction();
        User userToGet = mockTransaction.getRecipientWallet().getOwner();

        Mockito.when(userService.getById(userToGet.getId(), mockUser)).thenReturn(userToGet);
        Assertions.assertThrows(UnauthorizedOperationException.class, () -> transactionService.getAllForUser(mockUser, userToGet.getId()));
    }

    @Test
    public void getAllForUser_shouldReturnList_when_isTransactionParticipant() {
        User loggedUser = Helpers.createMockUser();
        User transactionsRecipient = Helpers.createMockUser();
        transactionsRecipient.setId(21);
        List<Transaction> mockTransactions = Helpers.createMockTransactionList(loggedUser, transactionsRecipient);

        Mockito.when(userService.getById(loggedUser.getId(), loggedUser)).thenReturn(loggedUser);
        Mockito.when(transactionRepository.getAllByRecipientWalletOwnerId(loggedUser.getId())).thenReturn(mockTransactions);

        List<Transaction> resultList = transactionService.getAllForUser(loggedUser, loggedUser.getId());

        Assertions.assertEquals(mockTransactions, resultList);
    }

    @Test
    public void getAllForCourse_should_throwException_when_initiatorIsNotAuthorized() {
        User mockUser = Helpers.createMockUser();
        mockUser.setId(21);
        Transaction mockTransaction = Helpers.createMockTransaction();
        Course mockCourse = mockTransaction.getPurchasedCourse();

        Assertions.assertThrows(UnauthorizedOperationException.class, () -> transactionService.getAllForCourse(mockCourse, mockUser));
    }

    @Test
    public void getAllForCourse_shouldReturnList_whenInitiatorIsTeacher() {
        User mockInitiator = Helpers.createMockTeacher();
        Course mockCourse = Helpers.createMockCourse();
        List<Transaction> mockTransactions = Helpers.createMockTransactionList(Helpers.createMockUser(), Helpers.createMockUser());

        Mockito.when(transactionRepository.getAllByPurchasedCourseId(mockCourse.getId())).thenReturn(mockTransactions);

        List<Transaction> result = transactionService.getAllForCourse(mockCourse, mockInitiator);
        Assertions.assertEquals(mockTransactions, result);
    }

    @Test
    public void getAllByStatus_ShouldThrowException_when_initiatorIsNotTeacherOrAdmin() {
        User mockUser = Helpers.createMockUser();
        Assertions.assertThrows(UnauthorizedOperationException.class, () -> transactionService.getAllByStatus(TransactionStatus.PENDING, mockUser));
    }

    @Test
    public void getAllByStatus_shouldReturnList_when_initiatorIsTeacher() {
        User mockTeacher = Helpers.createMockTeacher();
        User mockTransactionAssociate = Helpers.createMockUser();
        List<Transaction> mockTransactions = Helpers.createMockTransactionList(mockTransactionAssociate, mockTransactionAssociate);

        Mockito.when(transactionService.getAllByStatus(TransactionStatus.PENDING, mockTeacher)).thenReturn(mockTransactions);

        List<Transaction> result = transactionService.getAllByStatus(TransactionStatus.PENDING,mockTeacher);

        Assertions.assertEquals(mockTransactions, result);
    }

    @Test
    public void getAllByWallet_shouldThrowException_whenUserIsNot_ownerOrTeacher() {
        User mockUser = Helpers.createMockUser();
        User walletOwner = Helpers.createMockTeacher();
        Wallet mockWallet = Helpers.createMockWallet(walletOwner);

        Assertions.assertThrows(UnauthorizedOperationException.class, () -> transactionService.getAllByWallet(mockWallet, mockUser));
    }

    @Test
    public void getAllByWallet_shouldReturnList_when_initiatorIsOwner() {
        User walletOwner = Helpers.createMockUser();
        Wallet mockWallet = Helpers.createMockWallet(walletOwner);
        List<Transaction> mockTransactions = Helpers.createMockTransactionList(walletOwner, walletOwner);

        Mockito.when(transactionRepository.getAllBySenderWalletOwnerId(mockWallet.getId())).thenReturn(mockTransactions);

        List<Transaction> result = transactionService.getAllByWallet(mockWallet, walletOwner);

        Assertions.assertEquals(mockTransactions, result);
    }

    @Test
    public void create_shouldThrowException_when_initiatorIsNot_transactionAssociate() {
        User initiator = Helpers.createMockUser(21);
        Transaction toBeCreated = Helpers.createMockTransaction(Helpers.createMockUser(1), Helpers.createMockUser(2));

        Mockito.when(userService.getByEmail(toBeCreated.getSenderWallet().getOwner().getEmail())).thenReturn(toBeCreated.getSenderWallet().getOwner());
        Mockito.when(userService.getByEmail(toBeCreated.getRecipientWallet().getOwner().getEmail())).thenReturn(toBeCreated.getRecipientWallet().getOwner());

        Assertions.assertThrows(UnauthorizedOperationException.class, () -> transactionService.create(toBeCreated,initiator));
    }

    @Test
    public void create_shouldSaveEntity_when_initiatorIsTransactionAssociate() {
        User initiator = Helpers.createMockUser();
        Transaction mockTransaction = Helpers.createMockTransaction(initiator, initiator);

        Mockito.when(userService.getByEmail(mockTransaction.getSenderWallet().getOwner().getEmail())).thenReturn(mockTransaction.getSenderWallet().getOwner());
        Mockito.when(userService.getByEmail(mockTransaction.getRecipientWallet().getOwner().getEmail())).thenReturn(mockTransaction.getRecipientWallet().getOwner());
        Mockito.when(transactionRepository.save(mockTransaction)).thenReturn(mockTransaction);

        transactionService.create(mockTransaction, initiator);

        Mockito.verify(transactionRepository, Mockito.times(1)).save(Mockito.any(Transaction.class));
    }

    //todo: do update and delete method tests -> not done as i believe they will not be needed
}
