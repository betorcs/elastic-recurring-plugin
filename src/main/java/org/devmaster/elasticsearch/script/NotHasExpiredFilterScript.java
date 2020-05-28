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

import java.text.ParseException;
import java.util.Map;

public class NotHasExpiredFilterScript extends AbstractFilterScript {

    public static final String NAME = "notHasExpired";

    public NotHasExpiredFilterScript(Map<String, Object> params, SearchLookup lookup, LeafReaderContext leafContext) {
        super(params, lookup, leafContext);
    }

    @Override
    protected String getName() {
        return NAME;
    }

    @Override
    protected boolean doFilter() throws ParseException {
        return getRecurring().notHasExpired();
    }

    public static class Factory extends AbstractFilterScript.Factory<NotHasExpiredFilterScript> {
        public Factory() {
            super(NotHasExpiredFilterScript.class);
        }
    }
}
