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

import com.google.ical.compat.jodatime.LocalDateIterator;
import com.google.ical.compat.jodatime.LocalDateIteratorFactory;
import org.elasticsearch.common.Strings;
import org.joda.time.LocalDate;
import org.joda.time.Instant;
import org.joda.time.Interval;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Strings.emptyToNull;

public final class Recurring {

    private String startDate;
    private String endDate;
    private String rrule;

    public Recurring() {
        //
    }

    public Recurring(String startDate, String endDate, String rrule) {
        setStartDate(startDate);
        setEndDate(endDate);
        setRrule(rrule);
    }

    String getStartDate() {
        return startDate;
    }

    void setStartDate(String startDate) {
        if (Strings.isNullOrEmpty(startDate)) {
            throw new IllegalArgumentException("Parameter startDate can not be null or empty");
        }
        
        this.startDate = startDate;
    }

    String getEndDate() {
        return endDate;
    }

    void setEndDate(String endDate) {
        this.endDate = emptyToNull(endDate);
    }

    String getRrule() {
        return rrule;
    }

    void setRrule(String rrule) {
        this.rrule = emptyToNull(rrule);
    }

    public boolean hasOccurrencesAt(final LocalDate date) throws ParseException {
        if (this.rrule != null) {
            LocalDate end = date.plusDays(1);
            LocalDateIterator it = LocalDateIteratorFactory.createLocalDateIterator(rrule, new LocalDate(this.startDate), false);
            it.advanceTo(date);
            return it.hasNext() && it.next().isBefore(end);
        } else if (this.endDate != null) {
            LocalDate start = new LocalDate(this.startDate);
            LocalDate end = new LocalDate(this.endDate);
            return !date.isBefore(start) && !date.isAfter(end);
        } else {
            return new LocalDate(this.startDate).isEqual(date);
        }
    }

    public boolean occurBetween(String start, String end) throws ParseException {
        return occurBetween(new LocalDate(start), new LocalDate(end));
    }

    public boolean occurBetween(final LocalDate start, final LocalDate end) throws ParseException {
        LocalDate start_date = new LocalDate(this.startDate);
        LocalDate end_date = this.endDate != null ? new LocalDate(this.endDate) : null;

        if (rrule != null) {

            LocalDateIterator it = LocalDateIteratorFactory.createLocalDateIterator(rrule, start_date, false);
            it.advanceTo(start);

            if (it.hasNext()) {
                LocalDate nextOccurrence = it.next();
                return nextOccurrence != null && !nextOccurrence.isBefore(start) && !nextOccurrence.isAfter(end);
            } else {
                return false;
            }

        } else if (end_date != null && end_date.isAfter(start_date)) {
            return isBetween(start_date, start, end) || isBetween(end_date, start, end);
        }

        return !start_date.isBefore(start) && !start_date.isAfter(end);
    }

    private boolean isBetween(LocalDate date, LocalDate start, LocalDate end) {
        return date.compareTo(start) > -1 && date.compareTo(end) < 1;
    }

    public LocalDate getNextOccurrence(LocalDate date) throws ParseException {

        final LocalDate start = new LocalDate(this.startDate);

        if (this.rrule != null) {
            LocalDateIterator it = LocalDateIteratorFactory.createLocalDateIterator(rrule, start.minusDays(1), false);
            it.advanceTo(date);
            return it.hasNext() ? it.next() : null;
        } else if (this.endDate == null) {
            return !date.isAfter(start) ? start : null;
        } else {
            LocalDate end = new LocalDate(this.endDate);
            return !date.isAfter(end) ? start : null;
        }
    }

    public boolean notHasExpired() throws ParseException {
        final LocalDate today = LocalDate.now();
        return getNextOccurrence(today) != null;
    }

    public List<String> occurrencesBetween(LocalDate start, LocalDate end) throws ParseException {
        final LocalDate date = new LocalDate(this.startDate);
        List<String> dates = new ArrayList<String>();
        
        if (this.rrule != null) {
            LocalDateIterator it = LocalDateIteratorFactory.createLocalDateIterator(rrule, date, true);
            it.advanceTo(start);

            if (it.hasNext()) {
                do {
                    LocalDate current = it.next();
                    if (current != null && !current.isAfter(end)) {
                        dates.add(current.toString());
                    } else {
                        break;
                    }

                } while (it.hasNext());
            }
        } else {
            dates.add(date.toString());
        }
        return dates;
    }

    public boolean hasAnyOccurrenceBetween(String start, String end) throws ParseException {
        Interval lookingAtInterval = new Interval(Instant.parse(start), Instant.parse(end));
        Instant start_date = Instant.parse(getStartDate());
        if (rrule == null) {
            Interval interval = new Interval(start_date, Instant.parse(getEndDate()));
            return interval.abuts(lookingAtInterval) || interval.overlaps(lookingAtInterval);
        } else {
            LocalDateIterator it = LocalDateIteratorFactory.createLocalDateIterator(rrule, new LocalDate(start_date), false);
            it.advanceTo(lookingAtInterval.getStart().toLocalDate());
            if (it.hasNext()) {
                for (LocalDate current = it.next(); !current.isAfter(lookingAtInterval.getEnd().toLocalDate()); current = it.next()) {
                    if (lookingAtInterval.abuts(current.toInterval()) || lookingAtInterval.contains(current.toInterval())) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
