package com.margic.etl.file.route;

import lombok.Getter;
import lombok.Setter;
import org.apache.camel.PropertyInject;
import org.apache.camel.builder.RouteBuilder;

/**
 * Created by paulcrofts on 6/5/16.
 * Route that sends messages to the transaction service
 */
public class TransactionRoute extends RouteBuilder {

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
                .to(toUri);
    }
}
