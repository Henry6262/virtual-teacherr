

new Vue({
    el: "#app",
    data() {
        return {
            currentCardBackground: Math.floor(Math.random()* 25 + 1), // just for fun :D
            cardName: "",
            cardNumber: "",
            cardMonth: "",
            cardYear: "",
            cardCvv: "",
            minCardYear: new Date().getFullYear(),
            amexCardMask: "#### ###### #####",
            otherCardMask: "#### #### #### ####",
            cardNumberTemp: "",
            isCardFlipped: false,
            focusElementStyle: null,
            isInputFocused: false
        };
    },
    mounted() {
        this.cardNumberTemp = this.otherCardMask;
        document.getElementById("cardNumber").focus();
    },
    computed: {
        getCardType () {
            let number = this.cardNumber;
            let re = new RegExp("^4");
            if (number.match(re) != null) return "visa";

            re = new RegExp("^(34|37)");
            if (number.match(re) != null) return "amex";

            re = new RegExp("^5[1-5]");
            if (number.match(re) != null) return "mastercard";

            re = new RegExp("^6011");
            if (number.match(re) != null) return "discover";

            re = new RegExp('^9792')
            if (number.match(re) != null) return 'troy'

            return "visa"; // default type
        },
        generateCardNumberMask () {
            return this.getCardType === "amex" ? this.amexCardMask : this.otherCardMask;
        },
        minCardMonth () {
            if (this.cardYear === this.minCardYear) return new Date().getMonth() + 1;
            return 1;
        }
    },
    watch: {
        cardYear () {
            if (this.cardMonth < this.minCardMonth) {
                this.cardMonth = "";
            }
        }
    },
    methods: {
        flipCard (status) {
            this.isCardFlipped = status;
        },
        focusInput (e) {
            this.isInputFocused = true;
            let targetRef = e.target.dataset.ref;
            let target = this.$refs[targetRef];
            this.focusElementStyle = {
                width: `${target.offsetWidth}px`,
                height: `${target.offsetHeight}px`,
                transform: `translateX(${target.offsetLeft}px) translateY(${target.offsetTop}px)`
            }
        },
        blurInput() {
            let vm = this;
            setTimeout(() => {
                if (!vm.isInputFocused) {
                    vm.focusElementStyle = null;
                }
            }, 300);
            vm.isInputFocused = false;
        }
    }
});




// ---------------------------------------------------------------------------------------------------

// - Check for invalid inputs in credit card form

const $cardNumber = document.querySelector('#cardNumber')
const $cardExpirationMonth = document.querySelector('#cardMonth');
const $cardExpirationYear = document.querySelector('#cardYear')
const $cardHolder = document.querySelector('#cardName')
const $cardCVV = document.querySelector('#cardCvv');

const $submitButton = $('.card-form__button')

const $errorMessage = document.querySelector('.invalid-input-error')

const EMPTY_INPUT_MSG = 'Please fill all required fields'

$submitButton.on('click',() => checkCardInformation())

let hasInvalidInput;
function checkCardInformation() {

    checkInputIsNotEmpty($cardNumber);
    checkInputIsNotEmpty($cardHolder)
    checkInputIsNotEmpty($cardExpirationMonth)
    checkInputIsNotEmpty($cardExpirationYear)
    checkInputIsNotEmpty($cardCVV)

    if (hasInvalidInput) {
        return
    }

    check

}

function validateCardNumber(cardNumber) {
    
    if (cardNumber.length === 16) {
        return
    }
    
    showErrorMessage('Card Number must have exactly 16 digits')
}

function makeDeposit(amount) {
    $.ajax({
        type: 'POST',
        url: '/api/wallets/deposit',
        dataType: 'application/json',
        data: {amount: amount},

        success: function(response) {

            if (response === 'PENDING') {
                window.location.href = '/auth/transaction/verify'
            }
            else if (response === "COMPLETED") {
                window.location.href = '/wallets/my-wallet'
            }

        },

        error: function(error) {

        }

    })
}


function checkInputIsNotEmpty($inputSelector) {
    if ($inputSelector.value === '') {
        
        addErrorClassToInput($inputSelector)
        hasInvalidInput = true
        
        if ($errorMessage.classList.contains('visible')) {
            return
        }
        showErrorMessage(EMPTY_INPUT_MSG)
    }
}

function showErrorMessage(message) {
    if ( !showErrorMessage.classList.contains('visible')) {
        $errorMessage.classList.add('visible')
    }
    $errorMessage.innerHTML = message;
}

function checkCardIsNotExpired() {
    const cardDate = new Date()
    cardDate.setFullYear($cardExpirationYear, $cardExpirationMonth);
    const currentDate = Date.now()

    if (currentDate > cardDate) {
        addErrorClassToInput()
    }
}

function addErrorClassToInput($inputSelector) {
    $inputSelector.classList.add('input-error')
}