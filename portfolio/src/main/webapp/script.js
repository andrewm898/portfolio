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
    /* Setting up older/newer buttons */
    this.olderButton = document.createElement('button');
    this.newerButton = document.createElement('button');
    this.deleteButton = document.createElement('button');
    this.newerButton.addEventListener('click', () => this.handleNewer());
    this.olderButton.addEventListener('click', () => this.handleOlder());
    this.deleteButton.addEventListener('click', () => this.deleteMessages());
    this.buttonContainer = document.getElementById('button-container');

    /* Creating language selection options */
    this.currentLanguage = 'el'; /* default language is english, will add more in future */
    for (const languageInfo of [{"code":"en", "name":"English"},
                                {"code":"es", "name":"Spanish"},]) {
      const language = document.createElement('button');
      language.textContent = languageInfo.name;
      language.addEventListener('click', () => this.handleLanguage(languageInfo.code));
      document.getElementById('language-options').appendChild(language);
    }

    /* Setting up cursors */
    this.queryCursors = ["none"]; // Arbitrary keyword for first batch of data-

    /* Setting up message storing/indexing */
    this.messageList = document.getElementById('server-messages');
    this.messageArray = []; // Newer comments have smaller indexes, older comments are filled in at higher ones
    this.indexLimit = 0; // this variable holds the highest page index where the cursor following it
    // is known, so the index does not need to be taken from the server
    this.oldestFound = false; // oldestFound is true when an empty list of comments was found at a cursor
    this.activeIndex = 0;

    /* Creating newer button */
    this.newerButton.textContent = "⟨ newer";
    this.buttonContainer.appendChild(this.newerButton);

    /* Creating older button */
    this.olderButton.textContent = "older ⟩"
    this.buttonContainer.appendChild(this.olderButton);

    /* Creating delete button */
    this.deleteButton.textContent = "delete all"
    this.buttonContainer.appendChild(this.deleteButton);
  }

  /**
    * Handles a new language being selected
    */
  async handleLanguage(expectedLanguageId) {
    /* If the language has been changed, comments are reloaded in it */
    if (expectedLanguageId !== this.currentLanguage) {
      /* These values are cleared so comments are reloaded in different language */
      this.messageList.innerHTML = '';
      this.messageArray = [];
      this.indexLimit = 0;
      this.oldestFound = false;
      this.activeIndex = 0;
      this.currentLanguage = expectedLanguageId;
      this.render();
    }
  }
  /* Handles keypress to display older comments */
  async handleOlder() {
    if (!this.oldestFound) {
      this.indexLimit++;
      await this.getServerMessages();
    }
    if (this.activeIndex + 1 < this.messageArray.length) {
      this.messageArray[this.activeIndex].classList.remove("active-batch");
      this.activeIndex++;
      await this.showServerMessages();
    }
  }
  /* Handles keypress to display newer comments */
  handleNewer() {
    if (this.activeIndex > 0) {
      this.messageArray[this.activeIndex].classList.remove("active-batch");
      this.activeIndex--;
      this.showServerMessages();
    }
  }
  /*
   * Fetches messages from the server to store in message array using current language.
   */
  async getServerMessages() {
    const response = await fetch(`/data?scrs=${this.queryCursors[this.indexLimit]}&lan=${this.currentLanguage}`);
    const messages = await response.json();
    const cursor = await response.headers.get("Cursor");

    if (messages.length !== 0) {
      this.queryCursors[this.indexLimit + 1] = cursor;
      const messageBatch = document.createElement('div');
      messageBatch.classList.add('message-batch');
      for (let i = 0; i < messages.length; i++) {
        const listElement = document.createElement('p');
        listElement.setAttribute("class", "comment");
        listElement.textContent = `${messages[i].username}: ${messages[i].text}`;
        messageBatch.appendChild(listElement);
      }
      this.messageArray.push(messageBatch);
      this.messageList.appendChild(messageBatch);
    } else {
      this.oldestFound = true;
    }
  }
  /**
    * Displays messages at a specified index in the message array.
    */
  async showServerMessages() {
    this.messageArray[this.activeIndex].classList.add("active-batch");
  }
  /**
    * Calls to get most recent server message & shows active page
    */
  async render() {
    await this.getServerMessages();
    await this.showServerMessages();
  }
  /**
    * Deletes all messages from database & clears from screen
    */
  async deleteMessages() {
    const dataDelete = await fetch('/delete-data');
    this.render();
  }
}

document.addEventListener('DOMContentLoaded', () => {
  const visit = fetch("/visits", {method: 'POST'}); //records that site was visited
  const container = document.getElementById('photos');
  const slideshow = new SlideShow(document.getElementById('photos'));
  const comments = new Comments();
  comments.render();
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
