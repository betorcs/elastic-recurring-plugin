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
import org.devmaster.elasticsearch.index.mapper.RecurringFieldMapper;
import org.apache.lucene.index.LeafReaderContext;
import org.elasticsearch.search.lookup.SearchLookup;
import org.elasticsearch.common.xcontent.support.XContentMapValues;
import org.elasticsearch.script.SearchScript;
import org.elasticsearch.script.ScriptEngine;
import org.elasticsearch.script.ScriptContext;
import org.elasticsearch.script.ScoreScript;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class AbstractRecurringSearchScript extends SearchScript {

    private final Map<String, Object> paramMap;

    public AbstractRecurringSearchScript(Map<String, Object> params, SearchLookup lookup, LeafReaderContext leafContext) {
        super(params, lookup, leafContext);
        this.paramMap = params;
    }
    
    @SuppressWarnings("unchecked")
    protected Recurring getRecurring(String fieldName) {
        if (paramMap.containsKey(fieldName)) {
            Map<String, Object> map = (Map<String, Object>) paramMap.get(fieldName);

            String rrule = (String) map.get(RecurringFieldMapper.FieldNames.RRULE);
            String startDate = (String) map.get(RecurringFieldMapper.FieldNames.START_DATE);
            String endDate = (String) map.get(RecurringFieldMapper.FieldNames.END_DATE);
            
            return new Recurring(startDate, endDate, rrule);
        }
        return null;
    }
    
    protected String getParamValueFor(String paramName) {
        return (String) paramMap.get(paramName);
    }
    
    static abstract class AbstractFactory<T extends AbstractRecurringSearchScript> implements ScriptEngine {

        private final Map<String, Boolean> wantedFields;
        private final Class<T> cls;

        private static Map<String, Boolean> buildMap(List<String> items) {
            Map<String, Boolean> map = new HashMap<String, Boolean>();
            for (String item : items) {
                map.put(item, true);
            }
            return map;
        }

        AbstractFactory(Class<T> cls, List<String> wantedFields) {
            this(cls, buildMap(wantedFields));
        }

        AbstractFactory(Class<T> cls, Map<String, Boolean> wantedFields) {
            this.wantedFields = wantedFields;
            this.cls = cls;
        }
        
        @Override
        public <T> T compile(String scriptName, String scriptSource, ScriptContext<T> context, Map<String, String> params) {
            if (context.equals(ScoreScript.CONTEXT) == false) {
                throw new IllegalArgumentException(getType() + " scripts cannot be used for context ["+ context.name +"]");
            }
            
            Map<String, String> paramMap = new HashMap<String, String>();
            for (Map.Entry<String, Boolean> paramEntry : wantedFields.entrySet()) {
                String paramValue = params == null ? null : XContentMapValues.nodeStringValue(params.get(paramEntry.getKey()), null);
                if (paramEntry.getValue() && paramValue == null) {
                    throw new IllegalArgumentException("Missing the [" + paramEntry + "] parameter");
                }
                paramMap.put(paramEntry.getKey(), paramValue);
            }

            try {
                return (T)cls.getDeclaredConstructor(Map.class).newInstance(paramMap);
            } catch (Exception e) {
                throw new IllegalArgumentException("Error while loading script class: " + cls.getName());
            }
        }

        @Override
        public void close() {
            // optionally close resources
        }
        
    }
}
