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

var slideIndex = 1;

/**
 * Applies number received from next/prev buttons (1 or -1) to show next or previous slide
 * from the current slide index.
 */
function moveSlides(n) {
  showSlides(slideIndex += n);
}

/**
 * Shows slide at slideIndex and checks/adjusts if index is out of bounds.
 */
function showSlides(n) {
    let i;
    const slides = document.querySelectorAll('.slides');
    if (n > slides.length) {
        slideIndex = 1;
    }
    if (n < 1) {
        slideIndex = slides.length;
    }
    for (i = 0; i < slides.length; i++) {
        slides[i].style.display = "none";
    }
    slides[slideIndex-1].style.display = "block";
}

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
