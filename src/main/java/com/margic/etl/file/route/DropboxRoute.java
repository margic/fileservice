package com.margic.etl.file.route;

import lombok.Getter;
import lombok.Setter;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.PropertyInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.xml.XPathBuilder;

/**
 * Created by paulcrofts on 6/5/16.
 * Retrieves files from drop box for processing.
 */
public class DropboxRoute extends RouteBuilder {

    /**
     * Uri of dropbox.
     */
    @PropertyInject("{{com.margic.etl.file.route.DropboxRoute.dropboxUri}}")
    @Getter
    @Setter
    private String dropboxUri;

    /**
     * Uri to save downloaded files.
     */
    @PropertyInject("{{com.margic.etl.file.route.DropboxRoute.toUri}}")
    @Getter
    @Setter
    private String toUri;

    @Override
    public final void configure() throws Exception {
        // set up transaction splitter
        XPathBuilder xPathBuilder = new XPathBuilder("//transactions/transaction");
        from(dropboxUri)
                .routeId("dropbox")
                .convertBodyTo(String.class)
                .split(xPathBuilder)
                .parallelProcessing()
                .to(toUri)
                .process(new Processor() {
                             private Thread stop;

                             @Override
                             public void process(final Exchange exchange) throws Exception {
                                 // stop this route using a thread that will stop
                                 // this route gracefully while we are still running
                                 if (stop == null) {
                                     stop = new Thread() {
                                         @Override
                                         public void run() {
                                             try {
                                                 exchange.getContext().stopRoute("dropbox");
                                             } catch (Exception e) {
                                                 // ignore
                                             }
                                         }
                                     };
                                 }
                                 // start the thread that stops this route
                                 stop.start();
                             }
                         }
                );
    }
}
