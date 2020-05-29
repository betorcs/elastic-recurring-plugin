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

package org.devmaster.elasticsearch.plugin;


import org.devmaster.elasticsearch.index.mapper.RecurringFieldMapper;
import org.devmaster.elasticsearch.script.HasAnyOccurrenceBetweenFilterScript;
import org.devmaster.elasticsearch.script.HasOccurrencesAtFilterScript;
import org.devmaster.elasticsearch.script.NextOccurrenceFieldScript;
import org.devmaster.elasticsearch.script.NotHasExpiredFilterScript;
import org.devmaster.elasticsearch.script.OccurBetweenFilterScript;
import org.devmaster.elasticsearch.script.OccurrencesBetweenFieldScript;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.plugins.MapperPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.ScriptPlugin;
import org.elasticsearch.script.FieldScript;
import org.elasticsearch.script.FilterScript;
import org.elasticsearch.script.ScriptContext;
import org.elasticsearch.script.ScriptEngine;
import org.elasticsearch.script.ScriptFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RecurringPlugin extends Plugin implements MapperPlugin, ScriptPlugin {

    @Override
    public Map<String, Mapper.TypeParser> getMappers() {
        return Collections.singletonMap(RecurringFieldMapper.CONTENT_TYPE, new RecurringFieldMapper.TypeParser());
    }

    @Override
    public ScriptEngine getScriptEngine(Settings settings, Collection<ScriptContext<?>> contexts) {
        return new RecurringScriptEngine();
    }


    private static class RecurringScriptEngine implements ScriptEngine {

        @Override
        public String getType() {
            return "recurring_scripts";
        }

        @Override
        public <T> T compile(String name, String code, ScriptContext<T> context, Map<String, String> params) {
            if (!context.equals(FilterScript.CONTEXT) && !context.equals(FieldScript.CONTEXT)) {
                throw new IllegalArgumentException(getType()
                        + " scripts cannot be used for context ["
                        + context.name + "]");
            }

            ScriptFactory factory;
            if (HasAnyOccurrenceBetweenFilterScript.NAME.equals(code)) {
                factory = new HasAnyOccurrenceBetweenFilterScript.Factory();
            } else if (HasOccurrencesAtFilterScript.NAME.equals(code)) {
                factory = new HasOccurrencesAtFilterScript.Factory();
            } else if (OccurBetweenFilterScript.NAME.equals(code)) {
                factory = new OccurBetweenFilterScript.Factory();
            } else if (NotHasExpiredFilterScript.NAME.equals(code)) {
                factory = new NotHasExpiredFilterScript.Factory();
            } else if (NextOccurrenceFieldScript.NAME.equals(code)) {
                factory = new NextOccurrenceFieldScript.Factory();
            } else if (OccurrencesBetweenFieldScript.NAME.equals(code)) {
                factory = new OccurrencesBetweenFieldScript.Factory();
            } else throw new IllegalArgumentException("Not implemented");

            return context.factoryClazz.cast(factory);
        }

        @Override
        public Set<ScriptContext<?>> getSupportedContexts() {
            Set<ScriptContext<?>> contexts = new HashSet<>();
            contexts.add(FilterScript.CONTEXT);
            contexts.add(FieldScript.CONTEXT);
            return contexts;
        }
    }

}
