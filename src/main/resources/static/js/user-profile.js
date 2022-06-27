"use strict"

const $profileButtons = $(".profile-button")
const $editButtonsInnerText = $('.inner-button')
const $editProfileButton = document.querySelector(".edit-profile-button")
const $inventoryProfileButton = document.querySelector('.inventory-profile-button')
const $profilePicWrapper = document.querySelector('#profile-pic')
const profilePicWrapper = document.querySelector(".profile-picture")
const $uploadImageIcon = document.querySelector('.fa-cloud-arrow-up')
const $firstnameInput = document.querySelector("#firstname-input")
const $lastnameInput = document.querySelector("#lastname-input")
const $usernameInput = document.querySelector("#username-input")
const $activeFirstName = document.querySelector("#firstname")
const $activeLastName = document.querySelector("#lastname")
const $personalEmail = document.querySelector('#username')


const $profilePic = $("#profile-pic")
const $profileInputs = $('.input')
const $infoFields = $('.info-field')

const EDIT_ACTIVE_CLASS = 'edit-active';
const CLICKED_ACTIVE_BUTTON_CLASS = 'active'
const HOVERED_BUTTON_CLASS = 'hovered'
const UPLOAD_IMG_CLASS = "active";
const EDIT_ACTIVE_PROFILE_PIC_CLASS = "edit-active"

const EDIT_PROFILE_BUTTON_CLASS = 'edit-profile-button'
const INVENTORY_PROFILE_BUTTON = 'inventory-profile-button'


$editProfileButton.addEventListener('click',e => activateDeactivateEditMode(e))

function activateDeactivateEditMode(e) {
    if ($editProfileButton.classList.contains(CLICKED_ACTIVE_BUTTON_CLASS)) {
        deactivateEditProfileMode(e)
    } else {
        activateEditProfileMode(e)
    }
}

$editButtonsInnerText.on('click', e => activateDeactivateEditMode(e))

$profileButtons.mouseover(e => addProfileButtonHoverAnimation(e))
$profileButtons.mouseout(e => removeProfileButtonAnimation(e))
$editButtonsInnerText.mouseover(e => {
    addProfileButtonHoverAnimation(e)
})

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



