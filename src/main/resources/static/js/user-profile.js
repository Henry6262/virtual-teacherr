"use strict"

// BUTTONS RELATED
const $profileButtons = $(".profile-button")
const $editConfirmButton = document.querySelector(".edit-confirm");

const $editProfileButton = document.querySelector(".edit-profile-button")
const $checkUsernameButton = document.querySelector(".check-username-button")
const $inventoryProfileButton = document.querySelector('.inventory-profile-button')
const $editButtonsInnerText = $('.inner-button')
// BUTTONS RELATED

//PROFILE-PIC RELATED
const $profilePicWrapper = document.querySelector('#profile-pic')
const $profilePicContent = document.querySelector(".profile-picture")
const $uploadImageIcon = document.querySelector('.fa-cloud-arrow-up')
const $uploadImageIconJq = $('.fa-cloud-arrow-up')
const $pictureFormSelector = document.querySelector("#picture-form")
//PROFILE PIC RELATED

// INPUT RELATED VALUES
const $firstnameInput = document.querySelector("#firstname-input")
const $lastnameInput = document.querySelector("#lastname-input")
const $usernameInput = document.querySelector("#username-input")

const $activeFirstName = document.querySelector("#firstname")
const $activeLastName = document.querySelector("#lastname")
const $personalEmail = document.querySelector('#username')
// INPUT RELATED VALUES

const $uploadImageSelector = document.querySelector('.file-input')


const $profilePic = $("#profile-pic")
const $profileInputs = $('.input')
const $infoFields = $('.info-field')

const EDIT_ACTIVE_CLASS = 'edit-active';
const CLICKED_ACTIVE_BUTTON_CLASS = 'active'
const HOVERED_BUTTON_CLASS = 'hovered'
const UPLOAD_IMG_CLASS = "active";
const EDIT_ACTIVE_PROFILE_PIC_CLASS = "edit-active"
const CHECK_USERNAME_BUTTON_ACTIVE_CLASS = "active"

const EDIT_PROFILE_BUTTON_CLASS = 'edit-profile-button'
const INVENTORY_PROFILE_BUTTON = 'inventory-profile-button'

let userProfilePictureURL = $profilePic.src
let pictureWasChanged = false;


$editProfileButton.addEventListener('click',e => activateDeactivateEditMode(e))
$editConfirmButton.addEventListener("click",e => confirmProfileChanges(e))
$checkUsernameButton.addEventListener('click', e => checkNewUsernameExists(e))
$profilePicContent.addEventListener('click', e => {

    if ($profilePicWrapper.classList.contains(EDIT_ACTIVE_PROFILE_PIC_CLASS)) {
        $uploadImageSelector.click()
    }
})


function checkNewUsernameExists() {
    if (!$checkUsernameButton.classList.contains(CHECK_USERNAME_BUTTON_ACTIVE_CLASS)) {
        return
    }


    $.ajax({
        type: 'GET',
        url: "/api/users/verify/username/" + $usernameInput.value,

        success: function(response) {
            if (response) {

            } else {

            }

            },

        error: function(response) {

        }

    })
}

function saveNewUserInformation() {
    const firstname = $firstnameInput.value
    const lastname = $lastnameInput.value
    const username = $usernameInput.value

    $.ajax({
        type: 'PUT',
        url: "/api/users/update"
    })
}

function activateDeactivateEditMode(e) {

    if (editProfileButtonIsActive()) {
          deactivateEditProfileMode(e)
          hideConfirmEditButton()
    } else {
          activateEditProfileMode(e)
          showConfirmEditButton()
    }
}

function confirmProfileChanges(e) {

    if (!$editConfirmButton.classList.contains('active')) {
        return
    }

    confirmCustomerProfileChanges()
}

let inputErrors = 0
function confirmCustomerProfileChanges() {

    checkInputIsNotEmpty($firstnameInput)
    checkInputIsNotEmpty($lastnameInput)

    if (inputErrors) {
        alert("new value cannot be blank !")
    }
}

function ajaxCallToUpdateUserController () {

    const newFirstName = $firstnameInput.value
    const newLastName = $lastnameInput.value
    $.ajax({
        type: 'PUT',
        url: "/api/users/update",
        dataType: "application/json",
        data: {firstName: newFirstName, lastName: newLastName}
    })
}

function checkInputIsNotEmpty(selector) {
    if (selector.value === '') {
        inputErrors++
    }
}


function showConfirmEditButton() {
    $editConfirmButton.classList.add('active')
}

function hideConfirmEditButton() {
    $editConfirmButton.classList.remove('active')
}

function editProfileButtonIsActive() {
    return $editProfileButton.classList.contains(CLICKED_ACTIVE_BUTTON_CLASS)
}

$editButtonsInnerText.on('click', e => activateDeactivateEditMode(e))

$profileButtons.mouseover(e => addProfileButtonHoverAnimation(e))
$profileButtons.mouseout(e => removeProfileButtonAnimation(e))
$editButtonsInnerText.mouseover(e => {addProfileButtonHoverAnimation(e)})
$profilePic.mouseover(e => addPictureHoverAnimation(e))
$profilePic.mouseout(e => removePictureHoverAnimation(e))
$uploadImageIconJq.mouseover(e => {addPictureHoverAnimation(e)})
$uploadImageIconJq.mouseout(e => removePictureHoverAnimation(e))


