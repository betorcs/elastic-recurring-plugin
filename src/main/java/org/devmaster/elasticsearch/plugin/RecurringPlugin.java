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
import org.elasticsearch.plugins.ScriptPlugin;

import org.apache.lucene.index.LeafReaderContext;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.script.SearchScript;
import org.elasticsearch.script.ScriptContext;
import org.elasticsearch.script.ScriptEngine;
import org.elasticsearch.search.lookup.SearchLookup;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class RecurringPlugin extends Plugin implements MapperPlugin, ScriptPlugin {

    @Override
    public Map<String, Mapper.TypeParser> getMappers() {
        return Collections.singletonMap(RecurringFieldMapper.CONTENT_TYPE, new RecurringFieldMapper.TypeParser());
    }
    
    @Override
    public ScriptEngine getScriptEngine(Settings settings, Collection<ScriptContext<?>> contexts) {
        return new RecurringEngine();
    }

    private static class RecurringEngine implements ScriptEngine {

        @Override
        public String getType() {
            return "rec";
        }

        @Override
        public <T> T compile(String scriptName, String scriptSource, ScriptContext<T> context, Map<String, String> params) {
            if (context.equals(SearchScript.CONTEXT) == false) {
                throw new IllegalArgumentException(getType() + " scripts cannot be used for context [" + context.name + "]");
            }
            // we use the script "source" as the script identifier
            if ("hasAnyOccurrenceBetween".equals(scriptSource)) {
            	SearchScript.Factory factory = hasAnyOccurrenceBetween::new;
                return context.factoryClazz.cast(factory);
            }
            if ("hasOccurrencesAt".equals(scriptSource)) {
            	SearchScript.Factory factory = HasOccurrencesAt::new;
                return context.factoryClazz.cast(factory);
            }
            if ("nextOccurrence".equals(scriptSource)) {
            	SearchScript.Factory factory = nextOccurrence::new;
                return context.factoryClazz.cast(factory);
            }
            if ("notHasExpired".equals(scriptSource)) {
            	SearchScript.Factory factory = notHasExpired::new;
                return context.factoryClazz.cast(factory);
            }
            if ("occurBetween".equals(scriptSource)) {
            	SearchScript.Factory factory = occurBetween::new;
                return context.factoryClazz.cast(factory);
            }
            if ("occurrencesBetween".equals(scriptSource)) {
            	SearchScript.Factory factory = occurrencesBetween::new;
                return context.factoryClazz.cast(factory);
            }
            
            throw new IllegalArgumentException("Unknown script name " + scriptSource);
        }

        @Override
        public void close() {
            // optionally close resources
        }

        private static class hasAnyOccurrenceBetween implements SearchScript.LeafFactory {
        	
            private final Map<String, Object> params;
            private final SearchLookup lookup;
            
            private hasAnyOccurrenceBetween(Map<String, Object> params, SearchLookup lookup) {
                if (params.containsKey("field") == false) {
                    throw new IllegalArgumentException("Missing parameter [field]");
                }
                if (params.containsKey("start") == false) {
                    throw new IllegalArgumentException("Missing parameter [start]");
                }
                if (params.containsKey("end") == false) {
                    throw new IllegalArgumentException("Missing parameter [end]");
                }
                this.params = params;
                this.lookup = lookup;
            }

            @Override
            public boolean needs_score() {
                return false;  // Return true if the script needs the score
            }

            @Override
            public SearchScript newInstance(LeafReaderContext context)throws IOException {
                return new HasAnyOccurrenceBetweenSearchScript(params, lookup, context);
            }
        }
        
    	private static class HasOccurrencesAt implements SearchScript.LeafFactory {
        	
            private final Map<String, Object> params;
            private final SearchLookup lookup;
            
            private HasOccurrencesAt(Map<String, Object> params, SearchLookup lookup) {
                if (params.containsKey("field") == false) {
                    throw new IllegalArgumentException("Missing parameter [field]");
                }
                if (params.containsKey("date") == false) {
                    throw new IllegalArgumentException("Missing parameter [date]");
                }
                this.params = params;
                this.lookup = lookup;
            }

            @Override
            public boolean needs_score() {
                return false;  // Return true if the script needs the score
            }

            @Override
            public SearchScript newInstance(LeafReaderContext context)throws IOException {
                return new HasOccurrencesAtSearchScript(params, lookup, context);
            }
        }
    	
    	private static class nextOccurrence implements SearchScript.LeafFactory {
        	
            private final Map<String, Object> params;
            private final SearchLookup lookup;
            
            private nextOccurrence(Map<String, Object> params, SearchLookup lookup) {
                if (params.containsKey("field") == false) {
                    throw new IllegalArgumentException("Missing parameter [field]");
                }
                this.params = params;
                this.lookup = lookup;
            }
            
            @Override
            public boolean needs_score() {
                return false;  // Return true if the script needs the score
            }

            @Override
            public SearchScript newInstance(LeafReaderContext context)throws IOException {
                return new NextOccurrenceSearchScript(params, lookup, context);
            }
        }
		
		private static class notHasExpired implements SearchScript.LeafFactory {
			
		    private final Map<String, Object> params;
		    private final SearchLookup lookup;
		    
		    private notHasExpired(Map<String, Object> params, SearchLookup lookup) {
		        if (params.containsKey("field") == false) {
		            throw new IllegalArgumentException("Missing parameter [field]");
		        }
		        this.params = params;
		        this.lookup = lookup;
		    }
		
		    @Override
		    public boolean needs_score() {
		        return false;  // Return true if the script needs the score
		    }
		
		    @Override
		    public SearchScript newInstance(LeafReaderContext context)throws IOException {
		        return new NotHasExpiredSearchScript(params, lookup, context);
		    }
		}
		
		private static class occurBetween implements SearchScript.LeafFactory {
        	
            private final Map<String, Object> params;
            private final SearchLookup lookup;
            
            private occurBetween(Map<String, Object> params, SearchLookup lookup) {
                if (params.containsKey("field") == false) {
                    throw new IllegalArgumentException("Missing parameter [field]");
                }
                if (params.containsKey("start") == false) {
                    throw new IllegalArgumentException("Missing parameter [start]");
                }
                if (params.containsKey("end") == false) {
                    throw new IllegalArgumentException("Missing parameter [end]");
                }
                this.params = params;
                this.lookup = lookup;
            }

            @Override
            public boolean needs_score() {
                return false;  // Return true if the script needs the score
            }

            @Override
            public SearchScript newInstance(LeafReaderContext context)throws IOException {
                return new OccurBetweenSearchScript(params, lookup, context);
            }
        }

		private static class occurrencesBetween implements SearchScript.LeafFactory {
			
		    private final Map<String, Object> params;
		    private final SearchLookup lookup;
		    
		    private occurrencesBetween(Map<String, Object> params, SearchLookup lookup) {
		        if (params.containsKey("field") == false) {
		            throw new IllegalArgumentException("Missing parameter [field]");
		        }
		        this.params = params;
		        this.lookup = lookup;
		    }
		
		    @Override
		    public boolean needs_score() {
		        return false;  // Return true if the script needs the score
		    }
		
		    @Override
		    public SearchScript newInstance(LeafReaderContext context)throws IOException {
		        return new OccurrencesBetweenSearchScript(params, lookup, context);
		    }
		}
		
    }
    
}
