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

/**
*   SlideShow is a collection of images that can be clicked through by the user.
*/
class SlideShow {
    constructor( /**@type{HTMLElement}*/ photosElement) {
        this.prevButton = document.createElement('a');
        this.nextButton = document.createElement('a');
        this.numtext = document.createElement('div');
        this.slideIndex = 0;
        this.prevButton.addEventListener('click', () => this.handlePrev());
        this.nextButton.addEventListener('click', () => this.handleNext());
        
        /* Creating next button */
        this.nextButton.textContent = "⟩"
        this.nextButton.classList.add('next-button');
        photosElement.appendChild(this.nextButton);
        
        /* Creating prev button */
        this.prevButton.textContent = "⟨";
        this.prevButton.classList.add('prev-button');
        photosElement.appendChild(this.prevButton);
        
        /* Creating image array */
        this.slideElements = [];
        for (const source of ["/images/porchlights.JPG",
                              "/images/pokebowl.JPG",
                              "/images/doghat.JPG",
                              "/images/androidpillow.JPG"]) {
            const image = document.createElement('img');
            image.classList.add('slides');
            image.src = source;
            this.slideElements.push(image);
            photosElement.appendChild(image);
        }
        this.slideElements[0].classList.add("active-slide");

        /* Creating slide number label */
        this.numtext.classList.add('numbertext');
        this.numtext.textContent = `1 / ${this.slideElements.length}`;
        photosElement.appendChild(this.numtext);
    }
    handlePrev() {
        this.makeActiveIndex(this.slideIndex - 1);
    }
    handleNext() {
        this.makeActiveIndex(this.slideIndex + 1);
    }
    /**
    * Makes correct index the active one, calls function to display it.
    */
    makeActiveIndex(index) {
        this.slideElements[this.slideIndex].classList.remove("active-slide");
        this.slideIndex = index;
        if (this.slideIndex >= this.slideElements.length) {
            this.slideIndex = 0;
        }
        else if (this.slideIndex < 0) {
            this.slideIndex = this.slideElements.length - 1;
        }
        this.slideElements[this.slideIndex].classList.add("active-slide");
        this.numtext.textContent = `${this.slideIndex + 1} / ${this.slideElements.length}`;
    }
}

document.addEventListener('DOMContentLoaded', () => {
    updateQuotesFromServer();
    const container = document.getElementById('photos');
    const slideshow = new SlideShow(document.getElementById('photos'));
});

/**
 * Fetches messages from the server to update the page with.
 */
async function updateQuotesFromServer() {
  const response = await fetch('/data');
  const messages = await response.json();

  messageList = document.getElementById('server-messages');

  messageList.textContent = '';
  for (let i = 0; i < messages.length; i++) {
    const liElement = document.createElement('li');
    liElement.textContent = `${messages[i].username}: ${messages[i].text}`;
    messageList.appendChild(liElement);
  }
}

/**
 * Deletes all messages from database & clears from screen
 */
async function deleteMessages() {
  const dataDelete = await fetch('/delete-data');
  updateQuotesFromServer();
  console.log("here");
}

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
