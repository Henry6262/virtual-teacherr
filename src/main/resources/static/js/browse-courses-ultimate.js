'use strict'

//this is for the Top main 3 courses, in the beggining of the page.

var swiper = new Swiper('.blog-slider', {
    spaceBetween: 30,
    effect: 'fade',
    loop: true,
    mousewheel: {
        invert: false,
    },
    // autoHeight: true,
    pagination: {
        el: '.blog-slider__pagination',
        clickable: true,
    }
});


//------------------------------------------------------------------------------------------------------------------

//FIXME -> This is the old implementation. ADAPT TO WALLET AND PURCHASES

function enrollToCourse(e) {

    const courseTitle = e.parentElement.querySelector(".modal-title").innerHTML
    const courseId = e.parentElement.querySelector('#courseId').innerHTML;

    $.ajax({
        type: "GET",
        url: `/api/courses/${courseId}/purchase`,


        success: function (data) {
            alert(`you have enrolled to course: ${courseTitle}`)
            closeModal()
        },

        error: function (data) {
            alert(`You are already enrolled to the course ${courseTitle}`)
        }

    })
}

// -------------------------------------------------------------------------------------------------------------------


//this code is for the searchbar at the top of the page.

const $body = $('.body');
const $btnMenu = $('.menu-toggle');
$btnMenu.click(function() {
    $body.toggleClass('menu-open');
});

const btnCloseBar = document.querySelector('.js-close-bar');
const btnOpenBar = document.querySelector('.js-open-bar');
const searchBar = document.querySelector('.searchbar');

btnOpenBar.addEventListener('click', () => {
    searchBar.classList.add('bar--is-visible');
});

btnCloseBar.addEventListener('click', () => {
    searchBar.classList.remove('bar--is-visible');
});


//--------------------------------------------------------------------------------------------------------------------
//this is code for the modal that shows course information when a course is clicked


 let $modal = $('.modal-frame');
let $overlay = $('.modal-overlay');

/* Need this to clear out the keyframe classes so they dont clash with each other between ener/leave. Cheers. */
$modal.bind('webkitAnimationEnd oanimationend msAnimationEnd animationend', function(e){
    if($modal.hasClass('state-leave')) {
        $modal.removeClass('state-leave');
    }
});

$('.close').on('click', function() {
    hideModal()
});

