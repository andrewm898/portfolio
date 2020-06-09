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


/** Creates a chart and adds it to the page. */
function drawChart() {
  const data = new google.visualization.DataTable();
  data.addColumn('string', 'Weekday');
  data.addColumn('number', 'Visits');
        data.addRows([
          ['Monday', 10],
          ['Tuesday', 5],
          ['Wednesday', 11],
          ['Thursday', 12],
          ['Friday', 13]
        ]);

  const options = {
    'title': 'Site Visits',
    'width':500,
    'height':400
  };

  const chart = new google.charts.Bar(
      document.getElementById('chart-container'));
  chart.draw(data, options);
}