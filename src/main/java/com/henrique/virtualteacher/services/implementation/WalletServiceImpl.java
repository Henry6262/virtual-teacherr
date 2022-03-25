package com.henrique.virtualteacher.services.implementation;

import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.entities.Wallet;
import com.henrique.virtualteacher.exceptions.DuplicateEntityException;
import com.henrique.virtualteacher.exceptions.EntityNotFoundException;
import com.henrique.virtualteacher.exceptions.ImpossibleOperationException;
import com.henrique.virtualteacher.exceptions.UnauthorizedOperationException;
import com.henrique.virtualteacher.repositories.WalletRepository;
import com.henrique.virtualteacher.services.interfaces.UserService;
import com.henrique.virtualteacher.services.interfaces.WalletService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class WalletServiceImpl implements WalletService{

    private final UserService userService;
    private final WalletRepository walletRepository;
    private final Logger logger;


    @Override
    public Wallet getById(int walletId, User loggedUser) {

        Wallet wallet = walletRepository.getById(walletId);

        checkWalletExists(wallet, loggedUser);
        checkUserIsWalletOwner(loggedUser, wallet);

        return wallet;
    }


    @Override
    public Wallet getLoggedUserWallet(User loggedUser) {

        Wallet wallet =  walletRepository.getByUserId(loggedUser.getId());

        checkWalletExists(wallet, loggedUser);
        checkUserIsWalletOwner(loggedUser, wallet);

        return wallet;
    }

    @Override
    public void create(User userToCreateWalletFor) {

        checkUserDoesNotHaveWallet(userToCreateWalletFor);
        Wallet newWallet = new Wallet(userToCreateWalletFor);
        logger.info("Wallet for user with id: %d, has been created");
    }

    @Override
    public void delete(User walletOwner) {

        //todo: delete wallet only when user decides to close account

    }

    @Override
    public void deposit(User loggedUser, BigDecimal amount) {

        Wallet userWallet = getLoggedUserWallet(loggedUser);

        if (amount.doubleValue() < 10.00) {
            throw new ImpossibleOperationException("Minimum Deposit is 10");
        }

        userWallet.addToWallet(amount);
        logger.info(String.format("User with id %d, has successfully added 2%f", loggedUser.getId(), amount.doubleValue()));
        //todo: make deposits over 200, need to be verified via MAIL or PHONE
    }

    @Override
    public void makePurchase(Course course, User loggedUser) {

        Wallet userWallet = getLoggedUserWallet(loggedUser);

        checkUserWalletHasEnoughFunds(course, userWallet);
        userWallet.retrieveFromWallet(course.getPrice());

        logger.info(String.format("User with id: %d, has successfully purchase course with id %d",loggedUser.getId(), course.getId()));
    }


    private void checkUserWalletHasEnoughFunds(Course course, Wallet userWallet) {

        BigDecimal walletBalance = userWallet.getBalance();

        if (walletBalance.doubleValue() < course.getPrice().doubleValue()) {
            throw new ImpossibleOperationException(String.format
                    ("Wallet of user with id: %d, does not have enough money to purchase course with id: %d", userWallet.getUser().getId(), course.getId()));
        }
    }

    private void checkUserDoesNotHaveWallet(User user){

        Wallet userWallet =  getLoggedUserWallet(user);

        if (userWallet != null) {
            throw new DuplicateEntityException(String.format("User with id: %d, Already has a wallet", user.getId()));
        }
    }

    private void checkWalletExists(Wallet wallet, User loggedUser) {
        if (wallet == null) {
            throw new EntityNotFoundException(String.format("User with id: %d: does not have a wallet", loggedUser.getId()));
        }
    }

    private void checkUserIsWalletOwner(User user, Wallet wallet) {
        if (user.getId() != wallet.getUser().getId()) {
            throw new UnauthorizedOperationException(String.format("User with id: %d, is not the owner of wallet with id: %d", user.getId(), wallet.getId()));
        }
    }


    //todo : on user service, when a user is created, a wallet will also be generated
    // create deposit and withdrawal money for wallet
    // make users buy courses and then they will be enrolled,
    // make enroll to course a private method that is called after a user has finalized the purchase of a course

    //todo: create transactions -> need to figure out how to make them secure -> research good options

    //todo: make tests

}
