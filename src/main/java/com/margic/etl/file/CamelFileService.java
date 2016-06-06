package com.margic.etl.file;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.margic.etl.file.camel.MainSupport;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;

import java.util.Map;

/**
 * Created by paulcrofts on 6/3/16.
 */
@Slf4j
public final class CamelFileService extends MainSupport {
    /**
     * Instance of file service used to support shutdown.
     */
    private static CamelFileService instance;
    /**
     * Instance of the camel context.
     */
    private static CamelContext context;
    /**
     * Hide the default constructor.
     */
    private CamelFileService() {
    }

    /**
     * Start the camel file processor app.
     *
     * @param args application arguments
     * @throws Exception thrown by run
     */
    public static void main(final String... args) throws Exception {

        log.info("Starting Camel File Service");

        Injector injector = Guice.createInjector(new FileModule());

        context = injector.getInstance(CamelContext.class);
        context.setTracing(true);

        instance = new CamelFileService();
        instance.run(args);
    }

    @Override
    protected ProducerTemplate findOrCreateCamelTemplate() {
        return null;
    }

    @Override
    protected Map<String, CamelContext> getCamelContextMap() {
        return null;
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
        log.debug("Stopping camel contexts");
        if (context != null) {
            context.stop();
        }
    }
}
