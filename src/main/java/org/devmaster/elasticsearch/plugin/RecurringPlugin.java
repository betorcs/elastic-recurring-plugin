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

import org.devmaster.elasticsearch.script.*;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.script.ScriptModule;

import java.util.ArrayList;
import java.util.Collection;


public class RecurringPlugin extends AbstractPlugin {

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
        module.registerScript(NotHasExpiredSearchScript.SCRIPT_NAME, NotHasExpiredSearchScript.Factory.class);
        module.registerScript(OccurrencesBetweenSearchScript.SCRIPT_NAME, OccurrencesBetweenSearchScript.Factory.class);
        module.registerScript(HasAnyOccurrenceBetweenSearchScript.SCRIPT_NAME, HasAnyOccurrenceBetweenSearchScript.Factory.class);
    }

    @Override
    public Collection<Class<? extends Module>> indexModules() {
        Collection<Class<? extends Module>> modules = new ArrayList<>();
        modules.add(RecurringIndexModule.class);
        return modules;
    }
}
