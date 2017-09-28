package org.devmaster.elasticsearch.plugin;

import org.devmaster.elasticsearch.index.mapper.RecurringFieldMapper;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.AbstractIndexComponent;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.settings.IndexSettings;

public class RegisterRecurringType extends AbstractIndexComponent {

    @Inject
    public RegisterRecurringType(Index index, @IndexSettings Settings indexSettings, MapperService mapperService) {
        super(index, indexSettings);
        mapperService.documentMapperParser().putTypeParser(RecurringFieldMapper.CONTENT_TYPE, new RecurringFieldMapper.TypeParser());
    }
}
