/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.devmaster.elasticsearch.index.mapper;

import org.joda.time.LocalDate;
import org.junit.Test;

import static org.junit.Assert.*;

public class RecurringTest {

    @Test
    public void test_notHasExpired() throws Exception {
        LocalDate today = LocalDate.now();

        // Single date
        assertFalse(recurring(today.minusDays(1).toString(), null, null).notHasExpired());
        assertTrue(recurring(today.plusDays(1).toString(), null, null).notHasExpired());
        assertTrue(recurring(today.toString(), null, null).notHasExpired());

        // Range of dates
        assertFalse(recurring(today.minusDays(5).toString(), today.minusDays(1).toString(), null).notHasExpired());
        assertTrue(recurring(today.plusDays(1).toString(), today.plusDays(4).toString(), null).notHasExpired());
        assertTrue(recurring(today.toString(), today.plusDays(2).toString(), null).notHasExpired());

        // Recurrence
        Recurring recurring = recurring("2016-11-10", null, "RRULE:FREQ=MONTHLY;BYDAY=SU,WE,FR");
        assertTrue(recurring.notHasExpired());

        Recurring recurring1 = recurring("2016-02-10", null, "RRULE:FREQ=MONTHLY;BYDAY=SU,WE,FR;COUNT=5");
        assertFalse(recurring1.notHasExpired());
    }

    @Test
    public void test_nextOccurrence() throws Exception {
        assertNull(recurring("2016-11-10", null, null).getNextOccurrence());
        assertNull(recurring("2016-11-10", "2016-11-15", null).getNextOccurrence());
        assertNull(recurring("2016-02-10", null, "RRULE:FREQ=MONTHLY;BYDAY=SU;COUNT=1").getNextOccurrence());

        LocalDate today = LocalDate.now();

        // Testing single date
        LocalDate nextOccurrence = recurring(today.toString("yyyy-MM-dd"), null, null).getNextOccurrence();
        assertNotNull(nextOccurrence);
        assertTrue(today.isEqual(nextOccurrence));

        // Testing a range of dates
        LocalDate nextOccurrence1 = recurring(today.toString("yyyy-MM-dd"), today.plusDays(5).toString("yyyy-MM-dd"), null).getNextOccurrence();
        assertNotNull(nextOccurrence1);
        assertTrue(today.isEqual(nextOccurrence1));

        // Testing recurrence
        LocalDate nextOccurr = recurring(today.minusDays(160).toString("yyyy-MM-dd"), null, "RRULE:FREQ=MONTHLY;BYDAY=MO")
                .getNextOccurrence();

        assertNotNull(nextOccurr);
        assertEquals(1, nextOccurr.getDayOfWeek());
        assertTrue(nextOccurr.isEqual(today) || nextOccurr.isAfter(today));
    }

    @Test
    public void test_occurrBetween_withSingleDay() throws Exception {
        Recurring recurring = new Recurring("2016-11-10", null, null);

        assertTrue(recurring.occurBetween("2016-11-09", "2016-11-10"));
        assertTrue(recurring.occurBetween("2016-11-09", "2016-11-11"));
        assertTrue(recurring.occurBetween("2016-11-10", "2016-11-10"));
        assertTrue(recurring.occurBetween("2016-11-10", "2016-11-11"));
        assertFalse(recurring.occurBetween("2016-11-08", "2016-11-09"));
        assertFalse(recurring.occurBetween("2016-11-11", "2016-11-12"));
    }

    @Test
    public void test_occurrBetween_withRangeOfDay() throws Exception {
        Recurring recurring = new Recurring("2016-11-10", "2016-11-15", null);

        assertTrue(recurring.occurBetween("2016-11-10", "2016-11-15"));
        assertTrue(recurring.occurBetween("2016-11-11", "2016-11-15"));
        assertTrue(recurring.occurBetween("2016-11-11", "2016-11-14"));

        assertFalse(recurring.occurBetween("2016-11-09", "2016-11-10"));
        assertFalse(recurring.occurBetween("2016-11-13", "2016-11-16"));
        assertFalse(recurring.occurBetween("2016-11-09", "2016-11-16"));
    }

    @Test
    public void test_occurrBetween_withReccurrence() throws Exception {
        Recurring recurring = new Recurring("2016-11-10", null, "RRULE:FREQ=DAILY;BYDAY=TH,MO");

        assertTrue(recurring.occurBetween("2016-11-10", "2016-11-15"));
        assertTrue(recurring.occurBetween("2016-11-11", "2016-11-15"));
        assertTrue(recurring.occurBetween("2016-11-09", "2016-11-10"));
        assertTrue(recurring.occurBetween("2016-11-14", "2016-11-16"));
        assertTrue(recurring.occurBetween("2016-12-05", "2016-12-06"));
        assertFalse(recurring.occurBetween("2016-11-22", "2016-11-23"));
        assertFalse(recurring.occurBetween("2016-11-15", "2016-11-15"));
    }

    @Test
    public void text_hasOccurrencesAt() throws Exception {
        // Testing single date
        Recurring singleDate = recurring("2016-11-23", null, null);
        assertTrue(singleDate.hasOccurrencesAt(toLocalDate("2016-11-23")));
        assertFalse(singleDate.hasOccurrencesAt(toLocalDate("2016-11-24")));

        // Testing range of dates
        Recurring rangeOfDates = recurring("2016-11-23", "2016-11-25", null);
        assertTrue(rangeOfDates.hasOccurrencesAt(toLocalDate("2016-11-23")));
        assertTrue(rangeOfDates.hasOccurrencesAt(toLocalDate("2016-11-24")));
        assertTrue(rangeOfDates.hasOccurrencesAt(toLocalDate("2016-11-25")));
        assertFalse(rangeOfDates.hasOccurrencesAt(toLocalDate("2016-11-22")));
        assertFalse(rangeOfDates.hasOccurrencesAt(toLocalDate("2016-11-26")));

        // Testing recurrence
        Recurring recurrence = recurring("2016-11-23", null, "RRULE:FREQ=MONTHLY;BYDAY=MO,WE");
        assertTrue(recurrence.hasOccurrencesAt(toLocalDate("2016-11-23")));
        assertTrue(recurrence.hasOccurrencesAt(toLocalDate("2016-11-28")));
        assertTrue(recurrence.hasOccurrencesAt(toLocalDate("2016-12-19")));

        assertFalse(recurrence.hasOccurrencesAt(toLocalDate("2016-11-07")));
        assertFalse(recurrence.hasOccurrencesAt(toLocalDate("2016-11-24")));
        assertFalse(recurrence.hasOccurrencesAt(toLocalDate("2016-12-27")));
    }

    private Recurring recurring(String start, String end, String rrule) {
        return new Recurring(start, end, rrule);
    }

    private LocalDate toLocalDate(String date) {
        return new LocalDate(date);
    }

}