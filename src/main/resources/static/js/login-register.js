'use strict'

//This code is for changing the active form (login or register)

$('.message a').click(function(e) {
    e.preventDefault()
    $('form').animate({height: "toggle", opacity: "toggle"}, "slow");

    changeActiveForm();
});


function changeActiveForm() {

    const loginForm = document.querySelector(".login-form")
    const registerForm = document.querySelector(".register-form")

    const loginIsActive = function () {
        return loginForm.classList.contains("active-form")
    }

    if (loginIsActive()) {
        loginForm.classList.remove("active-form");
        registerForm.classList.add("active-form");
    } else {
        registerForm.classList.remove("active-form")
        loginForm.classList.add("active-form")
    }
    console.log(registerForm.classList)
    console.log(loginForm.classList)
}



// -------------------------------------------------------------------------------------------------------------------------




const form = document.querySelector(".form")
const activeForm = document.querySelector(".active-form")
const buttonChild = activeForm.querySelector("button")

const loginForm = document.querySelector(".login-form")
const registerForm = document.querySelector(".register-form")
const loginButton = loginForm.querySelector("button")
const registerButton = registerForm.querySelector("button")

const firstNameSelector = document.querySelector("#firstName")
const lastNameSelector = document.querySelector("#lastname")
const usernameRegisterSelector = document.querySelector("#username")
const passwordRegisterSelector = document.querySelector(".password-register")
const passwordConfirmRegisterSelector = document.querySelector(".password-confirm-register")
const emailSelector = document.querySelector(".emailAddress")

const usernameSelector = document.querySelector(".username-login")
const passwordSelector = document.querySelector(".password-login")

const modal = document.querySelector('#open-modal')
const closeModal = document.querySelector('.modal-close')

let inputInvalidEntries = []

registerButton.addEventListener("click", evt => {

    inputInvalidEntries.length = 0;

    checkForEmptyInput(firstNameSelector, firstNameSelector.value, 'first name')
    checkForEmptyInput(lastNameSelector, lastNameSelector.value, 'last name')
    checkForEmptyInput(usernameRegisterSelector, usernameRegisterSelector.value , 'username')
    checkPassword(passwordRegisterSelector.value)
    checkPasswordConfirm(passwordConfirmRegisterSelector.value, passwordRegisterSelector.value);
    checkEmail(emailSelector.value, 'register')

    if (!inputInvalidEntries.includes(true)) {

        const registerUserModel = {
            firstName: firstNameSelector.value,
            username: usernameRegisterSelector.value,
            lastName: lastNameSelector.value,
            password: passwordRegisterSelector.value,
            passwordConfirm: passwordConfirmRegisterSelector.value,
            email: emailSelector.value
        }

        register(registerUserModel)
    }

    function checkPassword(password) {

        const regex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/

        if (checkForEmptyInput(passwordRegisterSelector, passwordRegisterSelector.value, 'password')) {
            return
        }

        if (!regex.test(password)){
            setErrorFor(passwordRegisterSelector, "password must contain one upper and lower case letter, a number and a special character")
        } else {
            setSuccessFor(passwordRegisterSelector)
        }
    }

    function checkPasswordConfirm(passwordConfirm, password){

        if (checkForEmptyInput(passwordConfirmRegisterSelector, passwordConfirmRegisterSelector.value, 'password confirm')) {
            return
        }

        if (passwordConfirm !== password) {
            setErrorFor(passwordConfirmRegisterSelector, 'passwords must be identical')
        } else {
            setSuccessFor(passwordConfirmRegisterSelector)
        }
    }
})

function checkForEmptyInput(selector, value, fieldName) {
    if (value === '') {
        setErrorFor(selector,`${fieldName} is empty`)
        return true;
    } else {
        setSuccessFor(selector)
    }
}

function checkEmail(email, formType) {

    let selector;

    if (formType === 'login') {
        selector = usernameSelector
    } else {
        selector = emailSelector
    }


    if (selector.value !== '') {
        verifyUsernameExists(email, formType)
    } else {
        setErrorFor(selector, 'email is empty')
    }
}


loginButton.addEventListener("click", evt => {

    inputInvalidEntries.length = 0

    const username = usernameSelector.value
    const password = passwordSelector.value

    checkEmail(username, 'login')

    if (!inputInvalidEntries.includes(true)) {
        verifyLoginInfo(username,password)
    }
})

function showModal() {

    var buttonId = $('.submit-button').attr('id');
    $('#modal-container').removeAttr('class').addClass(buttonId);
    $('body').addClass('modal-active');
    document.cookie = 'SameSite'
}

$('#modal-container').click(function(){
    $(this).addClass('out');
    $('body').removeClass('modal-active');
    changeActiveFormView();
});

function verifyUsernameExists(username, formType) {

    $.ajax({
        type: 'GET',
        url: '/api/users/search',
        dataType: 'json',
        contentType: 'application/json',
        data: {keyword: username},


        success: function (data){

            if (formType === (`login`)) {
                setSuccessFor(usernameSelector)
            } else {
                setErrorFor(emailSelector,`email is already in use`)
            }

        },
        error: function () {

            if (formType === 'login'){
                setErrorFor(usernameSelector, `${username} does not exist`)
            } else {
                setSuccessFor(emailSelector)
            }
        }
    })
}

function verifyLoginInfo(username, password) {

    $.ajax({
        type:'GET',
        url: '/api/users/login',
        dataType: "json",
        data: {keyword: username, password: password},

        success: function() {
            checkForEmptyInput(passwordSelector, password, 'password')
            if (inputInvalidEntries.includes(true)){
                return
            }
            login(username, password)
        },

        error: function(error) {
            if (error.responseText !== "success") {
                if (error.status === 409) {
                    setErrorFor(passwordSelector, "please check your email and verify your account")
                } else {
                    setErrorFor(passwordSelector,'username or password incorrect')
                }
                return
            }
            checkForEmptyInput(passwordSelector, password, 'password')
            if (inputInvalidEntries.includes(true)){
                return
            }
            login(username, password)
        }
    })
}

function changeActiveFormView() {
    $('form').animate({height: "toggle", opacity: "toggle"}, "slow");
}

function register(RegisterUserModel) {

    $.ajax({
        type: 'POST',
        url: '/api/users/register',
        dataType: 'json',
        contentType: 'application/json',
        data: JSON.stringify(RegisterUserModel),


        error: function () {
            changeActiveFormView();
            console.log("error function activated in register")
            // even when it registers successfully a user, it triggers the error
        },

        success: function (data){
            changeActiveFormView();
            showEmailSentPage(RegisterUserModel.email)
        }
    })
}

function showEmailSentPage(email) {
    $.ajax({
        type: "GET",
        url: "/auth/register/verify",
        dataType: "html",
        contentType: "application/json",
        data: {email: email},

        success: function (response) {
            $('html').html(response);
        },

        error: function(response) {
            console.log(response)
        }
    })
}

function login(username, password) {

    $.ajax({
        type: 'POST',
        dataType: 'json',
        url: '/auth/login',
        data: {
            username: username,
            password: password
        },

        success: function(response) {
        },

        error: function(error) {
            window.location = '/'
        }
    })
}

function setErrorFor(input, message) {
    const formControl = input.parentElement;
    const small = formControl.querySelector('small')
    formControl.className = "form-control error"
    small.innerText = message // maybe with .innerText works
    inputInvalidEntries.push(true)
}

function setSuccessFor(input) {
    const formControl = input.parentElement
    const small = formControl.querySelector('small')
    formControl.className = 'form-control success'
    small.innerText = ''
    inputInvalidEntries.push(false)
}
