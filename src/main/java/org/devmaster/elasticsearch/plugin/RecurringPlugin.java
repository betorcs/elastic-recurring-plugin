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
import org.devmaster.elasticsearch.script.HasAnyOccurrenceBetweenSearchScript;
import org.devmaster.elasticsearch.script.HasOccurrencesAtSearchScript;
import org.devmaster.elasticsearch.script.NextOccurrenceSearchScript;
import org.devmaster.elasticsearch.script.NotHasExpiredSearchScript;
import org.devmaster.elasticsearch.script.OccurBetweenSearchScript;
import org.devmaster.elasticsearch.script.OccurrencesBetweenSearchScript;

import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.MapperPlugin;
import org.elasticsearch.plugins.IngestPlugin;
import org.elasticsearch.ingest.Processor;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class RecurringPlugin extends Plugin implements MapperPlugin, IngestPlugin  {
    
    @Override
    public Map<String, Mapper.TypeParser> getMappers() {
        Map<String, Mapper.TypeParser> mappers = new LinkedHashMap<String, Mapper.TypeParser>();
        mappers.put(RecurringFieldMapper.CONTENT_TYPE, new RecurringFieldMapper.TypeParser());
        return Collections.unmodifiableMap(mappers);
    }
    
    @Override
    public Map<String, Processor.Factory> getProcessors(Processor.Parameters parameters) {
        Map<String, Processor.Factory> mappers = new LinkedHashMap<String, Processor.Factory>();
        mappers.put(NextOccurrenceSearchScript.SCRIPT_NAME, (Processor.Factory) new NextOccurrenceSearchScript.Factory());
        mappers.put(HasOccurrencesAtSearchScript.SCRIPT_NAME, (Processor.Factory) new HasOccurrencesAtSearchScript.Factory());
        mappers.put(OccurBetweenSearchScript.SCRIPT_NAME, (Processor.Factory) new OccurBetweenSearchScript.Factory());
        mappers.put(NotHasExpiredSearchScript.SCRIPT_NAME, (Processor.Factory) new NotHasExpiredSearchScript.Factory());
        mappers.put(OccurrencesBetweenSearchScript.SCRIPT_NAME, (Processor.Factory) new OccurrencesBetweenSearchScript.Factory());
        mappers.put(HasAnyOccurrenceBetweenSearchScript.SCRIPT_NAME, (Processor.Factory) new HasAnyOccurrenceBetweenSearchScript.Factory());
        return Collections.unmodifiableMap(mappers);
    }
    
}
