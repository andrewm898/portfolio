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

    /* Gets all busy time ranges for optional and mandatory meeting attendees */
    List<TimeRange> mandatoryBusyTimes = new ArrayList<TimeRange>();
    List<TimeRange> optionalBusyTimes = new ArrayList<TimeRange>();
    for (Event event : events) {
      for (String attendee : event.getAttendees()) {
        /* Adds the attendee to the mandatory or optional list */
        if (request.getAttendees().contains(attendee)) {
          mandatoryBusyTimes.add(event.getWhen());
        } else if (request.getOptionalAttendees().contains(attendee)) {
          optionalBusyTimes.add(event.getWhen());
        }
        break;
      }
    }

    /* First tries to find times that allow mandatory AND optional attendees to attend.
     * If found, only those times are returned. If not, then all of the times that
     * work for only the mandatory people are returned */

    List<TimeRange> combinedBusyTimes = new ArrayList<TimeRange>(mandatoryBusyTimes);
    combinedBusyTimes.addAll(optionalBusyTimes);

    /* If none of the attendees are busy at any time, return the whole day */
    if (combinedBusyTimes.isEmpty()) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    /* ORDER_BY_START reorders the given list strictly by the TimeRange's start time, regardless
     * of end time. This enables the following merge algorithm to work properly */
    Collections.sort(combinedBusyTimes, TimeRange.ORDER_BY_START);

    /* Merges all overlaps in the combined mandatory & optional busy times */
    List<TimeRange> mergedCombinedBusyTimes = mergeOverlappingRanges(combinedBusyTimes);

    /* Checks if there are time ranges that work for both mandatory & optional attendees, if there are, return them */
    Collection<TimeRange> availableCombinedTimes = new ArrayList<TimeRange>();
    availableCombinedTimes = processAvailableTimes(mergedCombinedBusyTimes, request.getDuration());

    if (!availableCombinedTimes.isEmpty()) {
      return availableCombinedTimes;
    }

    /* If there are no times where the optional people can join, code finds times that work for mandatory only */

    /* If none of the mandatory attendees are busy at any time, return the whole day */
    if (mandatoryBusyTimes.isEmpty()) {
      if (mandatoryBusyTimes.isEmpty()) {
        Collection<TimeRange> emptyCollection = Arrays.asList();
        return emptyCollection;
      }
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    Collections.sort(mandatoryBusyTimes, TimeRange.ORDER_BY_START);

    List<TimeRange> mergedMandatoryBusyTimes = new ArrayList<TimeRange>();
    mergedMandatoryBusyTimes = mergeOverlappingRanges(mandatoryBusyTimes);

    Collection<TimeRange> availableMandatoryTimes = new ArrayList<TimeRange>();

    availableMandatoryTimes = processAvailableTimes(mergedMandatoryBusyTimes, request.getDuration());

    return availableMandatoryTimes;
  }
  
  public List<TimeRange> mergeOverlappingRanges(List<TimeRange> busyTimes) {
    /* A list of non overlapping time ranges made by merging all overlapping original ranges */
    List<TimeRange> mergedBusyTimes = new ArrayList<TimeRange>();

    /* Merging all overlapping busy times */
    for (int i = 0; i < busyTimes.size(); i++) {
      /* If the current timerange overlaps with a merged one, replace with a combo of both*/
      if ((!mergedBusyTimes.isEmpty()) && (busyTimes.get(i).overlaps(mergedBusyTimes.get(mergedBusyTimes.size() - 1)))) {
        int lastMergedIndex = mergedBusyTimes.size() - 1;
        TimeRange lastMergedRange = mergedBusyTimes.get(lastMergedIndex);
        
        /* newStart is earliest start, which will be the merged timerange, newEnd is latest end */
        int newStart = lastMergedRange.start();
        int newEnd = Math.max(busyTimes.get(i).end(), lastMergedRange.end());
        
        TimeRange mergedRange = TimeRange.fromStartEnd(newStart, newEnd, false);
        mergedBusyTimes.set(lastMergedIndex, mergedRange);
      } else {
        /* If there is no overlap & the ith index wasn't previously added, add it now */
        mergedBusyTimes.add(busyTimes.get(i));
      }
    }
    return mergedBusyTimes;
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
