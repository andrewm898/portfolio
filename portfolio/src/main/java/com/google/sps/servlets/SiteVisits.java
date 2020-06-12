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

package com.google.sps.servlets;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query.SortDirection;
import java.util.ArrayList;
import java.util.HashMap;
import java.time.LocalDate;  
import java.time.DayOfWeek;
import com.google.gson.Gson;
import com.google.sps.data.Weekday;

/** Servlet that handles updating and requesting site viewcount data. */
@WebServlet("/visits")
public class SiteVisits extends HttpServlet {

  /*
   * Gets all weekday entries from datastore and sends back as Json objects
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Weekdays").addSort("index", SortDirection.ASCENDING);;
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery prepQuery = datastore.prepare(query);

    ArrayList<Weekday> dailyVisits = new ArrayList<Weekday>();

    /* Datastore entities are stored with indices, this maps day names to each index */
    HashMap<Long, String> indexToWeekday = new HashMap<Long, String>();
    
    indexToWeekday.put(new Long(0), "Monday");
    indexToWeekday.put(new Long(1), "Tuesday");
    indexToWeekday.put(new Long(2), "Wednesday");
    indexToWeekday.put(new Long(3), "Thursday");
    indexToWeekday.put(new Long(4), "Friday");
    indexToWeekday.put(new Long(5), "Saturday");
    indexToWeekday.put(new Long(6), "Sunday");

    HashMap<Long, Long> dayOfWeekToCount = new HashMap<Long, Long>();
    for (Entity entity : prepQuery.asIterable()) {
      if ((entity == null) || (entity.getProperty("index") == null)) {
      } else {
        long day = (long) entity.getProperty("index");
        long count = (long) entity.getProperty("visits");
        dayOfWeekToCount.put(day, count);
      }
    }
    for (long i = 0; i < 7; i++) {
      dailyVisits.add(new Weekday(indexToWeekday.get(i), dayOfWeekToCount.getOrDefault(i, new Long(0))));
    }
    Gson gson = new Gson();
    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(dailyVisits));
  }

  /* 
   * Increments number of visits for current day of the week by 1
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    LocalDate date = LocalDate.now();
    DayOfWeek dayObject = date.getDayOfWeek();

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Filter propertyFilter = new FilterPredicate("index", FilterOperator.EQUAL, dayObject.getValue() - 1);
    Query query = new Query("Weekdays").setFilter(propertyFilter);
    PreparedQuery prepQuery = datastore.prepare(query);
    
    /* Entities of kind 'Weekdays' have 2 properties; an integer # of visits, 
     * and an index (Monday is 0, etc.) */
    boolean entityFound = false;
    Entity currentDay = new Entity("Weekdays");
    for (Entity entity : prepQuery.asIterable()) {
      long totalViews = 0;
      if ((entity.getProperty("visits") != null)) { //ensures 'entity' is correct one
        totalViews = (long) entity.getProperty("visits");
        currentDay = entity;
        currentDay.setProperty("visits", totalViews + 1);
        entityFound = true;
      }
      break; /* Only one entity should be queried, this ensures only one is looked at */
    }

    if (!entityFound) {
      int index = dayObject.getValue() - 1;
      /* Assigns all relevant fields to currentDay entity */
      currentDay.setProperty("visits", 1);
      currentDay.setProperty("index", index);
    }
    datastore.put(currentDay);
  }
}