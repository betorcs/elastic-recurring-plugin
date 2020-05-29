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
import org.elasticsearch.search.lookup.SearchLookup;
import org.joda.time.LocalDate;

import java.text.ParseException;
import java.util.Map;

public class NextOccurrenceFieldScript extends AbstractFieldScript {

    public static final String NAME = "nextOccurrence";

    public NextOccurrenceFieldScript(Map<String, Object> params, SearchLookup lookup, LeafReaderContext leafContext) {
        super(params, lookup, leafContext);
    }

    @Override
    protected String getName() {
        return NAME;
    }

    @Override
    protected Object run() throws ParseException {
        LocalDate nextOccurrence = getRecurring().getNextOccurrence(LocalDate.now());
        return nextOccurrence != null ? nextOccurrence.toString("yyyy-MM-dd") : null;
    }

    public static class Factory extends AbstractFieldScript.Factory<NextOccurrenceFieldScript> {

        public Factory() {
            super(NextOccurrenceFieldScript.class);
        }
    }

}
