package com.margic.etl.file.route;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.file.GenericFile;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;

/**
 * Created by paulcrofts on 6/5/16.
 */
@Slf4j
public class DropboxRouteTest extends CamelTestSupport {

    @Override
    protected RoutesBuilder[] createRouteBuilders() throws Exception {
        context.setTracing(true);
        RoutesBuilder[] routes = new RoutesBuilder[2];

        routes[0] = new RouteBuilder() {
            /**
             * Drop box component creates a ByteArrayOutputStream.
             * This route reads a sample file and creates a ByteArrayOutputStream
             * to simulate the component.
             * @throws Exception
             */
            @Override
            public void configure() throws Exception {
                from("file:src/test/data?noop=true")
                        .process(new Processor() {
                            @Override
                            public void process(Exchange exchange) throws Exception {
                                log.info("Prepping file to simulate drop box producer");
                                GenericFile gFile = exchange.getIn().getBody(GenericFile.class);
                                File file = (File)gFile.getFile();
                                ByteArrayOutputStream out = new ByteArrayOutputStream((int) file.length());

                                byte[] bytes = FileUtils.readFileToByteArray(file);
                                out.write(bytes, 0, bytes.length);
                                Exchange newExchange = createExchangeWithBody(out);
                                template.send("direct:drop", newExchange);
                            }
                        });
            }
        };

        DropboxRoute drop = new DropboxRoute();
        drop.setDropboxUri("direct:drop");
        drop.setToUri("mock:mock");
        routes[1] = drop;
        return routes;
    }

    @Test
    public void testFile() throws Exception {
        MockEndpoint mockEndpoint = getMockEndpoint("mock:mock");
        mockEndpoint.expectedHeaderReceived("institution", "ABC");
        mockEndpoint.expectedMessageCount(2);

        assertMockEndpointsSatisfied();
        assertTrue(true);
    }
}
