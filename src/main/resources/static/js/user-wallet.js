"use strict";


const loggedUserId = Number (document.querySelector("#user-id").textContent);
const pageSize = 10;
let totalTransactionPages;

getAndLoadContent(pageSize, 1).then(
    response => initializePaginationArrows())

$("#page-buttons").on('click', function (e) {
    e.preventDefault();
    let pageNumber = Number (e.target.innerHTML);
    const activePage = $(".active-page").element

    getAndLoadContent(pageSize, pageNumber)
})

function getAndLoadContent(size, page) {
    return $.ajax({
        url: `/api/transactions/my-wallet`,
        type: "GET",
        dataType: "json",
        data: {size: size, page: page},

        success: function (response) {

            totalTransactionPages = response.totalPages
            const tableContent = $(".transactions").empty();

            response.content.forEach( function (transaction) {

                let transactionType = lowercaseWord(transaction.transactionType)
                let senderName;
                let recipientName;
                let transactionDirection;
                let transactionAmount = transaction.amount;
                let transactionCreationTime = transaction.creationTime;
                let transactionStatus = lowercaseWord(transaction.status);

                if (transaction.senderWallet.owner.id === loggedUserId) {
                    senderName = 'me'
                    recipientName = transaction.recipientWallet.owner.firstName;
                    transactionDirection =  'outgoing'
                } else {
                    senderName = transaction.senderWallet.owner.firstName;
                    recipientName = "me"
                    transactionDirection = "incoming";
                }

                let statusClass;
                if (transactionStatus === "completed") {
                    statusClass = 'status-completed'
                }
                else if (transactionStatus === "pending") {
                    statusClass = 'status-pending'
                }
                else {
                    statusClass = 'status-rejected'
                }

                let toAppend = "<small" + " class=`" + transactionDirection + "'>" +
                    transactionDirection +
                    "</small>"
                let toAppendSender = "<small>" + senderName + "</small>"
                let toAppendRecipient = "<small>" + recipientName + "</small>"
                let toAppendStatus = "<small" + " class='" + statusClass + "'>" + transactionStatus +"</small>"

                tableContent.append(
                    " <tr class='row'>" +
                    ' <td class="col first-col">' +
                    '<small>' + transactionType + '</small>' +
                    '</td>' +
                    '<td class="col">' + toAppendSender +  "</td>" +
                    '<td class="col"' + toAppendRecipient +
                    '<td class="col">' +
                    toAppend +
                    '</td>' +

                    '<td class="col">' +
                    '<small>' + transactionAmount + '</small> ' +
                    '</td>' +
                    '<td class="col">' +
                    '<small class="transaction-creation-date">' + transactionCreationTime +'</small>' +
                    '</td>' +
                    '<td class="col last-col">' +
                    toAppendStatus +
                    '</td>' +
                    '</tr>'
                )
            })
        },

        error: function (response) {
            console.log("error")
        }
    })
}

function lowercaseWord(string) {
    return string.toLowerCase()
}

var pr = document.querySelector( '.paginate.left' );
var pl = document.querySelector( '.paginate.right' );

function initializePaginationArrows() {

    pr.onclick = slide.bind( this, -1 );
    pl.onclick = slide.bind( this, 1 );

    var index = 0, total = totalTransactionPages;

    function slide(offset) {
        index = Math.min( Math.max( index + offset, 0 ), total - 1 );

        document.querySelector( '.counter' ).innerHTML = ( index + 1 ) + ' / ' + total;

        getAndLoadContent(10, index + 1);

        pr.setAttribute( 'data-state', index === 0 ? 'disabled' : '' );
        pl.setAttribute( 'data-state', index === total - 1 ? 'disabled' : '' );
    }
    slide(0);
}


//-------------------------------------------------------------------------------------------------------------

//TODO -> THIS IS THE JS FOR THE USER-WALLET  {BUTTONS} And modal

const $depositButton = document.querySelector("#deposit-button");
const $modalContainer = document.querySelector("#section1-content")
const $depositInfo = document.querySelector(".modal-deposit-info")
const $sendInfo = document.querySelector(".modal-send-info")
const $transactionButtons = document.querySelector(".buttons")
let $activeModalBody = document.querySelector(".info-active");
const $completedLoadMessage = document.querySelector(".successful-transaction-req")
const $modalTitle = document.querySelector("#modal-header-title")
const $errorMsg = document.querySelector(".error-input-msg")

