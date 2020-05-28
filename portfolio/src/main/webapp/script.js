// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

class SlideShow {
    constructor( /**@type{HTMLElement}*/ prevButton,
        /**@type{HTMLElement}*/ nextButton, 
        /**@type{HTMLElement}*/ numText) {
        this.prevButton = prevButton;
        this.nextButton = nextButton;
        this.numText = numText;
        this.slideIndex = 0;
        this.prevButton.addEventListener('click', () => this.handlePrev());
        this.nextButton.addEventListener('click', () => this.handleNext());
    }

    handlePrev() {
        this.slideIndex--;
        this.showSlides(this.slideIndex);
        this.handleText(this.slideIndex + 1);
    }

    handleNext() {
        this.slideIndex++;
        this.showSlides(this.slideIndex);
        this.handleText(this.slideIndex + 1);
    }

    handleText(slideNum) {
        this.numText.textContent = `${slideNum} / 4`;
    }

    /**
    * Shows the slide at the index of the parameter.
    */
    showSlides(slideNum) {
        let i;
        const slides = document.querySelectorAll('.slides');
        if (slideNum >= slides.length) {
            slideNum = 0;
            this.slideIndex = 0;
        }
        if (slideNum < 0) {
            slideNum = slides.length - 1;
            this.slideIndex = slides.length - 1;
        }
        for (i = 0; i < slides.length; i++) {
            slides[i].style.display = "none";
        }
        slides[slideNum].style.display = "block";
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const container = document.getElementById('photos');
    const div = document.createElement('div');
    div.setAttribute("id", "numtext");
    div.classList.add('numbertext');
    div.textContent = "1 / 4";
    container.appendChild(div);
    
    const slideshow = new SlideShow(document.getElementById('prev'),
        document.getElementById('next'), document.getElementById('numtext'));
});


/**
 * Expands a collapsible when clicked.
 */
function expandCollapsible(expectedDivId) {
    const content = document.querySelector(`#${expectedDivId} .content`);
    if (content.style.maxHeight){
      content.style.maxHeight = null;
    } else {
      content.style.maxHeight = `${content.scrollHeight}px`;
    }
}
