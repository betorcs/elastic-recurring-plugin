package org.devmaster.elasticsearch.plugin;

import org.elasticsearch.common.inject.AbstractModule;

public class RecurringIndexModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(RegisterRecurringType.class).asEagerSingleton();
    }
}
