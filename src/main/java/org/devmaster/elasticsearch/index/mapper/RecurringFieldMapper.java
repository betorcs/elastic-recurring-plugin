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

import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.analysis.NamedAnalyzer;
import org.elasticsearch.index.codec.docvaluesformat.DocValuesFormatProvider;
import org.elasticsearch.index.codec.postingsformat.PostingsFormatProvider;
import org.elasticsearch.index.fielddata.FieldDataType;
import org.elasticsearch.index.mapper.ContentPath;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.MapperParsingException;
import org.elasticsearch.index.mapper.ParseContext;
import org.elasticsearch.index.mapper.core.AbstractFieldMapper;
import org.elasticsearch.index.mapper.core.DateFieldMapper;
import org.elasticsearch.index.mapper.core.StringFieldMapper;
import org.elasticsearch.index.similarity.SimilarityProvider;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.index.mapper.MapperBuilders.dateField;
import static org.elasticsearch.index.mapper.MapperBuilders.stringField;
import static org.elasticsearch.index.mapper.core.TypeParsers.parseField;
import static org.elasticsearch.index.mapper.core.TypeParsers.parseMultiField;

public class RecurringFieldMapper extends AbstractFieldMapper<Recurring> {

    public static final String CONTENT_TYPE = "recurring";

    private final DateFieldMapper startDateMapper;
    private final DateFieldMapper endDateMapper;
    private final StringFieldMapper rruleMapper;
    private final ContentPath.Type pathType;

    static class Defaults {

        static final ContentPath.Type PATH_TYPE = ContentPath.Type.FULL;

        static final FieldType FIELD_TYPE = new FieldType();

        static {
            FIELD_TYPE.freeze();
        }
    }

    public static class FieldNames {
        public static String START_DATE = "start_date";
        public static String END_DATE = "end_date";
        public static String RRULE = "rrule";
    }



    public static class Builder extends AbstractFieldMapper.Builder<Builder, RecurringFieldMapper> {

        ContentPath.Type pathType = Defaults.PATH_TYPE;

        private Mapper.Builder startDateBuilder = dateField(FieldNames.START_DATE);
        private Mapper.Builder endDateBuilder = dateField(FieldNames.END_DATE);
        private Mapper.Builder rruleBuilder = stringField(FieldNames.RRULE);

        public Builder(String name) {
            super(name, new FieldType(Defaults.FIELD_TYPE));
            this.builder = this;
        }

        @Override
        public RecurringFieldMapper build(BuilderContext context) {
            ContentPath.Type origPathType = context.path().pathType();
            context.path().pathType(pathType);

            context.path().add(name);

            DateFieldMapper startDateMapper = (DateFieldMapper) startDateBuilder.build(context);
            DateFieldMapper endDateMapper = (DateFieldMapper) endDateBuilder.build(context);
            StringFieldMapper rruleMapper = (StringFieldMapper) rruleBuilder.build(context);

            context.path().remove();

            context.path().pathType(origPathType);

//            MappedFieldType defaultFieldType = Defaults.FIELD_TYPE.clone();
//            if (this.fieldType.indexOptions() != FieldInfo.IndexOptions.NONE && !this.fieldType.tokenized()) {
//                defaultFieldType.setOmitNorms(true);
//                defaultFieldType.setIndexOptions(DOCS);
//                if (!this.omitNormsSet && this.fieldType.boost() == 1.0F) {
//                    this.fieldType.setOmitNorms(true);
//                }
//
//                if (!this.indexOptionsSet) {
//                    this.fieldType.setIndexOptions(DOCS);
//                }
//            }
//            defaultFieldType.freeze();
//
//            this.setupFieldType(context);


            // startDateMapper, endDateMapper, rruleMapper
            return new RecurringFieldMapper(buildNames(context), boost, fieldType, docValues, indexAnalyzer, searchAnalyzer,
                    postingsProvider, docValuesProvider, similarity, normsLoading, fieldDataSettings,
                    context.indexSettings(), origPathType, startDateMapper, endDateMapper, rruleMapper);
        }

    }


    public static class TypeParser implements Mapper.TypeParser {

        @Override
        public Mapper.Builder<?, ?> parse(String name, Map<String, Object> node, ParserContext parserContext)
                throws MapperParsingException {

            RecurringFieldMapper.Builder builder = new RecurringFieldMapper.Builder(name);
            parseField(builder, name, node, parserContext);

            for (Map.Entry<String, Object> entry : node.entrySet()) {
                String propName = Strings.toUnderscoreCase(entry.getKey());
                Object propNode = entry.getValue();
                parseMultiField(builder, name, parserContext, propName, propNode);
            }


            return builder;
        }
    }




    protected RecurringFieldMapper(Names names, float boost, FieldType fieldType, Boolean docValues,
                                   NamedAnalyzer indexAnalyzer, NamedAnalyzer searchAnalyzer,
                                   PostingsFormatProvider postingsFormat, DocValuesFormatProvider docValuesFormat,
                                   SimilarityProvider similarity, Loading normsLoading, @Nullable Settings fieldDataSettings,
                                   Settings indexSettings, ContentPath.Type pathType, DateFieldMapper dtstartMapper,
                                   DateFieldMapper dtendMapper, StringFieldMapper rruleMapper) {

        super(names, boost, fieldType, docValues, indexAnalyzer, searchAnalyzer, postingsFormat, docValuesFormat,
                similarity, normsLoading, fieldDataSettings, indexSettings);

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
    public void parse(ParseContext context) throws IOException {
        ContentPath.Type origPathType = context.path().pathType();
        context.path().pathType(pathType);
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
    }

    @Override
    protected String contentType() {
        return CONTENT_TYPE;
    }

    @Override
    public FieldType defaultFieldType() {
        return null;
    }

    @Override
    public FieldDataType defaultFieldDataType() {
        return null;
    }

    @Override
    public Recurring value(Object o) {
        return null;
    }



    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(name());
        builder.field("type", CONTENT_TYPE);

        startDateMapper.toXContent(builder, params);
        endDateMapper.toXContent(builder, params);
        rruleMapper.toXContent(builder, params);
        multiFields.toXContent(builder, params);
        builder.endObject();

        return builder;
    }
}