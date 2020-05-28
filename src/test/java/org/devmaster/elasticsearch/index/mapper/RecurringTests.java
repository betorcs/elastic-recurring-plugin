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

import org.devmaster.elasticsearch.Recurring;
import org.joda.time.LocalDate;
import org.junit.Test;

import java.text.ParseException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


public class RecurringTests {

    @Test
    public void testOccurrencesBetween() throws ParseException {
        Recurring recurring = recurring("2016-01-01", null, "RRULE:FREQ=WEEKLY;BYDAY=TU,TH;WKST=SU");
        LocalDate start = new LocalDate("2016-01-02");
        LocalDate end = new LocalDate("2016-01-20");
        List<String> occurrences = recurring.occurrencesBetween(start, end);
        assertNotNull(occurrences);
        assertEquals(5, occurrences.size());

        Recurring coogeePro = recurring("2018-02-21", null, "RRULE:FREQ=MONTHLY;BYDAY=4WE;WKST=SU");
        List<String> ocurrences = coogeePro.occurrencesBetween(new LocalDate(2018, 2, 16), new LocalDate(2018, 3, 16));
        assertEquals(2, ocurrences.size());
        assertEquals("2018-02-21", ocurrences.get(0));
    }

    @Test
    public void testNotHasExpired() throws Exception {
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
    public void testNextOccurrence() throws Exception {

        LocalDate today = LocalDate.now();

        assertNull(recurring("2016-11-10", null, null).getNextOccurrence(today));
        assertNull(recurring("2016-11-10", "2016-11-15", null).getNextOccurrence(today));
        assertNull(recurring("2016-02-10", null, "RRULE:FREQ=MONTHLY;BYDAY=SU;COUNT=1").getNextOccurrence(today));

        // Testing single date
        LocalDate nextOccurrence = recurring(today.toString("yyyy-MM-dd"), null, null).getNextOccurrence(today);
        assertNotNull(nextOccurrence);
        assertTrue(today.isEqual(nextOccurrence));

        // Testing a range of dates
        LocalDate nextOccurrence1 = recurring(today.toString("yyyy-MM-dd"),
                today.plusDays(5).toString("yyyy-MM-dd"), null).getNextOccurrence(today);
        assertNotNull(nextOccurrence1);
        assertTrue(today.isEqual(nextOccurrence1));

        // Testing recurrence
        LocalDate nextOccur = recurring(today.minusDays(160).toString("yyyy-MM-dd"), null, "RRULE:FREQ=MONTHLY;BYDAY=MO")
                .getNextOccurrence(today);

        assertNotNull(nextOccur);
        assertEquals(1, nextOccur.getDayOfWeek());
        assertTrue(nextOccur.isEqual(today) || nextOccur.isAfter(today));
    }

    @Test
    public void testOccurBetween_withSingleDay() throws Exception {
        Recurring recurring = new Recurring("2016-11-10", null, null);

        assertTrue(recurring.occurBetween("2016-11-09", "2016-11-10"));
        assertTrue(recurring.occurBetween("2016-11-09", "2016-11-11"));
        assertTrue(recurring.occurBetween("2016-11-10", "2016-11-10"));
        assertTrue(recurring.occurBetween("2016-11-10", "2016-11-11"));
        assertFalse(recurring.occurBetween("2016-11-08", "2016-11-09"));
        assertFalse(recurring.occurBetween("2016-11-11", "2016-11-12"));
    }

    @Test
    public void testOccurBetween_withRangeOfDay() throws Exception {
        Recurring recurring = new Recurring("2016-11-10", "2016-11-11", null);

        assertTrue(recurring.occurBetween("2016-11-10", "2016-11-15"));
        assertTrue(recurring.occurBetween("2016-11-11", "2016-11-15"));
        assertTrue(recurring.occurBetween("2016-11-11", "2016-11-14"));
        assertTrue(recurring.occurBetween("2016-11-09", "2016-11-14"));
        assertTrue(recurring.occurBetween("2016-11-09", "2016-11-10"));
        assertTrue(recurring.occurBetween("2016-11-09", "2016-11-16"));

        assertFalse(recurring.occurBetween("2016-11-13", "2016-11-16"));
    }

    @Test
    public void testHasAnyOccurrenceBetween() throws Exception {
        Recurring recurring = new Recurring("2017-06-01", "2017-06-30", null);

        assertFalse(recurring.hasAnyOccurrenceBetween("2017-05-20", "2017-05-25"));
        assertTrue(recurring.hasAnyOccurrenceBetween("2017-05-20", "2017-06-01"));
        assertTrue(recurring.hasAnyOccurrenceBetween("2017-06-01", "2017-06-01"));
        assertTrue(recurring.hasAnyOccurrenceBetween("2017-06-01", "2017-06-10"));
        assertTrue(recurring.hasAnyOccurrenceBetween("2017-06-01", "2017-06-30"));
        assertTrue(recurring.hasAnyOccurrenceBetween("2017-06-15", "2017-07-01"));
        assertTrue(recurring.hasAnyOccurrenceBetween("2017-06-30", "2017-07-01"));
        assertTrue(recurring.hasAnyOccurrenceBetween("2017-06-30", "2017-06-30"));
        assertTrue(recurring.hasAnyOccurrenceBetween("2017-03-30", "2017-08-30"));
        assertTrue(recurring.hasAnyOccurrenceBetween("2017-06-27", "2017-06-27"));
        assertFalse(recurring.hasAnyOccurrenceBetween("2017-07-01", "2017-07-15"));

    }

    @Test
    public void testHasAnyOccurrenceBetween_recurrences() throws Exception {
        Recurring recurring = new Recurring("2017-06-05", null, "RRULE:FREQ=MONTHLY;BYDAY=MO");

        assertFalse(recurring.hasAnyOccurrenceBetween("2017-06-01", "2017-06-04"));
        assertTrue(recurring.hasAnyOccurrenceBetween("2017-06-01", "2017-06-05"));
        assertTrue(recurring.hasAnyOccurrenceBetween("2017-06-01", "2017-06-06"));
        assertTrue(recurring.hasAnyOccurrenceBetween("2017-06-18", "2017-06-20"));
        assertTrue(recurring.hasAnyOccurrenceBetween("2017-06-19", "2017-06-19"));
        assertTrue(recurring.hasAnyOccurrenceBetween("2017-06-19", "2017-06-20"));
        assertFalse(recurring.hasAnyOccurrenceBetween("2017-06-20", "2017-06-25"));

    }

    @Test
    public void testOccurBetween_withSameDay() throws Exception {
        Recurring recurring = new Recurring("2016-11-10", "2016-11-10", null);

        assertTrue(recurring.occurBetween("2016-11-10", "2016-11-10"));
        assertTrue(recurring.occurBetween("2016-11-10", "2016-11-11"));
        assertTrue(recurring.occurBetween("2016-11-09", "2016-11-10"));
        assertTrue(recurring.occurBetween("2016-11-09", "2016-11-11"));

        assertFalse(recurring.occurBetween("2016-11-09", "2016-11-09"));
        assertFalse(recurring.occurBetween("2016-11-11", "2016-11-13"));
    }

    @Test
    public void testOccurBetween_withRecurrence() throws Exception {
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
    public void testHasOccurrencesAt() throws Exception {
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