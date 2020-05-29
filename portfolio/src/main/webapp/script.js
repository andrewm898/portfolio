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
    constructor( /**@type{HTMLElement}*/ photosElement) {
        this.prevButton = document.createElement('a');
        this.nextButton = document.createElement('a');
        this.numtext = document.createElement('div');
        this.slideIndex = 0;
        this.prevButton.addEventListener('click', () => this.handlePrev());
        this.nextButton.addEventListener('click', () => this.handleNext());
        
        /* Creating next button */
        this.nextButton.textContent = String.fromCharCode(0x027E9);
        this.nextButton.classList.add('next-button');
        photosElement.appendChild(this.nextButton);
        
        /* Creating prev button */
        this.prevButton.textContent = String.fromCharCode(0x027E8);
        this.prevButton.classList.add('prev-button');
        photosElement.appendChild(this.prevButton);
        
        /* Creating image array */
        this.slideElements = [];
        for (const source of ["/images/porchlights.JPG", "/images/pokebowl.JPG",
                              "/images/doghat.JPG", "/images/androidpillow.JPG"]) {
            const image = document.createElement('img');
            image.classList.add('slides');
            image.src = source;
            this.slideElements.push(image);
            photosElement.appendChild(image);
        }
        this.slideCount = this.slideElements.length;
        this.slideElements[0].style.display = "block";

        /* Creating slide number label */
        this.numtext.classList.add('numbertext');
        this.numtext.textContent = `1 / ${this.slideCount}`;
        photosElement.appendChild(this.numtext);
    }
    handlePrev() {
        this.slideElements[this.slideIndex].style.display = 'none';
        this.slideIndex--;
        this.showSlides(this.slideIndex);
    }
    handleNext() {
        this.slideElements[this.slideIndex].style.display = 'none';
        this.slideIndex++;
        this.showSlides(this.slideIndex);
    }
    /**
    * Shows the slide at the index of the parameter.
    */
    showSlides(slideNum) {
        if (slideNum >= this.slideCount) {
            slideNum = 0;
            this.slideIndex = 0;
        }
        if (slideNum < 0) {
            slideNum = this.slideCount - 1;
            this.slideIndex = this.slideCount - 1;
        }
        this.slideElements[slideNum].style.display = "block";
        this.numtext.textContent = `${slideNum + 1} / ${this.slideCount}`;
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const container = document.getElementById('photos');
    const slideshow = new SlideShow(document.getElementById('photos'));
});

/**
 * Expands a collapsible when clicked.
 */
function expandCollapsible(expectedDivId) {
    const content = document.querySelector(`#${expectedDivId} .content`);
    if (content.style.maxHeight) {
        content.style.maxHeight = null;
    } else {
        content.style.maxHeight = `${content.scrollHeight}px`;
    }
}
