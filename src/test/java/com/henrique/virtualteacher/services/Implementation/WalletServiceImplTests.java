package com.henrique.virtualteacher.services.Implementation;

import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.entities.Wallet;
import com.henrique.virtualteacher.exceptions.DuplicateEntityException;
import com.henrique.virtualteacher.exceptions.EntityNotFoundException;
import com.henrique.virtualteacher.exceptions.ImpossibleOperationException;
import com.henrique.virtualteacher.exceptions.UnauthorizedOperationException;
import com.henrique.virtualteacher.repositories.WalletRepository;
import com.henrique.virtualteacher.services.Helpers;
import com.henrique.virtualteacher.services.implementation.WalletServiceImpl;
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

    @InjectMocks
    WalletServiceImpl walletService;


    @Test
    public void getById_should_throwException_when_walletDoesNotExist(){
        User mockUser = Helpers.createMockUser();
        Wallet mockWallet = Helpers.createMockWallet(mockUser);

        Assertions.assertThrows(EntityNotFoundException.class, () -> walletService.getById(mockWallet.getId(), mockUser));
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

        Mockito.when(walletRepository.getByOwnerId(initiator.getId())).thenReturn(Optional.of(mockWallet));

        Assertions.assertThrows(UnauthorizedOperationException.class, () -> walletService.getLoggedUserWallet(initiator));
    }

    @Test
    public void create_shouldThrowException_when_userAlreadyHas_existingWallet() {
        User mockUser = Helpers.createMockUser();
        Wallet mockWallet = Helpers.createMockWallet(mockUser);

        Mockito.when(walletRepository.getByOwnerId(mockUser.getId())).thenReturn(Optional.of(mockWallet));

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

        Mockito.when(walletRepository.getByOwnerId(mockUser.getId())).thenReturn(Optional.of(mockWallet));

        Assertions.assertThrows(ImpossibleOperationException.class, () -> walletService.deposit(mockUser, BigDecimal.valueOf(7)));
    }

    @Test
    public void deposit_shouldAddFundsToWallet_when_AmountIsValid() {

        User mockUser = Helpers.createMockUser();
        Wallet mockWallet = Helpers.createMockWallet(mockUser);

        Mockito.when(walletRepository.getByOwnerId(mockUser.getId())).thenReturn(Optional.of(mockWallet));

        walletService.deposit(mockUser, BigDecimal.valueOf(30));

        Assertions.assertEquals(BigDecimal.valueOf(30),mockWallet.getBalance());
    }

    @Test
    public void makePurchase_shouldThrowException_when_UserDoesNot_haveEnoughFunds() {
        User mockUser = Helpers.createMockUser();
        Wallet mockWallet = Helpers.createMockWallet(mockUser);
        Course course = Helpers.createMockCourse();

        Mockito.when(walletRepository.getByOwnerId(mockUser.getId())).thenReturn(Optional.of(mockWallet));

        Assertions.assertThrows(ImpossibleOperationException.class, () -> walletService.makePurchase(course, mockUser));
    }




}
