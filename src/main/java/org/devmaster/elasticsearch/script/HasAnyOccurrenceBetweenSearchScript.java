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
import org.elasticsearch.script.ScriptException;
import org.joda.time.LocalDate;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Map;

public class HasAnyOccurrenceBetweenSearchScript extends AbstractRecurringSearchScript {

    public static final String SCRIPT_NAME = "hasAnyOccurrenceBetween";

    private static final String PARAM_FIELD = "field";
    private static final String PARAM_START = "start";
    private static final String PARAM_END = "end";

    protected HasAnyOccurrenceBetweenSearchScript(Map<String, String> paramMap) {
        super(paramMap);
    }

    public static class Factory extends AbstractFactory<HasAnyOccurrenceBetweenSearchScript> {

        public Factory() {
            super(HasAnyOccurrenceBetweenSearchScript.class, Arrays.asList(PARAM_FIELD, PARAM_START, PARAM_END));
        }

        @Override
        public String getName() {
            return SCRIPT_NAME;
        }
    }

    @Override
    public Object run() {
        Recurring recurring = getRecurring(getParamValueFor(PARAM_FIELD));
        String startDate = getParamValueFor(PARAM_START);
        String endDate = getParamValueFor(PARAM_END);
        try {
            return recurring != null && recurring.hasAnyOccurrenceBetween(startDate, endDate);
        } catch (ParseException e) {
            throw newScriptException("Error while obtaining has any occurrence between.", e, SCRIPT_NAME);
        }
    }
}
