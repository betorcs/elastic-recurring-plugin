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

import java.io.IOException;
import org.elasticsearch.common.SuppressForbidden;
import org.elasticsearch.cli.Terminal;
import org.elasticsearch.common.logging.LogConfigurator;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.node.MockNode;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.InternalSettingsPreparer;
import org.elasticsearch.plugins.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class to easily run the the plugin from a IDE.
 */
public class ElasticsearchWithPluginLauncher {

    @SuppressForbidden(reason = "not really code or a test")
    public static void main(String[] args) throws Throwable {
        System.setProperty("es.logger.prefix", "");
        Settings settings = Settings.builder()
                .put("security.manager.enabled", "false")
                .put("plugins.load_classpath_plugins", "false")
                .put("path.home", System.getProperty("es.path.home", System.getProperty("user.dir")))
                .build();

        // Setup logging using config/logging.yml
        //Environment environment = InternalSettingsPreparer.prepareEnvironment(settings, Terminal.DEFAULT);
        //Class.forName("org.apache.log4j.Logger");
        //LogConfigurator.configure(environment);

        final CountDownLatch latch = new CountDownLatch(1);

        Collection<Class<? extends Plugin>> classes = new ArrayList<Class<? extends Plugin>>();
        classes.add(RecurringPlugin.class);
        final Node node = new MockNode(settings, classes);
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                try {
                    node.close();
                } catch (IOException ex) {
                    Logger.getLogger(ElasticsearchWithPluginLauncher.class.getName()).log(Level.SEVERE, null, ex);
                }
                latch.countDown();
            }
            
        });
        
        node.start();
        latch.await();
    }
}
