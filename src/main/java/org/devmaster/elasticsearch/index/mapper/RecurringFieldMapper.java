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

package org.devmaster.elasticsearch.index.mapper;

import com.google.common.collect.Iterators;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.Query;
import org.devmaster.elasticsearch.Recurring;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.mapper.DateFieldMapper;
import org.elasticsearch.index.mapper.FieldMapper;
import org.elasticsearch.index.mapper.KeywordFieldMapper;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.MapperParsingException;
import org.elasticsearch.index.mapper.ParseContext;
import org.elasticsearch.index.query.QueryShardContext;
import org.elasticsearch.index.query.QueryShardException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.apache.lucene.index.IndexOptions.DOCS;

public class RecurringFieldMapper extends FieldMapper {

    public static final String CONTENT_TYPE = "recurring";

    private final DateFieldMapper startDateMapper;
    private final DateFieldMapper endDateMapper;
    private final KeywordFieldMapper rruleMapper;

    public static class Defaults {

        public static final RecurringFieldType FIELD_TYPE = new RecurringFieldType();

        static {
            FIELD_TYPE.freeze();
        }
    }

    public static class FieldNames {
        public static String START_DATE = "start_date";
        public static String END_DATE = "end_date";
        public static String RRULE = "rrule";
    }

    public static class RecurringFieldType extends MappedFieldType {

        public RecurringFieldType() {
        }

        protected RecurringFieldType(RecurringFieldType ref) {
            super(ref);
        }

        @Override
        public MappedFieldType clone() {
            return new RecurringFieldType(this);
        }

        @Override
        public String typeName() {
            return CONTENT_TYPE;
        }

        @Override
        public Query termQuery(Object value, @Nullable QueryShardContext context) {
            throw new QueryShardException(context, "Recurring fields are not searchable: [" + name() + "].");
        }

        @Override
        public Query existsQuery(QueryShardContext context) {
            return null;
        }

    }

    public static class Builder extends FieldMapper.Builder<Builder, RecurringFieldMapper> {

        private DateFieldMapper.Builder startDateBuilder = new DateFieldMapper.Builder(FieldNames.START_DATE)
                    .format("yyyy-MM-dd");
        private DateFieldMapper.Builder endDateBuilder = new DateFieldMapper.Builder(FieldNames.END_DATE)
                    .format("yyyy-MM-dd");
        private KeywordFieldMapper.Builder rruleBuilder = new KeywordFieldMapper.Builder(FieldNames.RRULE);

        protected Builder(String name) {
            super(name, new RecurringFieldType(), new RecurringFieldType());
            this.builder = this;
        }

        @Override
        public RecurringFieldMapper build(BuilderContext context) {

            context.path().add(name);

            DateFieldMapper startDateMapper = startDateBuilder.build(context);
            DateFieldMapper endDateMapper = endDateBuilder.build(context);
            KeywordFieldMapper rruleMapper = rruleBuilder.build(context);

            context.path().remove();

            MappedFieldType defaultFieldType = Defaults.FIELD_TYPE.clone();
            if (this.fieldType.indexOptions() != IndexOptions.NONE && !this.fieldType.tokenized()) {
                defaultFieldType.setOmitNorms(true);
                defaultFieldType.setIndexOptions(DOCS);
                if (!this.omitNormsSet && this.fieldType.boost() == 1.0F) {
                    this.fieldType.setOmitNorms(true);
                }

                if (!this.indexOptionsSet) {
                    this.fieldType.setIndexOptions(DOCS);
                }
            }
            defaultFieldType.freeze();

            this.setupFieldType(context);
            return new RecurringFieldMapper(name, fieldType, defaultFieldType, context.indexSettings(),
                    startDateMapper, endDateMapper, rruleMapper, multiFieldsBuilder.build(this, context), copyTo);
        }

    }


    public static class TypeParser implements Mapper.TypeParser {

        @Override
        public Mapper.Builder<?, ?> parse(String name, Map<String, Object> node, ParserContext parserContext)
                throws MapperParsingException {

            return new RecurringFieldMapper.Builder(name);
        }
    }

    protected RecurringFieldMapper(String simpleName, MappedFieldType fieldType, MappedFieldType defaultFieldType,
                                   Settings indexSettings, DateFieldMapper startDateMapper,
                                   DateFieldMapper endDateMapper, KeywordFieldMapper rruleMapper, MultiFields multiFields,
                                   CopyTo copyTo) {
        super(simpleName, fieldType, defaultFieldType, indexSettings, multiFields, copyTo);

        this.startDateMapper = startDateMapper;
        this.endDateMapper = endDateMapper;
        this.rruleMapper = rruleMapper;
    }

    @Override
    protected void parseCreateField(ParseContext context, List<IndexableField> fields) throws IOException {
        throw new UnsupportedOperationException("Parsing is implemented in parse(), this method should NEVER be called");
    }

    @Override
    public void parse(ParseContext context) throws IOException {

        context.path().add(name());

        XContentParser parser = context.parser();

        Recurring recurring = new Recurring();
        Map<String, Object> map = parser.map();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (FieldNames.START_DATE.equals(entry.getKey())) {
                recurring.setStartDate((String) entry.getValue());
            } else if (FieldNames.END_DATE.equals(entry.getKey())) {
                recurring.setEndDate((String) entry.getValue());
            } else if (FieldNames.RRULE.equals(entry.getKey())) {
                recurring.setRrule((String) entry.getValue());
            }
        }

        startDateMapper.parse(context.createExternalValueContext(recurring.getStart()));

        if (null != recurring.getEnd()) {
            endDateMapper.parse(context.createExternalValueContext(recurring.getEnd()));
        }

        if (null != recurring.getRrule()) {
            rruleMapper.parse(context.createExternalValueContext(recurring.getRrule()));
        }

        multiFields.parse(this, context.createExternalValueContext(recurring));

        context.path().remove();
    }

    @Override
    protected String contentType() {
        return CONTENT_TYPE;
    }

    @Override
    public RecurringFieldType fieldType() {
        return (RecurringFieldType) super.fieldType();
    }

    @Override
    public Iterator<Mapper> iterator() {
        List<? extends Mapper> extras = Arrays.asList(startDateMapper, endDateMapper, rruleMapper);
        return Iterators.concat(super.iterator(), extras.iterator());
    }

}