const $inventory = document.querySelector('.inventory')

const activeClass = 'info-active'
let operationType;

$inventory.addEventListener('click',e => {
    window.location.href = "/users/inventory"
})

$transactionButtons.addEventListener("click", e => {
    e.preventDefault();
    let buttonClicked = e.target

    if ($activeModalBody !== undefined) {
        $activeModalBody.classList.remove(activeClass)
    }

    $modalContainer.classList.add("blur")
    document.getElementsByClassName("popup")[0].classList.add("active")

    let classList = buttonClicked.classList;
    if (classList.contains('fa-solid') || classList.contains('button-text')) {
        buttonClicked = buttonClicked.parentElement
        classList = buttonClicked.classList;
    }

    if (classList.contains('deposit')) {
        $modalTitle.innerHTML = "Deposit"
        $depositInfo.classList.add(activeClass)
        operationType = "deposit";
    }
    else if (classList.contains("send")) {
        $modalTitle.innerHTML = "Send"
        $sendInfo.classList.add(activeClass)
        operationType = "send"
    }
    else if (classList.contains("inventory")) {
        //todo
    }
})

document.getElementById("dismiss-popup-btn").addEventListener("click", function (e){

    const activeInfo = document.querySelector(`.${activeClass}`)

    document.getElementsByClassName("popup")[0].classList.remove("active")
    $modalContainer.classList.remove("blur")
    hideLoadingSpinner()

    if (activeInfo !== null) {
        activeInfo.classList.remove(activeClass)
    }
});


var oldText;
const $loadingSpinner = document.querySelector(".circle-loader")
const $depositAmount = $('#deposit-amount')[0]
const $sendAmount = $('#amount-to-send')[0]
const $moneyRecipient = $('#recipient-email')[0]

const DEPOSIT_OPERATION_TYPE = 'deposit'
const SEND_OPERATION_TYPE = 'send';

const sendTransactionUrl = '/api/wallets/transfer'
const getLoggedUserWalletFundsUrl = '/api/wallets/my-wallet'
const depositTransactionUrl = "/api/wallets/deposit"
const searchForUserUrl = "/api/users/search";

let hasInvalidInputs = false;  //used to check for invalid inputs (non existing user emails and transfer sums larger than own wallet balance)

confirm = $('.confirm-button');

$(".confirm-button").click(function(e) {
    e.preventDefault()
    var b = $(this);

    if (b.hasClass("done")) {
        return;
    }

    if (b.hasClass("confirm")) {

        if (hasInvalidInputs) {
            primaryConfirmationButton(b)
            return;
        }
        if (operationType === DEPOSIT_OPERATION_TYPE) {
            createDepositTransaction($depositAmount.value)
        }
        else if (operationType === SEND_OPERATION_TYPE) {
            createSendTransaction($sendAmount.value, $moneyRecipient.value)
        }
        finalConfirmationButtonText(b);
    } else {

        hasInvalidInputs = false;

        if (operationType === DEPOSIT_OPERATION_TYPE) {
            const depositAmount = Number ($depositAmount.value)

            if (depositAmount <= 0) {
                addBlankInputErrorAnimation($depositAmount)
                return;
            }
            getCreditCardPage(depositAmount);
        }
        else if (operationType === SEND_OPERATION_TYPE) {

            checkInPutIsNotEmpty($sendAmount);
            checkInPutIsNotEmpty($moneyRecipient);

            if (hasInvalidInputs) {
                return;
            }

            const promise = checkTransferInformationIsValid();
            if (promise.isRejected) {
                primaryConfirmationButton(b);
            }
        }

        if (hasInvalidInputs) {
            return;
        }
        setSecondaryButtonText(b)
    }
})

let invalidTransferInformation = false;

function checkTransferInformationIsValid() {
    return new Promise((resolve, reject) => {

        checkRecipientExists($moneyRecipient.value)
            .then(resolve, reject)
        verifySenderHasEnoughFunds($sendAmount.value)
            .then(function() {
                hideModalErrorMsg();resolve()}
                ,reject)

    }).catch(error => {
        console.log(error)
    });
}

function displayModalErrorMsg(message) {
    if ($errorMsg.classList.contains('visible')) {
        return
    }
    $errorMsg.classList.add('visible')
    $errorMsg.innerHTML = message;
}

function hideModalErrorMsg() {
    $errorMsg.classList.remove('visible')
}

