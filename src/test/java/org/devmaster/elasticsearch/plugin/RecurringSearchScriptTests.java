package org.devmaster.elasticsearch.plugin;

import org.elasticsearch.action.index.IndexRequestBuilder;
//import org.elasticsearch.action.search.SearchResponse;
//import org.elasticsearch.script.Script;
//import org.elasticsearch.script.ScriptType;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;

//import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
//import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
//import static org.elasticsearch.index.query.QueryBuilders.scriptQuery;
//import static org.elasticsearch.index.query.QueryBuilders.termQuery;
//import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;
//import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertNoFailures;
//import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertHitCount;

import java.io.IOException;
//import java.util.HashMap;
import java.util.List;
//import java.util.Map;
import java.util.ArrayList;
//import static org.hamcrest.Matchers.equalTo;

public class RecurringSearchScriptTests extends AbstractSearchScriptTestCase {

public void testRecurringScript() throws Exception {

String mapping = XContentFactory.jsonBuilder()
.startObject()
.startObject("type")
.startObject("properties")
.startObject("name")
.field("type", "text")
.startObject("fields")
.startObject("analyzed")
.field("type", "text")
.field("analyzer", "std_lang")
.endObject()
.startObject("raw")
.field("type", "keyword")
.endObject()
.endObject()
.endObject()
.startObject("recurrent_date")
.field("type", "recurring")
.endObject()
.endObject()
.endObject()
.endObject().toString();

prepareCreate("test").addMapping("type", mapping, XContentType.JSON);
//assertAcked(prepareCreate("test").addMapping("type", mapping, XContentType.JSON));

List<IndexRequestBuilder> indexBuilders = new ArrayList<>();

indexBuilders.add(client().prepareIndex("test", "type", "1")
.setSource(createDoc("Christmas party", "2016-12-25", null, "RRULE:FREQ=YEARLY;BYMONTH=12;BYMONTHDAY=25")));

indexBuilders.add(client().prepareIndex("test", "type", "2")
.setSource(createDoc("Mother's Day", "2015-05-10", null, "RRULE:FREQ=YEARLY;BYMONTH=5;BYDAY=2SU")));

indexBuilders.add(client().prepareIndex("test", "type", "3")
.setSource(createDoc("Halloween Party", "2012-10-31", null, "RRULE:FREQ=YEARLY;BYMONTH=10;BYMONTHDAY=31;WKST=SU")));

indexBuilders.add(client().prepareIndex("test", "type", "4")
.setSource(createDoc("Cruze Monthly Review", "2016-02-10", null, "RRULE:FREQ=MONTHLY;BYMONTHDAY=10;COUNT=5;WKST=SU")));

indexBuilders.add(client().prepareIndex("test", "type", "5")
.setSource(createDoc("Marnaco Event", "2017-06-01", "2017-06-30", null)));

indexRandom(true, indexBuilders);

//check on no data, see it works
//SearchResponse searchResponse = client().prepareSearch("test").get();
//logger.info("totalHits:" + searchResponse.getHits().totalHits);
//assertThat(searchResponse.getHits().totalHits, equalTo(0L));
/*
// Show has any occurrence between
Map<String, String> params = new HashMap<>();
params.put("field", "recurrent_date");
params.put("start", "2017-06-27");
params.put("end", "2017-06-27");

SearchResponse searchResponse = client().prepareSearch("test")
.setQuery(matchAllQuery())
//.setQuery(scriptQuery(new Script("hasAnyOccurrenceBetween", ScriptService.ScriptType.INLINE, "native", params)))
.execute().actionGet();

logger.info(searchResponse.toString());
assertNoFailures(searchResponse);
assertHitCount(searchResponse, 1);
*/
/*
// Show next occurrences
Map<String, Object> params = new HashMap<>();
params.put("field", "recurrent_date");
SearchResponse searchResponse = client().prepareSearch("test")
.setQuery(scriptQuery(new Script(ScriptType.INLINE, "native", "nextOccurrence", params)))
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

// Show Mothers Day
params = new HashMap<>();
params.put("field", "recurrent_date");
params.put("date", "2025-05-11");
searchResponse = client().prepareSearch("test")
.setQuery(scriptQuery(new Script(ScriptType.INLINE, "native", "hasOccurrencesAt", params)))
.execute().actionGet();

logger.info("Mothers Day");
logger.info(searchResponse.toString());
assertNoFailures(searchResponse);
assertHitCount(searchResponse, 1);

// Show halloween
params = new HashMap<>();
params.put("field", "recurrent_date");
params.put("date", "2025-10-31");
searchResponse = client().prepareSearch("test")
.setQuery(scriptQuery(new Script(ScriptType.INLINE, "native", "hasOccurrencesAt", params)))
.execute().actionGet();

logger.info("Halloween");
logger.info(searchResponse.toString());
assertNoFailures(searchResponse);
assertHitCount(searchResponse, 1);

// Show Events in december
params = new HashMap<>();
params.put("field", "recurrent_date");
params.put("start", "2017-05-01");
params.put("end", "2017-05-31");
searchResponse = client().prepareSearch("test")
.setQuery(scriptQuery(new Script(ScriptType.INLINE, "native", "occurBetween", params)))
.execute().actionGet();

logger.info("Events in december");
logger.info(searchResponse.toString());
assertNoFailures(searchResponse);
assertHitCount(searchResponse, 1);

// Show Events that are occurring
params = new HashMap<>();
params.put("field", "recurrent_date");
searchResponse = client().prepareSearch("test")
.setQuery(scriptQuery(new Script(ScriptType.INLINE, "native", "notHasExpired", params)))
.execute().actionGet();

logger.info("Events that are occurring");
logger.info(searchResponse.toString());
assertNoFailures(searchResponse);
assertHitCount(searchResponse, 3);

// Show Events in december
params = new HashMap<>();
params.put("field", "recurrent_date");
params.put("start", "2017-01-01");
params.put("end", "2018-05-31");
searchResponse = client().prepareSearch("test")
.setQuery(scriptQuery(new Script(ScriptType.INLINE, "native", "occurBetween", params)))
.addScriptField("occur", new Script(ScriptType.INLINE, "native", "occurrencesBetween", params))
.execute().actionGet();

logger.info(searchResponse.toString());
assertNoFailures(searchResponse);
*/
}

private XContentBuilder createDoc(String name, String start_date, String end_date, String rrule) throws IOException {
return XContentFactory.jsonBuilder()
.startObject()
.field("name", name)
.startObject("recurrent_date")
.field("start_date", start_date)
.field("end_date", end_date)
.field("rrule", rrule)
.endObject()
.endObject();
}

}
