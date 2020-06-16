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

/* Returns a collection of TimeRange objects that the MeetingRequest can be scheduled in */
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
      /* If the current timerange overlaps with a merged one, replace with a combo of both*/
      if ((!mergedBusyTimes.isEmpty()) && (busyTimes.get(i).overlaps(mergedBusyTimes.get(mergedBusyTimes.size() - 1)))) {
        int lastMergedIndex = mergedBusyTimes.size() - 1;

        /* newStart is earliest start, which will be the merged timerange, newEnd is latest end */
        int newStart = mergedBusyTimes.get(mergedBusyTimes.size() - 1).start();
        int newEnd = (busyTimes.get(i).end() >= mergedBusyTimes.get(lastMergedIndex).end()) ?
                      busyTimes.get(i).end() : mergedBusyTimes.get(lastMergedIndex).end();
        
        TimeRange mergedRange = TimeRange.fromStartEnd(newStart, newEnd, false);
        mergedBusyTimes.set(lastMergedIndex, mergedRange);
      } else {
        /* If there is no overlap & the ith index wasn't previously added, add it now */
        mergedBusyTimes.add(busyTimes.get(i));
      }
    }

    Collection<TimeRange> availableMeetingTimes = new ArrayList<TimeRange>();

    availableMeetingTimes = processAvailableTimes(mergedBusyTimes, request.getDuration());

    return availableMeetingTimes;
  }

  public Collection<TimeRange> processAvailableTimes(List<TimeRange> mergedBusyTimes, long minDuration) {
    Collection<TimeRange> availableMeetingTimes = new ArrayList<TimeRange>();
    int availableTimeStart = TimeRange.START_OF_DAY;
    int availableTimeEnd = 0;

    /* Ensures there is a placeholder at the end of the list, +1 is to correctly assign end of day field */
    if (mergedBusyTimes.get(mergedBusyTimes.size() - 1).end() != TimeRange.END_OF_DAY) {
      TimeRange endHolder = TimeRange.fromStartEnd(TimeRange.END_OF_DAY + 1, TimeRange.END_OF_DAY + 1, false);
      mergedBusyTimes.add(endHolder);
    }

    /* Checks each open time slot prior to a busy time in the non-overlapping calendar, adds if long enough */
    for (int i = 0; i < mergedBusyTimes.size(); i++) {
      availableTimeEnd = mergedBusyTimes.get(i).start();
      TimeRange newRange = TimeRange.fromStartEnd(availableTimeStart, availableTimeEnd, false);
      if (newRange.duration() >= minDuration) {
        availableMeetingTimes.add(newRange);
      }
      /* New start time is moved up to the end of this current time slot */
      availableTimeStart = mergedBusyTimes.get(i).end();
    }
    return availableMeetingTimes;
  }
}
