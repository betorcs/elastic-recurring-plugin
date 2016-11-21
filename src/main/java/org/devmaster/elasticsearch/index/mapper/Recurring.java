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

import java.text.ParseException;

import static com.google.common.base.Strings.emptyToNull;

public final class Recurring {

    private String startDate;
    private String endDate;
    private String rrule;

    public Recurring() {
    }

    public Recurring(String startDate, String endDate, String rrule) {
        setStartDate(startDate);
        setEndDate(endDate);
        setRrule(rrule);
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        if (Strings.isNullOrEmpty(startDate))
            throw new IllegalArgumentException("Parameter startDate can not be null or empty");

        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = emptyToNull(endDate);
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
        LocalDate startDate = new LocalDate(this.startDate);
        LocalDate endDate = this.endDate != null ? new LocalDate(this.endDate) : null;

        if (rrule != null) {

            LocalDateIterator it = LocalDateIteratorFactory.createLocalDateIterator(rrule, startDate, false);
            it.advanceTo(start);

            if (it.hasNext()) {
                LocalDate nextOccurrence = it.next();
                return nextOccurrence != null && !nextOccurrence.isBefore(start) && !nextOccurrence.isAfter(end);
            } else {
                return false;
            }

        } else if (endDate != null) {

            return !start.isBefore(startDate) && !end.isAfter(endDate);

        }

        return !startDate.isBefore(start) && !startDate.isAfter(end);
    }

    public LocalDate getNextOccurrence() throws ParseException {

        final LocalDate today = LocalDate.now();

        if (this.rrule != null) {

            LocalDate start = new LocalDate(this.startDate).minusDays(1);
            LocalDateIterator it = LocalDateIteratorFactory.createLocalDateIterator(rrule, start, false);
            it.advanceTo(today);

            return it.hasNext() ? it.next() : null;

        } else if (this.endDate != null) {
            LocalDate start = new LocalDate(this.startDate);
            LocalDate end = new LocalDate(this.endDate);

            if (!today.isAfter(start)) {
                return start;
            } else if (!today.isAfter(end)) {
                return end;
            } else {
                return null;
            }
        }

        LocalDate date = new LocalDate(this.startDate);
        return !date.isBefore(today) ? date : null;
    }

    public boolean notHasExpired() throws ParseException {
        final LocalDate today = LocalDate.now();
        LocalDate nextOccurrence = getNextOccurrence();
        return nextOccurrence != null && (today.isBefore(nextOccurrence) || today.isEqual(nextOccurrence));
    }
}
