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
import org.apache.lucene.document.Field;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.mapper.*;
import org.elasticsearch.index.mapper.core.DateFieldMapper;
import org.elasticsearch.index.mapper.core.StringFieldMapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.index.mapper.MapperBuilders.dateField;
import static org.elasticsearch.index.mapper.MapperBuilders.stringField;
import static org.elasticsearch.index.mapper.core.TypeParsers.parseField;
import static org.elasticsearch.index.mapper.core.TypeParsers.parseMultiField;

public class RecurringFieldMapper extends FieldMapper {

    public static final String CONTENT_TYPE = "recurring";

    private final DateFieldMapper startDateMapper;
    private final DateFieldMapper endDateMapper;
    private final StringFieldMapper rruleMapper;
    private final ContentPath.Type pathType;

    public static class RecurringFieldType extends MappedFieldType {

        protected MappedFieldType startDateFieldType;
        protected MappedFieldType endDateFieldType;
        protected MappedFieldType rruleFieldType;

        public RecurringFieldType() {
        }

        protected RecurringFieldType(RecurringFieldMapper.RecurringFieldType ref) {
            super(ref);
            this.startDateFieldType = ref.startDateFieldType;
            this.endDateFieldType = ref.endDateFieldType;
            this.rruleFieldType = ref.rruleFieldType;
        }

        @Override
        public MappedFieldType clone() {
            return new RecurringFieldMapper.RecurringFieldType(this);
        }

        @Override
        public String typeName() {
            return CONTENT_TYPE;
        }

        @Override
        public Object value(Object value) {
            return value == null ? null : value.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (!super.equals(o)) return false;
            RecurringFieldType that = (RecurringFieldType) o;
            return java.util.Objects.equals(startDateFieldType, that.startDateFieldType)
                    && java.util.Objects.equals(endDateFieldType, that.endDateFieldType)
                    && java.util.Objects.equals(rruleFieldType, that.rruleFieldType);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(super.hashCode(), startDateFieldType, endDateFieldType, rruleFieldType);
        }

        public MappedFieldType startDateFieldType() {
            return endDateFieldType;
        }

        public MappedFieldType endDateFieldType() {
            return endDateFieldType;
        }

        public MappedFieldType rruleFieldType() {
            return rruleFieldType;
        }

    }


    public static class Names {
        public static String START_DATE = "start_date";
        public static String END_DATE = "end_date";
        public static String RRULE = "rrule";
    }

    public static class Defaults {
        public static final ContentPath.Type PATH_TYPE = ContentPath.Type.FULL;
    }

    public static class TypeParser implements Mapper.TypeParser {

        @Override
        public Mapper.Builder<?, ?> parse(String name, Map<String, Object> node, ParserContext parserContext)
                throws MapperParsingException {

            RecurringFieldMapper.Builder builder = new RecurringFieldMapper.Builder(name);
            parseField(builder, name, node, parserContext);

            for (Iterator<Map.Entry<String, Object>> iterator = node.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<String, Object> entry = iterator.next();
                String propName = Strings.toUnderscoreCase(entry.getKey());
                Object propNode = entry.getValue();

                if (parseMultiField(builder, name, parserContext, propName, propNode)) {
                    iterator.remove();
                }
            }


            return builder;
        }
    }


    public static class Builder extends FieldMapper.Builder<Builder, RecurringFieldMapper> {

        protected ContentPath.Type pathType = Defaults.PATH_TYPE;

        protected Builder(String name) {
            super(name, new RecurringFieldType(), new RecurringFieldType());
            this.builder = this;
        }

        @Override
        public RecurringFieldMapper build(BuilderContext context) {
            context.path().pathType(pathType);
            context.path().add(name);

            DateFieldMapper.Builder startDateMapperBuilder = dateField(Names.START_DATE).includeInAll(false);
            DateFieldMapper.Builder endDateMapperBuilder = dateField(Names.END_DATE).includeInAll(false);
            StringFieldMapper.Builder rruleMapperBuilder = stringField(Names.RRULE).includeInAll(false);

            DateFieldMapper startDateMapper = startDateMapperBuilder.includeInAll(false)
                    .store(fieldType.stored()).build(context);
            DateFieldMapper endDateMapper = endDateMapperBuilder.includeInAll(false)
                    .store(fieldType.stored()).build(context);
            StringFieldMapper rruleMapper = rruleMapperBuilder.includeInAll(false)
                    .store(fieldType.stored()).build(context);

            this.setupFieldType(context);

            return new RecurringFieldMapper(name, fieldType, defaultFieldType, context.indexSettings(), pathType,
                    startDateMapper, endDateMapper, rruleMapper, multiFieldsBuilder.build(this, context), copyTo);
        }

    }

    protected RecurringFieldMapper(String simpleName, MappedFieldType fieldType, MappedFieldType defaultFieldType,
                                   Settings indexSettings, ContentPath.Type pathType, DateFieldMapper dtstartMapper,
                                   DateFieldMapper dtendMapper, StringFieldMapper rruleMapper, MultiFields multiFields,
                                   CopyTo copyTo) {
        super(simpleName, fieldType, defaultFieldType, indexSettings, multiFields, copyTo);

        this.startDateMapper = dtstartMapper;
        this.endDateMapper = dtendMapper;
        this.rruleMapper = rruleMapper;
        this.pathType = pathType;
    }

    @Override
    protected void parseCreateField(ParseContext parseContext, List<Field> list) throws IOException {
        throw new UnsupportedOperationException("Parsing is implemented in parse(), this method should NEVER be called");
    }

    @Override
    public Mapper parse(ParseContext context) throws IOException {
        ContentPath.Type origPathType = context.path().pathType();
        context.path().pathType(pathType);
        context.path().add(simpleName());

        XContentParser parser = context.parser();

        Recurring recurring = new Recurring();
        Map<String, Object> map = parser.map();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (Names.START_DATE.equals(entry.getKey())) {
                recurring.setStartDate((String) entry.getValue());
            } else if (Names.END_DATE.equals(entry.getKey())) {
                recurring.setEndDate((String) entry.getValue());
            } else if (Names.RRULE.equals(entry.getKey())) {
                recurring.setRrule((String) entry.getValue());
            }
        }

        startDateMapper.parse(context.createExternalValueContext(recurring.getStartDate()));

        if (null != recurring.getEndDate()) {
            endDateMapper.parse(context.createExternalValueContext(recurring.getEndDate()));
        }

        if (null != recurring.getRrule()) {
            rruleMapper.parse(context.createExternalValueContext(recurring.getRrule()));
        }

        multiFields.parse(this, context.createExternalValueContext(recurring));

        context.path().remove();
        context.path().pathType(origPathType);
        return null;
    }

    @Override
    protected String contentType() {
        return CONTENT_TYPE;
    }

    @Override
    public RecurringFieldMapper.RecurringFieldType fieldType() {
        return (RecurringFieldType) super.fieldType();
    }

    @Override
    public Iterator<Mapper> iterator() {
        List<? extends Mapper> extras = Arrays.asList(
                startDateMapper,
                endDateMapper,
                rruleMapper);
        return Iterators.concat(super.iterator(), extras.iterator());
    }
}