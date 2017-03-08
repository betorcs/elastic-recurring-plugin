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

import java.text.ParseException;
import java.util.Arrays;
import java.util.Map;

public class NotHasExpiredSearchScript extends AbstractRecurringSearchScript {

    public static final String SCRIPT_NAME = "notHasExpired";
    private static final String PARAM_FIELD = "field";

    public static class Factory extends AbstractRecurringSearchScript.AbstractFactory<NotHasExpiredSearchScript> {
        public Factory() {
            super(NotHasExpiredSearchScript.class, Arrays.asList(PARAM_FIELD));
        }

        @Override
        public String getName() {
            return SCRIPT_NAME;
        }
    }

    public NotHasExpiredSearchScript(Map<String, String> paramMap) {
        super(paramMap);
    }

    @Override
    public Object run() {
        Recurring recurring = getRecurring(getParamValueFor(PARAM_FIELD));
        try {
            return recurring != null && recurring.notHasExpired();
        } catch (ParseException ignored) {}
        return false;
    }
}
