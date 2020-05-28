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

package org.devmaster.elasticsearch.script;

import org.apache.lucene.index.LeafReaderContext;
import org.devmaster.elasticsearch.Recurring;
import org.devmaster.elasticsearch.index.mapper.RecurringFieldMapper;
import org.devmaster.elasticsearch.script.exceptions.FilterScriptCreationException;
import org.devmaster.elasticsearch.script.exceptions.FilterScriptRunException;
import org.elasticsearch.script.FilterScript;
import org.elasticsearch.script.JodaCompatibleZonedDateTime;
import org.elasticsearch.search.lookup.SearchLookup;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public abstract class AbstractFilterScript extends FilterScript {

    public AbstractFilterScript(Map<String, Object> params, SearchLookup lookup, LeafReaderContext leafContext) {
        super(params, lookup, leafContext);
    }

    @Override
    public final boolean execute() {
        try {
            return doFilter();
        } catch (ParseException e) {
            throw new FilterScriptRunException(getName(), e);
        }
    }

    protected abstract String getName();

    protected abstract boolean doFilter() throws ParseException;

    protected Recurring getRecurring() {
        String field = (String) getParams().get("field");
        String startDateFieldName = field + "." + RecurringFieldMapper.FieldNames.START_DATE;
        String endDateFieldName = field + "." + RecurringFieldMapper.FieldNames.END_DATE;
        String rruleFieldName = field + "." + RecurringFieldMapper.FieldNames.RRULE;

        String start = getDoc().containsKey(startDateFieldName)
                ? ((JodaCompatibleZonedDateTime)getDoc().get(startDateFieldName).get(0)).format(DateTimeFormatter.ISO_LOCAL_DATE)
                : null;
        String end = getDoc().containsKey(endDateFieldName) && !getDoc().get(endDateFieldName).isEmpty()
                ? ((JodaCompatibleZonedDateTime) getDoc().get(endDateFieldName).get(0)).format(DateTimeFormatter.ISO_LOCAL_DATE)
                : null;
        String rrule = getDoc().containsKey(rruleFieldName) && !getDoc().get(rruleFieldName).isEmpty()
                ? getDoc().get(rruleFieldName).get(0).toString()
                : null;

        return new Recurring(start, end, rrule);
    }

    public static class Factory<T extends  AbstractFilterScript> implements FilterScript.Factory {

        private final Class<T> cls;

        public Factory(Class<T> cls) {
            this.cls = cls;
        }

        @Override
        public LeafFactory<T> newFactory(Map<String, Object> params, SearchLookup lookup) {
            return new LeafFactory<>(params, lookup, cls);
        }
    }

    private static class LeafFactory<T extends  AbstractFilterScript> implements FilterScript.LeafFactory {

        private final Map<String, Object> params;
        private final SearchLookup lookup;
        private final Class<T> cls;

        private LeafFactory(Map<String, Object> params, SearchLookup lookup, Class<T> cls) {
            this.params = params;
            this.lookup = lookup;
            this.cls = cls;
        }

        @Override
        public FilterScript newInstance(LeafReaderContext leafContext) throws IOException {
            try {
                return (FilterScript) cls.getConstructors()[0].newInstance(params, lookup, leafContext);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new FilterScriptCreationException(cls, e);
            }
        }

    }

}
