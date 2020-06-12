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
  const response = await fetch('/visits', {method: 'GET'});
  const visitArray = await response.json();
  
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
 