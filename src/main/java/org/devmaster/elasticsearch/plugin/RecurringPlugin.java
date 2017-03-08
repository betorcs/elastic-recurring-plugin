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
import org.devmaster.elasticsearch.script.*;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.plugins.MapperPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.ScriptPlugin;
import org.elasticsearch.script.NativeScriptFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class RecurringPlugin extends Plugin implements MapperPlugin, ScriptPlugin {

    @Override
    public Map<String, Mapper.TypeParser> getMappers() {
        return Collections.singletonMap(RecurringFieldMapper.CONTENT_TYPE, new RecurringFieldMapper.TypeParser());
    }

    @Override
    public List<NativeScriptFactory> getNativeScripts() {
        return Arrays.asList(new NextOccurrenceSearchScript.Factory(),
                new HasOccurrencesAtSearchScript.Factory(),
                new OccurBetweenSearchScript.Factory(),
                new NotHasExpiredSearchScript.Factory(),
                new OccurrencesBetweenSearchScript.Factory());
    }

}
