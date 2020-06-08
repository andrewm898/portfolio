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

/**
*   SlideShow is a collection of images that can be clicked through by the user.
*/
class Comments {
  constructor() {
    this.olderButton = document.createElement('button');
    this.newerButton = document.createElement('button');
    this.newerButton.addEventListener('click', () => this.handleNewer());
    this.olderButton.addEventListener('click', () => this.handleOlder());
    this.buttonContainer = document.getElementById('button-container');

    /* Setting up cursors */
    this.queryCursors = ["none"]; // Arbitrary keyword for first batch of data-
    this.cursorIndex = 0;         // this tells the servlet to start at beginning

    /* Creating newer button */
    this.newerButton.textContent = "⟨ newer";
    this.buttonContainer.appendChild(this.newerButton);

    /* Creating older button */
    this.olderButton.textContent = "older ⟩"
    this.buttonContainer.appendChild(this.olderButton);
  }
  /* Handles keypress to display older comments */
  handleOlder() {
    this.cursorIndex++;
    this.updateQuotesFromServer("forwards");
  }
  /* Handles keypress to display newer comments */
  handleNewer() {
    if (this.cursorIndex != 0) {
      this.cursorIndex--;
      this.updateQuotesFromServer("backwards");
    }
  }
  /**
    * Fetches messages from the server to update the page with.
    */
  async updateQuotesFromServer(direction) {
    const response = await fetch(`/data?scrs=${this.queryCursors[this.cursorIndex]}`);
    const messages = await response.json();
    const cursor = await response.headers.get("Cursor");
    
    if (!(cursor === this.queryCursors[this.cursorIndex])) { //When end of data is reached, cursor will equal the previous one
      if (direction === "forwards") { //only adds next cursor when moving forwards
        this.queryCursors[this.cursorIndex + 1] = cursor;
      }
      let messageList = document.getElementById('server-messages');
      messageList.textContent = '';
      for (let i = 0; i < messages.length; i++) {
        const liElement = document.createElement('p');
        liElement.setAttribute("class", "comment");
        liElement.textContent = `${messages[i].username}: ${messages[i].text}`;
        messageList.appendChild(liElement);
      }
    }
    else {
      this.cursorIndex--; //fixes cursor index if it was moved to end of data
    }
  }
  /**
    * Deletes all messages from database & clears from screen
    */
  async deleteMessages() {
    const dataDelete = await fetch('/delete-data');
    this.updateQuotesFromServer();
  }
}

document.addEventListener('DOMContentLoaded', () => {
    const container = document.getElementById('photos');
    const slideshow = new SlideShow(document.getElementById('photos'));
    const comments = new Comments();
    comments.updateQuotesFromServer("forwards"); //Automatically adds a cursor for the next page to the array
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
