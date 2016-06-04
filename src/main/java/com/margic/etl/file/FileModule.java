package com.margic.etl.file;

import org.apache.camel.guice.CamelModuleWithMatchingRoutes;

/**
 * Created by paulcrofts on 6/3/16.
 * Creates a guice module that injects camel routes into the
 * guice camel context
 */
public class FileModule extends CamelModuleWithMatchingRoutes {

    @Override
    protected final void configure() {
        super.configure();

        // bind my routes here
    }
}
