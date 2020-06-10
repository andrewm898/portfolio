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
import java.util.HashMap;
import java.util.ArrayList;
import java.time.LocalDate;  
import java.time.DayOfWeek;
import com.google.gson.Gson;
import com.google.sps.data.Weekday;

/** Servlet that returns some example content. */
@WebServlet("/visits")
public class SiteVisits extends HttpServlet {

  /*
   * Gets all weekday entries from datastore and sends back as Json objects
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Weekdays").addSort("index", SortDirection.ASCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery prepQuery = datastore.prepare(query);

    ArrayList<Weekday> dailyVisits = new ArrayList<Weekday>();

    for (Entity entity : prepQuery.asIterable()) {
      /* fills arraylist with all existing weekday objects */
      Weekday weekday = new Weekday((String) entity.getProperty("day"),
                                    Integer.parseInt(entity.getProperty("visits").toString()));
      dailyVisits.add(weekday);
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
    /* dayObject string is stored in all caps, this creates string with only first letter capitalized */
    String day = dayObject.toString().substring(0, 1).toUpperCase() + dayObject.toString().substring(1).toLowerCase();

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Filter propertyFilter = new FilterPredicate("day", FilterOperator.EQUAL, day.toString());
    Query query = new Query("Weekdays").setFilter(propertyFilter);
    PreparedQuery prepQuery = datastore.prepare(query);
    
    /* Entities of kind 'Weekdays' have 3 properties; a day (string for a day of the week), integer # of visits, 
     * and an index (Sunday is 0, Monday is 1, etc.) */

    boolean entityFound = false;
    Entity currentDay = new Entity("Weekdays");
    for (Entity entity : prepQuery.asIterable()) {
      int totalViews = 0;
      if ((entity.getProperty("visits") != null) &&
         (entity.getProperty("day").equals(day))) { //ensures 'entity' is correct one
        totalViews = Integer.parseInt(entity.getProperty("visits").toString());
        currentDay = entity;
        currentDay.setProperty("visits", totalViews + 1);
        entityFound = true;
      }
      break; /* Only one entity should be queried, this ensures only one is looked at */
    }

    if (!entityFound) {
      /* If the datastore does not have an entity matching today's 'day' string (meaning the site hasn't
       * been visited on the current day of the week), it gets created here */

      /* Sets up indexes to use for each day of the week */
      HashMap<String, Integer> weekdays = new HashMap<String, Integer>();
      weekdays.put("Sunday", 0);
      weekdays.put("Monday", 1);
      weekdays.put("Tuesday", 2);
      weekdays.put("Wednesday", 3);
      weekdays.put("Thursday", 4);
      weekdays.put("Friday", 5);
      weekdays.put("Saturday", 6);

      Integer index = weekdays.get(day);

      /* Assigns all relevant fields to currentDay entity */
      currentDay.setProperty("visits", 1);
      currentDay.setProperty("index", index);
      currentDay.setProperty("day", day);
    }
    datastore.put(currentDay);
  }
}
