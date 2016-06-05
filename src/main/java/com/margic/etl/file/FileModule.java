package com.margic.etl.file;

import com.margic.etl.file.camel.MargicCamelModuleWithMatchingRoutes;
import com.margic.etl.file.route.TransactionRoute;

/**
 * Created by paulcrofts on 6/3/16.
 * Creates a guice module that injects camel routes into the
 * guice camel context
 */
class FileModule extends MargicCamelModuleWithMatchingRoutes {

    @Override
    protected final void configure() {
        super.configure();
        // ignore error is binding is required for matchinroutes module
        bind(TransactionRoute.class);
    }



}
