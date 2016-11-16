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
import org.devmaster.elasticsearch.script.HasOccurrencesAtSearchScript;
import org.devmaster.elasticsearch.script.IsOccurringSearchScript;
import org.devmaster.elasticsearch.script.NextOccurrenceSearchScript;
import org.devmaster.elasticsearch.script.OccurBetweenSearchScript;
import org.elasticsearch.indices.IndicesModule;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.script.ScriptModule;


public class RecurringPlugin extends Plugin {

    @Override
    public String name() {
        return "native-recurring-plugin";
    }

    @Override
    public String description() {
        return "Native script to allow recurring feature";
    }

    public void onModule(ScriptModule module) {
        module.registerScript(NextOccurrenceSearchScript.SCRIPT_NAME, NextOccurrenceSearchScript.Factory.class);
        module.registerScript(HasOccurrencesAtSearchScript.SCRIPT_NAME, HasOccurrencesAtSearchScript.Factory.class);
        module.registerScript(OccurBetweenSearchScript.SCRIPT_NAME, OccurBetweenSearchScript.Factory.class);
        module.registerScript(IsOccurringSearchScript.SCRIPT_NAME, IsOccurringSearchScript.Factory.class);
    }

    public void onModule(IndicesModule module) {
        module.registerMapper(RecurringFieldMapper.CONTENT_TYPE, new RecurringFieldMapper.TypeParser());
    }

}
