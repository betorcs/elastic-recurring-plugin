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
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.xcontent.support.XContentMapValues;
import org.elasticsearch.script.AbstractSearchScript;
import org.elasticsearch.script.NativeScriptFactory;
import org.elasticsearch.script.ScriptException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class AbstractRecurringSearchScript extends AbstractSearchScript {

    static abstract class AbstractFactory<T extends AbstractRecurringSearchScript> implements NativeScriptFactory {

        private final Map<String, Boolean> wantedFields;
        private final Class<T> cls;

        private static Map<String, Boolean> buildMap(List<String> items) {
            Map<String, Boolean> map = new HashMap<>();
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
        public T newScript(@Nullable Map<String, Object> params) {
            Map<String, String> paramMap = new HashMap<>();
            for (Map.Entry<String, Boolean> paramEntry : wantedFields.entrySet()) {
                String paramValue = params == null ? null : XContentMapValues.nodeStringValue(params.get(paramEntry.getKey()), null);
                if (paramEntry.getValue() && paramValue == null) {
                    throw new ScriptException("Missing the [" + paramEntry + "] parameter");
                }
                paramMap.put(paramEntry.getKey(), paramValue);
            }

            try {
                return cls.getDeclaredConstructor(Map.class).newInstance(paramMap);
            } catch (Exception e) {
                throw new ScriptException("Error while loading script class: " + cls.getName());
            }
        }

        @Override
        public boolean needsScores() {
            return false;
        }
    }

    private final Map<String, String> paramMap;

    AbstractRecurringSearchScript(Map<String, String> paramMap) {
        this.paramMap = paramMap;
    }

    protected Recurring getRecurring(String fieldName) {
        if (source().containsKey(fieldName)) {
            Map<String, Object> map = (Map<String, Object>) source().get(fieldName);

            String rrule = (String) map.get(RecurringFieldMapper.Names.RRULE);
            String startDate = (String) map.get(RecurringFieldMapper.Names.START_DATE);
            String endDate = (String) map.get(RecurringFieldMapper.Names.END_DATE);
            return new Recurring(startDate, endDate, rrule);
        }
        return null;
    }

    protected String getParamValueFor(String paramName) {
        return paramMap.get(paramName);
    }

}
