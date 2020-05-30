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
import org.devmaster.elasticsearch.index.mapper.Recurring;
import org.devmaster.elasticsearch.index.mapper.RecurringFieldMapper;
import org.elasticsearch.script.SearchScript;
import org.elasticsearch.search.lookup.SearchLookup;

import java.util.Map;

abstract class AbstractRecurringSearchScript extends SearchScript {

	private final Map<String, Object> params;
	
	AbstractRecurringSearchScript(Map<String, Object> params, SearchLookup lookup, LeafReaderContext leafContext) {
		super(params, lookup, leafContext);
		this.params = params;
	}
	
    @SuppressWarnings("unchecked")
	protected Recurring getRecurring(String fieldName) {
        if (params.containsKey(fieldName)) {
            Map<String, Object> map = (Map<String, Object>) params.get(fieldName);

            String rrule = (String) map.get(RecurringFieldMapper.FieldNames.RRULE);
            String startDate = (String) map.get(RecurringFieldMapper.FieldNames.START_DATE);
            String endDate = (String) map.get(RecurringFieldMapper.FieldNames.END_DATE);
            
            return new Recurring(startDate, endDate, rrule);
        }
        return null;
    }

    protected String getParamValueFor(String paramName) {
        return (String) params.get(paramName);
    }
	
}