function verifySenderHasEnoughFunds(transferAmount) {
     return $.ajax({
        type: 'GET',
        url: getLoggedUserWalletFundsUrl,
        contentType: 'json',

        success: function (response) {

            const loggedUserWalletBalance = response.balance;
            const insufficientFundsMsg = `Insufficient funds to complete the transaction`
            const sufficientFundsMsg = 'user has the necessary funds to complete the transfer'


            return new Promise((resolve, reject) => {
                if (loggedUserWalletBalance < transferAmount) {
                    invalidTransferInformation = true;
                    reject(insufficientFundsMsg)
                } else {
                    resolve(sufficientFundsMsg)
                }
            }).catch(error => {
                console.log(error)
                addBlankInputErrorAnimation($sendAmount)
                displayModalErrorMsg(insufficientFundsMsg)
                hasInvalidInputs = true
            })
        },

        error: function(error) {
            console.log('wallet not found')
        }
    })
}

function createDepositTransaction(amount) {
    $.ajax({
        type: 'POST',
        url: depositTransactionUrl,
        data: {amount: amount},

        success: function (response) {
            console.log(`deposit of ${$depositAmount} was successful !`)
        },

        error: function (error) {
            console.log("failure in deposit, please try again later")
        }
    })
}

function createSendTransaction(amount, recipient) {
    $.ajax({
        url : sendTransactionUrl,
        type : 'POST',
        data: {amount: amount, email :recipient},

        success: function (response) {
            console.log(`transfer of ${amount} to ${recipient} was successful !`)
        },

        error : function (error) {
            console.log(`Error in transfer of ${amount} to ${recipient}`)
        }

    })
}

function checkInPutIsNotEmpty(selector) {
    if (selector.value === '' ) {
        addBlankInputErrorAnimation(selector)
        hasInvalidInputs = true
    }
}

function checkRecipientExists(email) {
    return $.ajax({
        type: "GET",
        url: searchForUserUrl,
        data: {keyword: email},

        success: function (userExists) {

            const userNotFoundMsg = `user with email ${email}, does not exist`
            const userFoundMsg = `user: ${email}, has been found !`

            return new Promise((resolve, reject) => {
                if (userExists) {
                    resolve(userExists)
                } else {
                    invalidTransferInformation = true;
                    reject(userNotFoundMsg)
                }

            }).catch(error => {
                console.log(error)
                addBlankInputErrorAnimation($moneyRecipient);
                displayModalErrorMsg(userNotFoundMsg)
                hasInvalidInputs = true
            })
        },

        error: function (error) {


        }

    })
}

function getCreditCardPage(depositAmount) {
    window.location.href = `/wallets/deposit?amount=${depositAmount}`
}

function setSecondaryButtonText(b) {
    oldText = b.text();
    $(b).addClass("confirm");
    $(b).text("Are you sure?")

    setTimeout(function() {
        primaryConfirmationButton(b)
    }, 4000);
}

function primaryConfirmationButton(b) {
    if (!b.hasClass("done")) {
        b.removeClass("confirm");
        b.text(oldText);
    }
}

function finalConfirmationButtonText(b) {
    b.text("Thanks!");
    b.removeClass("confirm");
    b.addClass("done");
    showLoadingSpinner(operationType);

    setTimeout(function() {
        b.removeClass("done");
        b.text(oldText);
        $completedLoadMessage.classList.add('activated')
    }, 4000);
}

function addBlankInputErrorAnimation($element) {
    $element.classList.add('error-animation');

    setTimeout(function () {
        $element.classList.remove('error-animation')
    }, 420)
}

function showLoadingSpinner(operationType) {
    $activeModalBody = $(".info-active")[0];
    if ($activeModalBody !== undefined) {
        $activeModalBody.classList.remove("info-active");
    }
    else {
        toggleCircleAndSApinner()
    }

    $loadingSpinner.classList.remove("hidden")

    setTimeout(function () {
        toggleCircleAndSApinner()
        $completedLoadMessage.innerHTML = operationType === "send" ? 'Money was sent successfully' : 'Deposit successful'
    }, 3000)

    function toggleCircleAndSApinner() {
        $('.circle-loader').toggleClass('load-complete');
        $('.checkmark').toggle();
    }
}

function hideLoadingSpinner() {
    $loadingSpinner.classList.add("hidden")
    $('.circle-loader').toggleClass('load-complete')
    $('.checkmark').toggle();

    $completedLoadMessage.classList.remove('activated')
}
