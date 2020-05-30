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

import org.devmaster.elasticsearch.index.mapper.Recurring;
import org.apache.lucene.index.LeafReaderContext;
import org.elasticsearch.search.lookup.SearchLookup;
import org.joda.time.LocalDate;

import java.text.ParseException;
import java.util.Map;

public class NextOccurrenceSearchScript extends AbstractRecurringSearchScript {

    public static final String SCRIPT_NAME = "nextOccurrence";

    private static final String PARAM_FIELD = "field";
    private static final String PARAM_FROM = "from";

    public NextOccurrenceSearchScript(Map<String, Object> params, SearchLookup lookup, LeafReaderContext leafContext) {
        super(params, lookup, leafContext);
    }

    @Override
    public Object run() {
        Recurring recurring = getRecurring(getParamValueFor(PARAM_FIELD));
        if (recurring != null) {
            String fromParam = getParamValueFor(PARAM_FROM);
            LocalDate date = fromParam != null ? new LocalDate(fromParam) : LocalDate.now();
            try {
                LocalDate nextOccurrence = recurring.getNextOccurrence(date);
                return nextOccurrence != null ? nextOccurrence.toString() : null;
            } catch (ParseException e) {
                throw new IllegalArgumentException("Error while obtaining has occurrences Next. Error: " + e.getMessage());
            }
        }
        return null;
    }

    @Override
    public double runAsDouble() {
        return 0;
    }
}
