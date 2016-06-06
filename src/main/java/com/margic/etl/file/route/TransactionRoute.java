package com.margic.etl.file.route;

import lombok.Getter;
import lombok.Setter;
import org.apache.camel.Exchange;
import org.apache.camel.PropertyInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.http.HttpHeaders;


/**
 * Created by paulcrofts on 6/5/16.
 * Route that sends messages to the transaction service
 */
public class TransactionRoute extends RouteBuilder {

    /**
     * Delay between retries if delivery fails.
     */
    private static final long REDELIVERY_DELAY = 1000L;
    /**
     * Maximum number or retries is http service is not available or error occurs.
     */
    private static final int MAXIMUM_REDELIVERIES = 5;

    /**
     * toUri provides field to allow configuration of endpoint
     * by injecting property or setting manually in a test.
     */
    @Setter
    @Getter
    @PropertyInject("{{com.margic.etl.file.route.TransactionRoute.toUri}}")
    private String toUri;

    @Override
    public final void configure() throws Exception {
        from("direct:transaction")
                .errorHandler(defaultErrorHandler()
                        .redeliveryDelay(REDELIVERY_DELAY)
                        .maximumRedeliveries(MAXIMUM_REDELIVERIES))
                .routeId("transaction")
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .setHeader(HttpHeaders.CONTENT_TYPE, constant("application/json"))
                .marshal().json(JsonLibrary.Jackson)
                .to("log:com.margic.etl.file?showBody=true&multiline=true")
                .to(toUri);
    }
}
