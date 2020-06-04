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
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;

/** Servlet that returns some example content. */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  public int maxComments;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Messages");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
    
    if (this.maxComments < 0) {
      this.maxComments = 0;
    }
    //if the user enters less than 0 it returns 0 comments, more than # of comments it simply shows them all
    ArrayList<String> messages = new ArrayList<String>();
    int loadedResults = 0;

    for (Entity entity : results.asIterable()) {
      if (loadedResults == maxComments) {
        break;
      }
      String message = (String) entity.getProperty("message");
      messages.add(message);
      loadedResults++;
    }
    Gson gson = new Gson();
    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(messages));
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String text = request.getParameter("text-input");
    String maxCommentsString = request.getParameter("max-comments");
    try {
      maxComments = Integer.parseInt(maxCommentsString);
    } catch (NumberFormatException e) {
      System.err.println("Could not convert to int: " + maxCommentsString);
      maxComments = Integer.MAX_VALUE; //displays all comments if user does not enter valid number
    }
    if ((text != null) && !(text.isEmpty())) {
      Entity messageEntity = new Entity("Messages");
      messageEntity.setProperty("message", text);
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      datastore.put(messageEntity);
    }
    response.sendRedirect("/index.html");
  }
}
