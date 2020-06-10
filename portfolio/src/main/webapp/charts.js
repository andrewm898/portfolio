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
 
google.charts.load('current', {packages: ['corechart', 'bar']});
google.charts.setOnLoadCallback(drawChart);
 
/**
 * Creates a chart and adds it to the page.
 */
async function drawChart() {
  const visitArray = await getVisitArray();
  
  const data = new google.visualization.DataTable();
  data.addColumn('string', 'Weekday');
  data.addColumn('number', 'Visits');
 
  for (let i = 0; i < visitArray.length; i++) {
    data.addRow([visitArray[i].dayName.toString(), visitArray[i].visitCount]);
  }
 
  const options = {
    title: "Site visits",
    width: 1000,
    height: 900,
  };
 
  const chart = new google.charts.Bar(
      document.getElementById('chart-container'));
  chart.draw(data, options);
}
 
/**
 * Gets all weekday entries from datastore,
 * returns an array with any missing entries filled in.
 */
 async function getVisitArray() {
  const response = await fetch('/visits', {method: 'GET'});
  const datastoreVisits = await response.json();
  /* datastoreVisits is array of weekday objects present in datastore. 
   * Each has a dayName string (Sunday, Monday, etc.), index integer 
   * (Sunday is 0), and a visitCount integer */
 
  let filledVisitArray = [];
  const indexedDayNames = ["Sunday", "Monday", "Tuesday", "Wednesday",
                           "Thursday", "Friday", "Saturday"];
  
  /* Missing entries in datastore are found by checking if the entry's index
   * matches what it should be if all prior entries were present. If it
   * does not, then that missing entry is created and added to filledVisitArray */
 
  let datastoreArrayIndex = 0;
  for (let i = 0; i < 7; i++) {
    if (((datastoreArrayIndex + 1) <= datastoreVisits.length) &&
        (datastoreVisits[datastoreArrayIndex].dayName === indexedDayNames[i])) {
      /* Checks if end of datastore array values have been reached && if value is
       * correct for its index */
      filledVisitArray.push(datastoreVisits[datastoreArrayIndex]);
      datastoreArrayIndex++;
    }
    else {
      /* if datastoreVisits[i].dayName is not the right name for its index,
       * the day at that index is missing from the datastore, so it is created. */
      const newVisitObject = {
        dayName : indexedDayNames[i],
        visitCount : 0
      }
      filledVisitArray.push(newVisitObject);
    }
  }
  return filledVisitArray;
 }