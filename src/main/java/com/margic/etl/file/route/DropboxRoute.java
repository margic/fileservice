package com.margic.etl.file.route;

import com.margic.etl.file.model.Transaction;
import lombok.Getter;
import lombok.Setter;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.PropertyInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jacksonxml.JacksonXMLDataFormat;

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
        JacksonXMLDataFormat txnFormat = new JacksonXMLDataFormat(Transaction.class);
        from(dropboxUri)
                .routeId("dropbox")
                .convertBodyTo(String.class)
                .setProperty("institution", xpath("//header/institution", String.class))
                .split(xpath("//transactions/transaction"))
                .parallelProcessing()
                .convertBodyTo(String.class)
                .setHeader("institution", exchangeProperty("institution"))
                .unmarshal(txnFormat)
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
                                 /*
                                  * TODO stop the route after processing the file.
                                  * will enable this when file change trigger is added
                                  */
                                 //stop.start();
                             }
                         }
                );
    }
}
