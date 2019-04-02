package org.devmaster.elasticsearch.index;

import org.devmaster.elasticsearch.index.mapper.Recurring;

import org.joda.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

import java.text.ParseException;
import java.util.List;

public class RecurringTest {

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

    public void testNextOccurrence() throws Exception {
        
        LocalDate today = LocalDate.now();

        assertNull(recurring("2016-11-10", null, null).getNextOccurrence(today));
        assertNull(recurring("2016-11-10", "2016-11-15", null).getNextOccurrence(today));
        assertNull(recurring("2016-02-10", null, "RRULE:FREQ=MONTHLY;BYDAY=SU;COUNT=1").getNextOccurrence(today));

        // Testing single date
        LocalDate nextOccurrence = recurring(today.toString("yyyy-MM-dd"), null, null).getNextOccurrence(today);
        assertNotNull(nextOccurrence);
        assertTrue(today.isEqual(nextOccurrence));

        LocalDate nextOccurrence1;
        nextOccurrence1 = recurring(today.toString("yyyy-MM-dd"), today.plusDays(5).toString("yyyy-MM-dd"), null).getNextOccurrence(today);
        assertNotNull(nextOccurrence1);
        assertTrue(today.isEqual(nextOccurrence1));

        // Testing recurrence
        LocalDate nextOccur = recurring(today.minusDays(160).toString("yyyy-MM-dd"), null, "RRULE:FREQ=MONTHLY;BYDAY=MO")
                .getNextOccurrence(today);

        assertNotNull(nextOccur);
        assertEquals(1, nextOccur.getDayOfWeek());
        assertTrue(nextOccur.isEqual(today) || nextOccur.isAfter(today));
    }

    public void testOccurBetweenWithSingleDay() throws Exception {
        Recurring recurring = new Recurring("2016-11-10", null, null);
		
		// assertTrue
        assertTrue(recurring.occurBetween("2016-11-09", "2016-11-10"));
        assertTrue(recurring.occurBetween("2016-11-09", "2016-11-11"));
        assertTrue(recurring.occurBetween("2016-11-10", "2016-11-10"));
        assertTrue(recurring.occurBetween("2016-11-10", "2016-11-11"));
        
		// assertFalse
		assertFalse(recurring.occurBetween("2016-11-08", "2016-11-09"));
        assertFalse(recurring.occurBetween("2016-11-11", "2016-11-12"));
    }

    public void testOccurBetweenWithRangeOfDay() throws Exception {
        Recurring recurring = new Recurring("2016-11-10", "2016-11-11", null);

		// assertTrue
        assertTrue(recurring.occurBetween("2016-11-10", "2016-11-15"));
        assertTrue(recurring.occurBetween("2016-11-11", "2016-11-15"));
        assertTrue(recurring.occurBetween("2016-11-11", "2016-11-14"));
        assertTrue(recurring.occurBetween("2016-11-09", "2016-11-14"));
        assertTrue(recurring.occurBetween("2016-11-09", "2016-11-10"));
        assertTrue(recurring.occurBetween("2016-11-09", "2016-11-16"));

		// assertFalse
        assertFalse(recurring.occurBetween("2016-11-13", "2016-11-16"));
    }

    public void testHasAnyOccurrenceBetween() throws Exception {
        Recurring recurring = new Recurring("2017-06-01", "2017-06-30", null);

		// assertTrue
        assertTrue(recurring.hasAnyOccurrenceBetween("2017-05-20", "2017-06-01"));
        assertTrue(recurring.hasAnyOccurrenceBetween("2017-06-01", "2017-06-01"));
        assertTrue(recurring.hasAnyOccurrenceBetween("2017-06-01", "2017-06-10"));
        assertTrue(recurring.hasAnyOccurrenceBetween("2017-06-01", "2017-06-30"));
        assertTrue(recurring.hasAnyOccurrenceBetween("2017-06-15", "2017-07-01"));
        assertTrue(recurring.hasAnyOccurrenceBetween("2017-06-30", "2017-07-01"));
        assertTrue(recurring.hasAnyOccurrenceBetween("2017-06-30", "2017-06-30"));
        assertTrue(recurring.hasAnyOccurrenceBetween("2017-03-30", "2017-08-30"));
        assertTrue(recurring.hasAnyOccurrenceBetween("2017-06-27", "2017-06-27"));
        
		// assertFalse
        assertFalse(recurring.hasAnyOccurrenceBetween("2017-05-20", "2017-05-25"));
		assertFalse(recurring.hasAnyOccurrenceBetween("2017-07-01", "2017-07-15"));
    }

