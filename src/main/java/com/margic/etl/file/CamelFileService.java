package com.margic.etl.file;

import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by paulcrofts on 6/3/16.
 */
@Slf4j
public final class CamelFileService {

    /**
     * Hide the default constructor.
     */
    private CamelFileService() {
    }

    /**
     * Start the camel file processor app.
     * @param args application arguments
     */
    public static void main(final String... args) {

        log.info("Starting Camel File Service");

        Injector injector = Guice.createInjector(new FileModule());

    }

}
