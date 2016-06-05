package com.margic.etl.file.route;

import com.margic.etl.file.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * Created by paulcrofts on 6/5/16.
 * Basic camel route test to test the transaction route
 */
@Slf4j
public class TransactionRouteTest extends CamelTestSupport {



    @Override
    protected RoutesBuilder createRouteBuilder() throws Exception {
        context.setTracing(true);
        String toUri = "mock:mock";
        log.info("Creating a transaction route with toUri: {}", toUri);
        TransactionRoute route = new TransactionRoute();
        route.setToUri(toUri);
        return route;
    }

    @Test
    public void testRoute() throws Exception {
        log.debug("Preparing test message");
        Transaction txn = Transaction.builder()
                .pan("ThePan")
                .merchant("TheMerchant")
                .currencyCode("USD")
                .amount(new BigDecimal("10.00")).build();
        Exchange exchange = createExchangeWithBody(txn);

        log.debug("Setting up mock expectations");
        MockEndpoint mock = getMockEndpoint("mock:mock");
        mock.expectedHeaderReceived(Exchange.HTTP_METHOD, "POST");

        log.debug("Sending test message {}", exchange);
        Exchange response = template.send("direct:transaction", exchange);

        assertMockEndpointsSatisfied();
    }
}
