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

package com.google.sps;

import java.util.Collection;
import java.util.Collections;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Comparator;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {

    /* Returns an empty array if requested duration is more than the day */
    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      Collection<TimeRange> emptyCollection = Arrays.asList();
      return emptyCollection;
    }

    List<TimeRange> busyTimes = new ArrayList<TimeRange>();

    /* Finding all timeranges of existing events that attendees are going to */
    for (Event event : events) {
      Set<String> eventAttendees = event.getAttendees();
      for (String meetingAttendee : request.getAttendees()) {
        if (eventAttendees.contains(meetingAttendee)) {
          busyTimes.add(event.getWhen());
          break;          
        }
      }
    }

    /* If none of the attendees are busy at any time, return the whole day */
    if (busyTimes.isEmpty()) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    Collections.sort(busyTimes, TimeRange.ORDER_BY_START);

    /* A list of non overlapping time ranges made by merging all overlapping original ranges */
    List<TimeRange> mergedBusyTimes = new ArrayList<TimeRange>();

    /* Merging all overlapping busy times */
    for (int i = 0; i < busyTimes.size(); i++) {
      /* If the next timerange overlaps with current one, create a composite of the two */
      if ((i < (busyTimes.size() - 1)) && (busyTimes.get(i).overlaps(busyTimes.get(i + 1)))) {

        int newStart = busyTimes.get(i).start(); // earlier range will have earlier start time
        int newEnd = (busyTimes.get(i).end() >= busyTimes.get(i + 1).end()) ? busyTimes.get(i).end() : busyTimes.get(i + 1).end();
        
        TimeRange mergedRange = TimeRange.fromStartEnd(newStart, newEnd, false);
        busyTimes.set(i + 1, mergedRange); // This position is replaced in the array so it is not added twice
        mergedBusyTimes.add(mergedRange);
      } else if (!mergedBusyTimes.contains(busyTimes.get(i))) {
        /* If there is no overlap & the ith index wasn't previously added, add it now */
        mergedBusyTimes.add(busyTimes.get(i));
      }
    }

    int startTime = TimeRange.START_OF_DAY;
    int endTime = 0;
    Collection<TimeRange> validRanges = new ArrayList<TimeRange>();

    /* Checks each open time slot prior to a busy time in the non-overlapping calendar, adds if long enough */
    for (int i = 0; i < mergedBusyTimes.size(); i++) {
      endTime = mergedBusyTimes.get(i).start();
      TimeRange newRange = TimeRange.fromStartEnd(startTime, endTime, false);
      if (newRange.duration() >= request.getDuration()) {
        validRanges.add(newRange);
      }
      /* New start time is moved up to the end of this current time slot */
      startTime = mergedBusyTimes.get(i).end();
    }
    
    /* Loop exits w/ startTime as the latest end time, this checks if there is free time after that*/
    if ((startTime < TimeRange.END_OF_DAY) && ((TimeRange.END_OF_DAY - startTime) >= request.getDuration())) {
      endTime = TimeRange.END_OF_DAY;
      TimeRange newRange = TimeRange.fromStartEnd(startTime, endTime, true);
      validRanges.add(newRange);
    }
    return validRanges;
  }
}
