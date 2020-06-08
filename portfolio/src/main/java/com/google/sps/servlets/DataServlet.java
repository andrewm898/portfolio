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
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.sps.data.Message;

/** Servlet that returns some example content. */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  public int maxComments;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(5);
    String startCursor = request.getParameter("scrs");

    if (!startCursor.equals("none")) { //if the given cursor is 'none' no cursor is necessary
      fetchOptions.startCursor(Cursor.fromWebSafeString(startCursor));
    }
    Query query = new Query("Messages").addSort("timestampMillis", SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery prepQuery = datastore.prepare(query);
    
    QueryResultList<Entity> results;

    results = prepQuery.asQueryResultList(fetchOptions);

    Cursor endCursor = results.getCursor();
    String encodedEndCursor = endCursor.toWebSafeString();

    ArrayList<Message> messages = new ArrayList<Message>();

    for (Entity entity : results) {
      Message msg = new Message((String) entity.getProperty("username"),
                                (String) entity.getProperty("text"),
                                (long) entity.getProperty("timestampMillis"));
      messages.add(msg);
    }

    Gson gson = new Gson();
    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(messages));
    response.addHeader("Cursor", encodedEndCursor);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String text = request.getParameter("text-input");
    String username = request.getParameter("username");
    long timestampMillis = System.currentTimeMillis();
    String maxCommentsString = request.getParameter("max-comments");
    try {
      maxComments = Integer.parseInt(maxCommentsString);
    } catch (NumberFormatException e) {
      System.err.println("Could not convert to int: " + maxCommentsString);
      maxComments = Integer.MAX_VALUE; //displays all comments if user does not enter valid number
    }
    if ((username == null) || (username.isEmpty())) {
      username = "Anon";
    }
    if ((text != null) && !(text.isEmpty())) {
      Entity messageEntity = new Entity("Messages");
      messageEntity.setProperty("text", text);
      messageEntity.setProperty("username", username);
      messageEntity.setProperty("timestampMillis", timestampMillis);
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      datastore.put(messageEntity);
    }
    response.sendRedirect("/index.html");
  }
}