$('.course-modal-link').on('click', function(){
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


const modalSelector = document.querySelector('.modal-frame')
const modalContentSelector = document.querySelector('.modal-content')
const bodySelector = document.querySelector('.body')
const modalTitleSelector = document.querySelector('.modal-title')
const modalCourseIdSelector = document.querySelector("#courseId");
const modalDescriptionSelector = document.querySelector('.modal-description')
const modalImage = document.querySelector('.modal-image')
const courseStartingDateSelector = document.querySelector('.modal-date')
const modalReview = document.querySelector('.modal-review')
const modalAvg = document.querySelector('.modal-avg-rating')
const modalLevel = document.querySelector(".modal-level")
const modalSkill1 = document.querySelector('.one')
const modalSkill2 = document.querySelector('.two')
const modalSkill3 = document.querySelector('.three')



//todo make click on background of modal, exit modal,
// also make modal show information of clicked item

modalSelector.addEventListener('click', function (e) {

    if (modalSelector.classList.contains('open')) {
        if (e.target === modalSelector) {
            hideModal()
        }
    }

})

//open course modal
function openModal(e) {

    let courseId;
    let courseTitle;
    let courseDescription;
    let courseImage;
    let courseStartingDate
    let courseAverageRating
    let courseDifficulty;
    let courseSkill1;
    let courseSkill2;
    let courseSkill3;


    if (e.classList.contains("blog-slider__button")) { //means the button of the slider was clicked

        const elementContainer = e.offsetParent

        courseId = elementContainer.querySelector('.blog-slider__id')
        courseTitle = elementContainer.querySelector('.blog-slider__title')
        courseImage = elementContainer.querySelector('#slider-img')
        courseDescription = elementContainer.querySelector(".blog-slider__text")
        courseDifficulty = elementContainer.querySelector(".blog-slider__code")
        courseAverageRating = elementContainer.querySelector(".average-rating")
        courseStartingDate = elementContainer.querySelector(".starting-date")
        courseSkill1 = elementContainer.querySelector(".skill1")
        courseSkill2 = elementContainer.querySelector('.skill2')
        courseSkill3 = elementContainer.querySelector('.skill3')

    } else {

        courseId = e.querySelector('.id-course');
        courseTitle = e.querySelector('.name-course')
        courseDescription = e.querySelector('.description-course')
        courseImage = e.querySelector('.course-image')
        courseStartingDate = e.querySelector('.starting-date-course')
        // courseAverageRating = e.querySelector('.review')
        courseAverageRating = e.querySelector('.avg-rating')
        courseSkill1 = e.querySelector('.skill-1')
        courseSkill2 = e.querySelector('.skill-2')
        courseSkill3 = e.querySelector('.skill-3')
        courseDifficulty = e.querySelector(".difficulty-course")
    }

    modalSelector.classList.add('open')
    //set course values to modal
    modalLevel.innerHTML = courseDifficulty.innerHTML
    modalSkill1.innerHTML = courseSkill1.innerHTML
    modalSkill2.innerHTML = courseSkill2.innerHTML
    modalSkill3.innerHTML = courseSkill3.innerHTML
    modalAvg.innerHTML = courseAverageRating.innerHTML
    courseStartingDateSelector.innerHTML = courseStartingDate.innerHTML;
    modalTitleSelector.innerHTML = courseTitle.innerHTML
    modalCourseIdSelector.innerHTML = courseId.innerHTML;
    modalDescriptionSelector.innerHTML = courseDescription.innerHTML
    modalImage.src = courseImage.src
    console.log('hello')
}


//--------------------------------------------------------------------------------------------------------------------

//this part is for the rating/ rating-stars of each course


(function ($) {
    $(function () {
        $('.review').each(function(){
            getRate($(this));
        });

    });
})(jQuery);

function getRate(s){
    var rate = Math.round(s.attr('rate'));

    var good = 0 + rate;
    var bad = 5 - good;

    for (let i = 0; i < 5; i++) {
        if(rate >= good && good){
            s.append('<i class="fas fa-star"></i>');
            good--;
        } else{
            s.append('<i class="far fa-star"></i>');
        }
    }
}

//--------------------------------------------------------------------------------------------------------------------

//!! FOR FIRST COURSE SLIDER ROW !! this code is for the logic of the arrows that show NEXT/PREVIOUS courses, and how they are displayed
// CHANGES TO NEXT/PREVIOUS 5 COURSES

<!-- First Arrow Slider -->
const courseElements = document.querySelectorAll('.course-element.default')
const courseBrowse = document.querySelector('#browse-courses')
const arrowWrapper = document.querySelector('.arrow-wrapper')
const defaultRightArrow = document.querySelector('.regular.right')
const defaultLeftArrow = document.querySelector('.regular.left')

reset()

function reset() {
    courseElements.forEach((element, index) => initializeCourses(element, index))
}

function initializeCourses(element, index) {

    if (index > 3){
        hideElement(element, 312);
    } else {
        showElement(element, index)
    }
}

function hideElement(element, position) {
    element.style.opacity = "0"
    element.style.zIndex = -1
    element.style.transform = `TranslateX(${ position }%)`
}

function showElement(element, index) {
    element.style.opacity = '1'
    element.style.zIndex = '10'
    element.style.transform = `TranslateX(${ (100 * index) + (4 * index) }%)`
}


defaultRightArrow.addEventListener('click', function (e) {
    e.preventDefault()
    loadNextCourses()
})

defaultLeftArrow.addEventListener('click', function (e) {
    e.preventDefault()
    loadPreviousCourses()
})



let firstElementIndex = 0
let lastElementIndex = 3;
let currentRowElement = 0 // 0 to 4
const elementsToSum = 4

const loadedLastElementIndex = courseElements.length -1
let loadedFirstElementIndex = 0;

const a = function () {
    courseElements.forEach((element, index) => {

        if (index > lastElementIndex) {
            hideElement(element, 312)
        }

        else if (index < firstElementIndex) {
            hideElement(element, 0)
        }

        if (index >= firstElementIndex && index <= lastElementIndex) {
            showElement(element, currentRowElement)
            currentRowElement++
        }
    })
}

function loadPreviousCourses() {

    let lastRowWasLoaded = false

    if (firstElementIndex - elementsToSum < 0) {

        firstElementIndex = 16
        lastElementIndex = 19
        currentRowElement = 0
        a()
    }
    else {
        firstElementIndex -= 4
        lastElementIndex -= 4
        currentRowElement = 0;
        a()
    }
}


function loadNextCourses() {
    //check if we can show a new row with atleast 1 course
    if (firstElementIndex + elementsToSum > courseElements.length -1){

        reset()
        firstElementIndex = 0
        lastElementIndex = 3
        return
    }

    firstElementIndex += 4;
    lastElementIndex += 4;
    currentRowElement = 0

    courseElements.forEach((element, index) =>  {

        if (index < firstElementIndex - 4) {
            return;
        }

        if (index >= firstElementIndex - elementsToSum && index < firstElementIndex) {
            hideElement(element,0)
        }
        else if (index >= firstElementIndex && index <= lastElementIndex) {
            showElement(element,currentRowElement)
            currentRowElement++
        }
    })
}


//-----------------------------------------------------------------------------------------------------------------
//!! FOR SECOND COURSE SLIDER ROW !! this code is for the logic of the arrows that show NEXT/PREVIOUS courses, and how they are displayed
//ONE BY ONE


const javaArrowRight = document.querySelector('.java-arrow.right')
const javaArrowLeft = document.querySelector('.java-arrow.left')
const javaCourseElements = document.querySelectorAll('.java-course')

resetJavaCourses()

function resetJavaCourses() {
    javaCourseElements.forEach(
        (element, index) => initializeJavaCourses(element, index))
}

function initializeJavaCourses(element, index) {

    if (index <= 3) {
        showElement(element, index)
    }
    else {
        hideElement(element, 440)
    }

}

javaArrowRight.addEventListener('click', function (e) {
    e.preventDefault()
    changeVisibleJavaCourses("right")
})

javaArrowLeft.addEventListener('click', function (e) {
    e.preventDefault()
    changeVisibleJavaCourses("left")
})

let firstVisibleElementIndex = 0
let lastVisibleElementIndex = 3
let indexLastJavaCourse = javaCourseElements.length -1
let currentJavaElement = 0// 1 to 4

function changeVisibleJavaCourses(arrowClicked) {

    currentJavaElement = 0

    if (arrowClicked === 'right') {
        firstVisibleElementIndex++
        lastVisibleElementIndex++

        if (lastVisibleElementIndex > indexLastJavaCourse) {
            firstVisibleElementIndex = 0;
            lastVisibleElementIndex = 3
        }
    }
    else {
        firstVisibleElementIndex--
        lastVisibleElementIndex--

        if (firstVisibleElementIndex < 0) {
            firstVisibleElementIndex = indexLastJavaCourse - 3
            lastVisibleElementIndex = indexLastJavaCourse
        }
    }


    javaCourseElements.forEach((element, index) => {

        if (index < firstVisibleElementIndex) {
            hideElement(element, 0)
            return
        }
        else if (index > lastVisibleElementIndex) {
            hideElement(element, 312)
            return;
        }

        if (index >=  firstVisibleElementIndex &&  index <= lastVisibleElementIndex) {
            showElement(element, currentJavaElement++)
        }
    })

}





