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

package org.devmaster.elasticsearch;

import com.google.ical.compat.jodatime.LocalDateIterator;
import com.google.ical.compat.jodatime.LocalDateIteratorFactory;
import org.elasticsearch.common.Strings;
import org.joda.time.Instant;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Strings.emptyToNull;

public final class Recurring {

    private LocalDate start;
    private LocalDate end;
    private String rrule;

    public Recurring() {
    }

    public Recurring(String start, String end, String rrule) {
        setStartDate(start);
        setEndDate(end);
        setRrule(rrule);
    }

    public Recurring(LocalDate start, LocalDate end, String rrule) {
        setStart(start);
        setEnd(end);
        setRrule(rrule);
    }

    public LocalDate getStart() {
        return start;
    }

    public void setStart(LocalDate start) {
        if (start == null)
            throw new IllegalArgumentException("Parameter startDate can not be null or empty");

        this.start = start;
    }

    public void setStartDate(String startDate) {
        if (Strings.isNullOrEmpty(startDate))
            throw new IllegalArgumentException("Parameter startDate can not be null or empty");

        this.start = LocalDate.parse(startDate);
    }

    public LocalDate getEnd() {
        return end;
    }

    public void setEnd(LocalDate end) {
        this.end = end;
    }

    public void setEndDate(String endDate) {
        this.end = endDate != null ? LocalDate.parse(endDate) : null;
    }

    public String getRrule() {
        return rrule;
    }

    public void setRrule(String rrule) {
        this.rrule = emptyToNull(rrule);
    }

    public boolean hasOccurrencesAt(final LocalDate date) throws ParseException {
        if (this.rrule != null) {
            LocalDate end = date.plusDays(1);
            LocalDateIterator it = LocalDateIteratorFactory.createLocalDateIterator(rrule, new LocalDate(this.start), false);
            it.advanceTo(date);
            return it.hasNext() && it.next().isBefore(end);
        } else if (this.end != null) {
            LocalDate start = new LocalDate(this.start);
            LocalDate end = new LocalDate(this.end);
            return !date.isBefore(start) && !date.isAfter(end);
        } else {
            return new LocalDate(this.start).isEqual(date);
        }
    }

    public boolean occurBetween(String start, String end) throws ParseException {
        return occurBetween(new LocalDate(start), new LocalDate(end));
    }

    public boolean occurBetween(final LocalDate start, final LocalDate end) throws ParseException {
        LocalDate startDate = new LocalDate(this.start);
        LocalDate endDate = this.end != null ? new LocalDate(this.end) : null;

        if (rrule != null) {

            LocalDateIterator it = LocalDateIteratorFactory.createLocalDateIterator(rrule, startDate, false);
            it.advanceTo(start);

            if (it.hasNext()) {
                LocalDate nextOccurrence = it.next();
                return nextOccurrence != null && !nextOccurrence.isBefore(start) && !nextOccurrence.isAfter(end);
            } else {
                return false;
            }

        } else if (endDate != null && endDate.isAfter(startDate)) {

            return isBetween(startDate, start, end)
                    || isBetween(endDate, start, end);

        }

        return !startDate.isBefore(start) && !startDate.isAfter(end);
    }

    private boolean isBetween(LocalDate date, LocalDate start, LocalDate end) {
        return date.compareTo(start) > -1 && date.compareTo(end) < 1;
    }

    public LocalDate getNextOccurrence(LocalDate date) throws ParseException {

        final LocalDate start = new LocalDate(this.start);

        if (this.rrule != null) {
            LocalDateIterator it = LocalDateIteratorFactory.createLocalDateIterator(rrule, start.minusDays(1), false);
            it.advanceTo(date);
            return it.hasNext() ? it.next() : null;
        } else if (this.end == null) {
            return !date.isAfter(start) ? start : null;
        } else {
            LocalDate end = new LocalDate(this.end);
            return !date.isAfter(end) ? start : null;
        }
    }

    public boolean notHasExpired() throws ParseException {
        final LocalDate today = LocalDate.now();
        return getNextOccurrence(today) != null;
    }

    public List<String> occurrencesBetween(LocalDate start, LocalDate end) throws ParseException {
        final LocalDate date = new LocalDate(this.start);
        List<String> dates = new ArrayList<>();
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
        if (rrule == null) {
            Interval interval = new Interval(this.start.toDateTime(LocalTime.MIDNIGHT), this.end.toDateTime(LocalTime.MIDNIGHT));
            return interval.abuts(lookingAtInterval) || interval.overlaps(lookingAtInterval);
        } else {
            LocalDateIterator it = LocalDateIteratorFactory
                    .createLocalDateIterator(rrule, this.start, false);
            it.advanceTo(lookingAtInterval.getStart().toLocalDate());
            if (it.hasNext()) {
                for (LocalDate current = it.next(); it.hasNext()
                        && !current.isAfter(lookingAtInterval.getEnd().toLocalDate()); current = it.next()) {

                    if (lookingAtInterval.abuts(current.toInterval())
                            || lookingAtInterval.contains(current.toInterval())) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
