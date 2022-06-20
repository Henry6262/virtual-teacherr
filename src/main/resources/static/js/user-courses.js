'use strict'

$('.charts-container').each(function (){
    const currentElement = $(this);

    const rightCircle = currentElement.find('.right-side')
    const leftCircle = currentElement.find(".left-side")
    const elementPercentage = currentElement.children(".label").context.innerText
    const percentageNumberValue = Number(elementPercentage.substring(0, elementPercentage.length - 1));
    const pieWrapperSelector =  currentElement.context.firstElementChild

    if (percentageNumberValue <= 50) {

        pieWrapperSelector.classList.add('progress-30')

        rightCircle.display = 'none';
        const percentage = percentageNumberValue * 100 / 50 //50 bcs it is percentage for the left-side circle
        const degrees = percentage * 180 / 100
        const degreeString = `rotate(${degrees}deg)`
        leftCircle.css('transform', degreeString)
    } else {

        pieWrapperSelector.classList.add('progress-60')

        rightCircle.display = "unset"
        const percentage = percentageNumberValue * 100 / 100 //100 because it includes the right-side
        const degrees = percentage * 360 / 100
        const degreeString = `rotate(${degrees}deg)`
        rightCircle.css('transform', degreeString)
        leftCircle.css('transform', 'rotate(180deg)') //180 ts max, as it is half of the circle
        //todo: if percentage is more than 50, do calculation an =d make changes
    }

})