    public void testHasAnyOccurrenceBetween_recurrences() throws Exception {
        Recurring recurring = new Recurring("2017-06-05", null, "RRULE:FREQ=MONTHLY;BYDAY=MO");

        //assertTrue
        assertTrue(recurring.hasAnyOccurrenceBetween("2017-06-01", "2017-06-05"));
        assertTrue(recurring.hasAnyOccurrenceBetween("2017-06-01", "2017-06-06"));
        assertTrue(recurring.hasAnyOccurrenceBetween("2017-06-18", "2017-06-20"));
        assertTrue(recurring.hasAnyOccurrenceBetween("2017-06-19", "2017-06-19"));
        assertTrue(recurring.hasAnyOccurrenceBetween("2017-06-19", "2017-06-20"));
        
		// assertFalse
		assertFalse(recurring.hasAnyOccurrenceBetween("2017-06-01", "2017-06-04"));
		assertFalse(recurring.hasAnyOccurrenceBetween("2017-06-20", "2017-06-25"));
    }

    public void testOccurBetweenWithSameDay() throws Exception {
        Recurring recurring = new Recurring("2016-11-10", "2016-11-10", null);

		// assertTrue
        assertTrue(recurring.occurBetween("2016-11-10", "2016-11-10"));
        assertTrue(recurring.occurBetween("2016-11-10", "2016-11-11"));
        assertTrue(recurring.occurBetween("2016-11-09", "2016-11-10"));
        assertTrue(recurring.occurBetween("2016-11-09", "2016-11-11"));

		// assertFalse
        assertFalse(recurring.occurBetween("2016-11-09", "2016-11-09"));
        assertFalse(recurring.occurBetween("2016-11-11", "2016-11-13"));
    }

    public void testOccurBetweenWithRecurrence() throws Exception {
        Recurring recurring = new Recurring("2016-11-10", null, "RRULE:FREQ=DAILY;BYDAY=TH,MO");
        
        // assertTrue
		assertTrue(recurring.occurBetween("2016-11-10", "2016-11-15"));
        assertTrue(recurring.occurBetween("2016-11-11", "2016-11-15"));
        assertTrue(recurring.occurBetween("2016-11-09", "2016-11-10"));
        assertTrue(recurring.occurBetween("2016-11-14", "2016-11-16"));
        assertTrue(recurring.occurBetween("2016-12-05", "2016-12-06"));
        
        // assertFalse
        assertFalse(recurring.occurBetween("2016-11-22", "2016-11-23"));
        assertFalse(recurring.occurBetween("2016-11-15", "2016-11-15"));
    }

    public void testOccurBetweenWithEndDateAndStartDateAreTheSame() throws ParseException {
        Recurring recurring = new Recurring("2018-02-28", null, "RRULE:FREQ=WEEKLY;BYDAY=WE");
        assertTrue(recurring.occurBetween("2018-02-28", "2018-02-28"));
    }

    public void textHasOccurrencesAt() throws Exception {
        
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
    
    public void testHasAnyOccurrenceBetweenWeekly() throws Exception {
        Recurring recurring = new Recurring("2018-05-02", "2018-06-07", "RRULE:FREQ=WEEKLY;BYDAY=TU;UNTIL=20180607T000000Z;WKST=SU");
        
        // assertTrue
		assertTrue(recurring.hasAnyOccurrenceBetween("2018-05-08", "2018-05-08"));
        assertTrue(recurring.hasAnyOccurrenceBetween("2018-05-15", "2018-05-15"));
        assertTrue(recurring.hasAnyOccurrenceBetween("2018-05-22", "2018-05-22"));
        assertTrue(recurring.hasAnyOccurrenceBetween("2018-05-29", "2018-05-29"));
        assertTrue(recurring.hasAnyOccurrenceBetween("2018-06-05", "2018-06-05"));
        
        // assertFalse
        assertFalse(recurring.hasAnyOccurrenceBetween("2018-06-06", "2018-06-06"));
    }

    private Recurring recurring(String start, String end, String rrule) {
        return new Recurring(start, end, rrule);
    }

    private LocalDate toLocalDate(String date) {
        return new LocalDate(date);
    }

}
