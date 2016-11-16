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
import org.joda.time.LocalDate;

import java.text.ParseException;

public final class Recurring {

    private String startDate;
    private String endDate;
    private String rrule;

    public Recurring() {
    }

    public Recurring(String startDate, String endDate, String rrule) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.rrule = rrule;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String dateStart) {
        this.startDate = dateStart;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getRrule() {
        return rrule;
    }

    public void setRrule(String rrule) {
        this.rrule = rrule;
    }

    public final String dtstart() {
        return startDate;
    }

    public final String dtend() {
        return endDate;
    }

    public final String rrule() {
        return rrule;
    }

    public boolean hasOccurrencesAt(LocalDate date) throws ParseException {
        LocalDate start = date.minusDays(1);
        LocalDate end = date.plusDays(1);
        LocalDateIterator it = LocalDateIteratorFactory.createLocalDateIterator(rrule, start, false);
        it.advanceTo(date);
        return it.hasNext() && it.next().isBefore(end);
    }

    public boolean occurBetween(LocalDate start, LocalDate end) throws ParseException {
        LocalDateIterator it = LocalDateIteratorFactory.createLocalDateIterator(rrule, new LocalDate(dtstart()), false);
        it.advanceTo(start);
        if (it.hasNext()) {
            LocalDate nextOccurrence = it.next();
            return nextOccurrence != null && !nextOccurrence.isBefore(start) && !nextOccurrence.isAfter(end);
        }
        return false;
    }

    public LocalDate getNextOccurrence() throws ParseException {
        LocalDate start = new LocalDate(dtstart());
        LocalDateIterator it = LocalDateIteratorFactory.createLocalDateIterator(rrule, start, false);
        it.advanceTo(LocalDate.now());
        return it.hasNext() ? it.next() : null;
    }

    public boolean isOccurring() throws ParseException {
        LocalDate nextOccurrence = getNextOccurrence();
        LocalDate today = LocalDate.now();
        return nextOccurrence != null && (today.isBefore(nextOccurrence) || today.isEqual(nextOccurrence));
    }
}
