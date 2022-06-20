$modal = $('.modal-frame');
$overlay = $('.modal-overlay');

// THIS IS JS FOR THE MODAL

/* Need this to clear out the keyframe classes so they dont clash with each other between ener/leave. Cheers. */
$modal.bind('webkitAnimationEnd oanimationend msAnimationEnd animationend', function(e){
    if($modal.hasClass('state-leave')) {
        $modal.removeClass('state-leave');
    }
});

$('.close').on('click', function() {
    hideModal()
});

$('.next-button').on('click', function(){
    closeModal()
});

function hideModal() {
    $overlay.removeClass('state-show');
    $modal.removeClass('state-appear').addClass('state-leave');
}

function closeModal() {
    $overlay.addClass('state-show');
    $modal.removeClass('state-leave').addClass('state-appear');
}

document.cookie = 'SameSite'



//----------------------------------------------------------------------------------------------------------




    const inputButtonSelector = document.querySelector('#input-button')
    const pictureFormSelector = document.querySelector("#picture-form")
    const uploadImageSelector = document.querySelector('.file-input')
    const imageSelector = document.querySelector('.image-content')
    const courseSubmitButtonSelector = document.querySelector('.course-form-submit-button')
    const imageCropperSelector = document.querySelector(".image-cropper")
    let createSelector = document.querySelector("#create-course-button");

    const titleSelector = document.querySelector("#title-input")
    const topicSelector = document.querySelector("#topic-input")
    const descriptionSelector = document.querySelector("#description-input")
    const difficultySelector = document.querySelector("#difficulty-input")
    const startingDateSelector = document.querySelector("#starting-date-input")
    const pictureSelector = document.querySelector("#picture-input")
    const blahSelector = document.querySelector("#blah")
    const iconUploadSelector = document.querySelector(".fa-cloud-arrow-up")
    const iconDivSelector = document.querySelector('.icon-div')

    const skill1 = document.querySelector('.skill-input-1')
    const skill2 = document.querySelector('.skill-input-2')
    const skill3 = document.querySelector('.skill-input-3')

    const formPicture = document.querySelector('#picture-form')

    let pictureWasChanged = false;
    let uploadedPictureUrl = document.getElementById('blah').src;
    let formHasErrors = [];

    function checkFieldIsNotEmpty(selector, value) {
    if (value === '' || value === ' ') {
    setErrorFor(selector, "title")
    formHasErrors.push(true)
} else {
    //todo : maybe add checkmark next to inout field
}
}

    function setErrorFor(selector, errorMessage ){
    //todo
}

    $('#work').on('click', function (e) {

    e.preventDefault()
    formHasErrors.length = 0

    if (pictureWasChanged){
    uploadImage()
}

    checkFieldIsNotEmpty(titleSelector, titleSelector.value)
    checkFieldIsNotEmpty(startingDateSelector, startingDateSelector.value)

    if (formHasErrors.length > 0) {
    alert('Please fill all required fields')
    return
}

    createCourse()
})

    function createCourse() {
    const courseModel = {
    title: titleSelector.value,
    topic: topicSelector.value,
    description: descriptionSelector.value,
    difficulty: difficultySelector.value,
    startingDate: startingDateSelector.value,
    picture: uploadedPictureUrl,

    skill1: skill1.value,
    skill2: skill2.value,
    skill3: skill3.value
}

    $.ajax({
    type: "POST",
    url: "/api/courses",
    dataType: "json",
    contentType: 'application/json',
    data: JSON.stringify(courseModel),

    success: function(data) {
    alert('course created')
},

    error: function (data) {

}

})
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

    uploadedPictureUrl = imageUrl.body;
},

    error: function(data) {
    alert("login-dumbass")
}

})
}

    pictureFormSelector.onchange = evt => {
    const [file] = uploadImageSelector.files
    if (file) {
    blahSelector.src = URL.createObjectURL(file)
    pictureWasChanged = true;
}
}

    blahSelector.addEventListener('mouseover', function(e) {
    iconUploadSelector.classList.add('show')
})

    blahSelector.addEventListener('mouseout', function (e) {
    iconUploadSelector.classList.remove('show')
})

    blahSelector.onclick = evt => {
    uploadImageSelector.click()
}


    $.ajax({
    type: "GET",
    url: "/api/courses/topics",
    dataType: "json",

    success: function (response) {
    let topicsSelector = $(".topic-dropdown")

    $.each(response, function (index, value) {
    topicsSelector.append(
    "<option>" + value + "</option>"
    )
    })
},

    error: function (response) {
    prompt("error m8")
}
})
