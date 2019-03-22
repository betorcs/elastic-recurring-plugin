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
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.search.Query;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;

import org.elasticsearch.index.mapper.*;
import org.elasticsearch.index.mapper.FieldMapper;
import org.elasticsearch.index.mapper.DateFieldMapper;
import org.elasticsearch.index.mapper.TextFieldMapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.apache.lucene.index.IndexOptions.DOCS;
import static org.elasticsearch.index.mapper.Mapper.BuilderContext;
import static org.elasticsearch.index.mapper.TypeParsers.parseField;
import static org.elasticsearch.index.mapper.TypeParsers.parseMultiField;
import org.elasticsearch.index.query.QueryShardContext;

public class RecurringFieldMapper extends FieldMapper {

    public static final String CONTENT_TYPE = "recurring";

    private final DateFieldMapper startDateMapper;
    private final DateFieldMapper endDateMapper;
    private final TextFieldMapper rruleMapper;

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
            //
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
        public Query termQuery(Object value, QueryShardContext context) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Query existsQuery(QueryShardContext context) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }

    public static TextFieldMapper.Builder textField(String name) {
        return new TextFieldMapper.Builder(name);
    }

    public static DateFieldMapper.Builder dateField(String name) {
        return new DateFieldMapper.Builder(name);
    }
    
    public static class Builder extends FieldMapper.Builder<Builder, RecurringFieldMapper> {

        private final Mapper.Builder startDateBuilder = dateField(FieldNames.START_DATE);
        private final Mapper.Builder endDateBuilder = dateField(FieldNames.END_DATE);
        private final Mapper.Builder rruleBuilder = textField(FieldNames.RRULE);

        protected Builder(String name) {
            super(name, Defaults.FIELD_TYPE, Defaults.FIELD_TYPE);
            builder = this;
        }

        @Override
        public RecurringFieldMapper build(BuilderContext context) {
            
            context.path().add(name);

            DateFieldMapper startDateMapper = (DateFieldMapper) startDateBuilder.build(context);
            DateFieldMapper endDateMapper = (DateFieldMapper) endDateBuilder.build(context);
            TextFieldMapper rruleMapper = (TextFieldMapper) rruleBuilder.build(context);

            context.path().remove();

            MappedFieldType default_field_type = Defaults.FIELD_TYPE.clone();
            
            if (this.fieldType.indexOptions() != IndexOptions.NONE && !this.fieldType.tokenized()) {
                
                default_field_type.setOmitNorms(true);
                default_field_type.setIndexOptions(DOCS);
                
                if (!this.omitNormsSet && this.fieldType.boost() == 1.0F) {
                    this.fieldType.setOmitNorms(true);
                }

                if (!this.indexOptionsSet) {
                    this.fieldType.setIndexOptions(DOCS);
                }
            }
            
            default_field_type.freeze();

            this.setupFieldType(context);
            
            return new RecurringFieldMapper(
                    name, 
                    fieldType, 
                    default_field_type, 
                    context.indexSettings(), 
                    startDateMapper, 
                    endDateMapper, 
                    rruleMapper, 
                    multiFieldsBuilder.build(this, context), 
                    copyTo
            );
        }
    }
    
    public static class TypeParser implements Mapper.TypeParser {
        @Override
        public Mapper.Builder<?, ?> parse(String name, Map<String, Object> node, ParserContext parserContext)
                throws MapperParsingException {
            
            Builder builder = new Builder(name);
            
            // tweaking these settings is no longer allowed, the entire purpose of murmur3 fields is to store a hash
            if (node.get("doc_values") != null) {
                throw new MapperParsingException("Setting [doc_values] cannot be modified for field [" + name + "]");
            }
            if (node.get("index") != null) {
                throw new MapperParsingException("Setting [index] cannot be modified for field [" + name + "]");
            }

            for (Iterator<Map.Entry<String, Object>> iterator = node.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry<String, Object> entry = iterator.next();
                String propName = entry.getKey();
                Object propNode = entry.getValue();
                if (parseMultiField(builder, name, parserContext, propName, propNode)) {
                    iterator.remove();
                }
            }
            
            parseField(builder, name, node, parserContext);

            return builder;
        }
    }
    
    protected RecurringFieldMapper(
            String simpleName, 
            MappedFieldType fieldType, 
            MappedFieldType defaultFieldType,
            Settings indexSettings, 
            DateFieldMapper dtstartMapper,
            DateFieldMapper dtendMapper, 
            TextFieldMapper rruleMapper, 
            MultiFields multiFields,
            CopyTo copyTo
    ) {
        super(simpleName, fieldType, defaultFieldType, indexSettings, multiFields, copyTo);

        this.startDateMapper = dtstartMapper;
        this.endDateMapper = dtendMapper;
        this.rruleMapper = rruleMapper;
    }

    @Override
    protected void parseCreateField(ParseContext context, List<IndexableField> fields) throws IOException {
        throw new UnsupportedOperationException("Parsing is implemented in parse(), this method should NEVER be called");
    }

    @Override
    public void parse(ParseContext context) throws IOException {
        context.path().add(simpleName());

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
        List<? extends Mapper> extras = Arrays.asList(
                startDateMapper,
                endDateMapper,
                rruleMapper
        );
        
        return Iterators.concat(super.iterator(), extras.iterator());
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        
        builder.startObject(simpleName());
        builder.field("type", CONTENT_TYPE);
        
        startDateMapper.toXContent(builder, params);
        endDateMapper.toXContent(builder, params);
        rruleMapper.toXContent(builder, params);
        multiFields.toXContent(builder, params);
        
        builder.endObject();
        
        return super.toXContent(builder, params);
    }
}