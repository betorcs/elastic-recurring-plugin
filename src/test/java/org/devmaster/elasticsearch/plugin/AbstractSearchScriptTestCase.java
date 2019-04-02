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

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.test.ESIntegTestCase;
import org.elasticsearch.test.ESIntegTestCase.Scope;
import org.elasticsearch.test.ESIntegTestCase.ClusterScope;

import java.util.Collection;
import java.util.Collections;

import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.test.junit.annotations.TestLogging;
import org.junit.runner.RunWith;

@TestLogging("_root:DEBUG")
@ClusterScope(scope = Scope.SUITE, numDataNodes = 1)
@RunWith(com.carrotsearch.randomizedtesting.RandomizedRunner.class)
public abstract class AbstractSearchScriptTestCase extends ESIntegTestCase {
	
	@Override
	protected Settings nodeSettings(int nodeOrdinal) {
		return Settings.builder().put(super.nodeSettings(nodeOrdinal)).build();
	}
	
	@Override
    public Settings indexSettings() {
        return Settings.builder()
	        .put(IndexMetaData.SETTING_NUMBER_OF_SHARDS, 1)
	        .put(IndexMetaData.SETTING_NUMBER_OF_REPLICAS, 0)
	        .build();
    }
    
    @Override
    protected Collection<Class<? extends Plugin>> nodePlugins() {
    	return Collections.singletonList(RecurringPlugin.class);
    }
}
