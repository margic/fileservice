package com.margic.etl.file.route;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

/**
 * Created by paulcrofts on 6/5/16.
 * Basic camel route test to test the transaction route
 */
@Slf4j
public class TransactionRouteTest extends CamelTestSupport {

    @Override
    protected RoutesBuilder createRouteBuilder() throws Exception {
        String toUri = "mock:mock";
        log.info("Creating a transaction route with toUri: {}", toUri);
        TransactionRoute route = new TransactionRoute();
        route.setToUri(toUri);
        return route;
    }

    @Test
    public void testRoute(){
        assertTrue(true); // favorite test to get things rolling
    }
}