const inputTypes = ['username','firstname', 'lastname']

$editConfirmButton.onclick = evt => {
    if (pictureWasChanged) {
        uploadImage()
    }

    verifyInputFieldMeetsRequirements($usernameInput, inputTypes[0])
    verifyInputFieldMeetsRequirements($firstnameInput, inputTypes[1])
    verifyInputFieldMeetsRequirements($lastnameInput, inputTypes[2])

    function checkInputIsNotEmpty(inputSelector) { }
    function verifyInputFieldMeetsRequirements(inputSelector, inputType) {

        if (inputType === inputTypes[1] || inputType === inputTypes[2]) {
            verifyNameInputIsValid(inputSelector)
        }
        else if (inputType === inputTypes[0]) {
            verifyUsernameIsValid(inputSelector)
        }

    }
    function verifyUsernameIsValid(usernameSelector) {

        const newUsername = usernameSelector.value

        if (newUsername === '') {
            setInputError(usernameSelector)
            return
        }

        let hasInvalidCharacter = false;
        for (let currentChar of newUsername) {

            if (hasInvalidCharacter) {return}

            if (!isLetter(currentChar) && !isUnderscore(currentChar) && !isNumber(currentChar)) {
                setInputError(usernameSelector)
                hasInvalidCharacter = true;
            }
        }
    }
    function isUnderscore (char) {
        return char === '_'
    }
    function isNumber(char) {
        return char >= '0' && char <= '9';
    }
    function isLetter(c) {
        return c.toLowerCase() !== c.toUpperCase();
    }
    function verifyNameInputIsValid(input) {

        const nameValue = input.value
        if (nameValue !== '') {
            setInputError(input)
            return
        }

        let isInvalid = false;
         nameValue.forEach(currentChar => {

             if (isInvalid) {return}

             if (!isLetter(currentChar)) {
                 isInvalid = true;
             }
         })

        if (isInvalid) {setInputError(input)}
    }
}

function uploadImage() {
    if (uploadImageSelector.value === '') {
        alert("Please select a valid image")
        return
    }

    var formData = new FormData(pictureFormSelector)
    $.ajax({
        type: "POST",
        url: "/api/images/upload",
        data: formData,
        processData: false,
        contentType: false,

        success: function(imageUrl) {

            userProfilePictureURL = imageUrl.body;

            //TODO TEST
        },

        error: function(data) {

        }
    })
}

function setInputError(inputSelector) {
    inputSelector.classList.add('error-animation')

    setTimeout(function () {
        inputSelector.classList.remove('error-animation')
    }, 420)
}


$pictureFormSelector.onchange = e => {
    const [file] = uploadImageSelector.files
    if (file) {
        $profilePicWrapper.src = URL.createObjectURL(file)
        pictureWasChanged = true;
    }
}

function addPictureHoverAnimation(e) {
    $uploadImageIcon.classList.add("hovered")
}

function removePictureHoverAnimation(e) {
    $uploadImageIcon.classList.remove('hovered')
}


function  addProfileButtonHoverAnimation(e) {
    let buttonHovered = e.currentTarget
    if (buttonHovered.classList.contains('inner-button')) {
        buttonHovered = buttonHovered.parentElement
    }

    buttonHovered.classList.add(HOVERED_BUTTON_CLASS);
}

function removeProfileButtonAnimation(e) {
    const buttonHovered = e.target
    buttonHovered.classList.remove(HOVERED_BUTTON_CLASS);
}

function activateEditProfileMode(e) {
    e.preventDefault()
    $profilePicWrapper.classList.add(EDIT_ACTIVE_CLASS)
    $editProfileButton.classList.add(CLICKED_ACTIVE_BUTTON_CLASS)
    $uploadImageIcon.classList.add(UPLOAD_IMG_CLASS)
    enableInputEditMode();
}

function deactivateEditProfileMode(e) {
    e.preventDefault()
    $profilePicWrapper.classList.remove(EDIT_ACTIVE_CLASS)
    $editProfileButton.classList.remove(CLICKED_ACTIVE_BUTTON_CLASS)
    $uploadImageIcon.classList.remove(UPLOAD_IMG_CLASS)
    disableInputEditMode()
}

function enableInputEditMode() {
    $infoFields.each((index, element) => {
        element.classList.remove('visible')
    })
    $profileInputs.each((index, element) => {
        element.classList.add(EDIT_ACTIVE_CLASS)
    })
    $firstnameInput.value = $activeFirstName.innerHtml
    $lastnameInput.value = $activeLastName.innerHTML
    $usernameInput.value = $personalEmail.innerHTML
}

function disableInputEditMode() {
    $infoFields.each((index, element) => {
        element.classList.add('visible')
    })
    $profileInputs.each((index, element) => {
        element.classList.remove(EDIT_ACTIVE_CLASS)
    })
}



