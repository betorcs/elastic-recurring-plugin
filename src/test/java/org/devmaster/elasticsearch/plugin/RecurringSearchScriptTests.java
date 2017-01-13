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

import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.scriptQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.*;

public class RecurringSearchScriptTests extends AbstractSearchScriptTestCase {

    public void testRecurringScript() throws Exception {

        String mapping = jsonBuilder().startObject().startObject("type")
                .startObject("properties")
                    .startObject("name")
                        .field("type", "string")
                        .startObject("fields")
                            .startObject("analyzed")
                                .field("type", "string")
                                .field("analyzer", "text")
                            .endObject()
                            .startObject("raw")
                                .field("type", "string")
                                .field("index", "not_analyzed")
                            .endObject()
                        .endObject()
                    .endObject()
                    .startObject("recurrent_date").field("type", "recurring").endObject()
                .endObject().endObject().endObject()
                .string();

        assertAcked(prepareCreate("test", 1, Settings.settingsBuilder()
                    .put("index.number_of_shards", 2)
                    .put("index.number_of_replicas", 1)
                    .put("analysis.analyzer.text.type", "brazilian"))
                .addMapping("type", mapping));

        List<IndexRequestBuilder> indexBuilders = new ArrayList<>();

        indexBuilders.add(client().prepareIndex("test", "type", "1")
            .setSource(createDoc("Festa de Natal", "2016-12-25", null, "RRULE:FREQ=YEARLY;BYMONTH=12;BYMONTHDAY=25")));

        indexBuilders.add(client().prepareIndex("test", "type", "2")
                .setSource(createDoc("Dias das Mães", "2015-05-10", null, "RRULE:FREQ=YEARLY;BYMONTH=5;BYDAY=2SU")));

        indexBuilders.add(client().prepareIndex("test", "type", "3")
                .setSource(createDoc("Halloween Party", "2012-10-31", null, "RRULE:FREQ=YEARLY;BYMONTH=10;BYMONTHDAY=31;WKST=SU")));

        indexBuilders.add(client().prepareIndex("test", "type", "4")
                .setSource(createDoc("Revisão Mensal Cruze", "2016-02-10", null, "RRULE:FREQ=MONTHLY;BYMONTHDAY=10;COUNT=5;WKST=SU")));

        indexRandom(true, indexBuilders);


        // Show next occurrences
        Map<String, Object> params = new HashMap<>();
        params.put("field", "recurrent_date");
        SearchResponse searchResponse = client().prepareSearch("test")
                .addScriptField("occur", new Script(
                        "nextOccurrence", ScriptService.ScriptType.INLINE, "native", params))
                .execute().actionGet();
        logger.info(searchResponse.toString());
        assertNoFailures(searchResponse);
        assertHitCount(searchResponse, indexBuilders.size());

        // Search natal
        searchResponse = client().prepareSearch("test")
                .setQuery(boolQuery().should(termQuery("name", "natal")))
                .execute().actionGet();
        logger.info("Looking for natal");
        logger.info(searchResponse.toString());
        assertNoFailures(searchResponse);
        assertHitCount(searchResponse, 1);

        // Show dias das maes
        params = new HashMap<>();
        params.put("field", "recurrent_date");
        params.put("date", "2025-05-11");
        searchResponse = client().prepareSearch("test")
                .setQuery(scriptQuery(new Script("hasOccurrencesAt", ScriptService.ScriptType.INLINE, "native", params)))
                .execute().actionGet();
        logger.info("Dia das maes");
        logger.info(searchResponse.toString());
        assertNoFailures(searchResponse);
        assertHitCount(searchResponse, 1);

        // Show halloween
        params = new HashMap<>();
        params.put("field", "recurrent_date");
        params.put("date", "2025-10-31");
        searchResponse = client().prepareSearch("test")
                .setQuery(scriptQuery(new Script("hasOccurrencesAt", ScriptService.ScriptType.INLINE, "native", params)))
                .execute().actionGet();
        logger.info("Halloween");
        logger.info(searchResponse.toString());
        assertNoFailures(searchResponse);
        assertHitCount(searchResponse, 1);


        // Show eventos em dezembro
        params = new HashMap<>();
        params.put("field", "recurrent_date");
        params.put("start", "2017-05-01");
        params.put("end", "2017-05-31");
        searchResponse = client().prepareSearch("test")
                .setQuery(scriptQuery(new Script("occurBetween", ScriptService.ScriptType.INLINE, "native", params)))
                .execute().actionGet();
        logger.info("Eventos em dezembro");
        logger.info(searchResponse.toString());
        assertNoFailures(searchResponse);
        assertHitCount(searchResponse, 1);


        // Show event that are occurring
        params = new HashMap<>();
        params.put("field", "recurrent_date");
        searchResponse = client().prepareSearch("test")
                .setQuery(scriptQuery(new Script("notHasExpired", ScriptService.ScriptType.INLINE, "native", params)))
                .execute().actionGet();
        logger.info("Eventos que estão ocorrendo");
        logger.info(searchResponse.toString());
        assertNoFailures(searchResponse);
        assertHitCount(searchResponse, 3);

        // Show eventos em dezembro
        params = new HashMap<>();
        params.put("field", "recurrent_date");
        params.put("start", "2017-01-01");
        params.put("end", "2018-05-31");
        searchResponse = client().prepareSearch("test")
                .setQuery(scriptQuery(new Script("occurBetween", ScriptService.ScriptType.INLINE, "native", params)))
                .addScriptField("occur", new Script(
                        "occurrencesBetween", ScriptService.ScriptType.INLINE, "native", params))
                .execute().actionGet();
        logger.info(searchResponse.toString());
        assertNoFailures(searchResponse);

    }

    private XContentBuilder createDoc(String name, String dtstart, String dtend, String rrule) throws IOException {
        return jsonBuilder().startObject()
                    .field("name", name)
                    .startObject("recurrent_date")
                        .field("start_date", dtstart)
                        .field("end_date", dtend)
                        .field("rrule", rrule)
                    .endObject()
                .endObject();
    }

}
