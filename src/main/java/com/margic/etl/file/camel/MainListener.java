package com.margic.etl.file.camel;

import org.apache.camel.CamelContext;

/**
 * Created by paulcrofts on 6/5/16.
 */
public interface MainListener {
    /**
     * Callback before the CamelContext(s) is being created and started.
     *
     * @param main  the main instance
     */
    void beforeStart(MainSupport main);

    /**
     * Callback to configure <b>each</b> created CamelContext.
     * <p/>
     * Notice this callback will be invoked for <b>each</b> CamelContext and therefore can be invoked
     * multiple times if there is 2 or more CamelContext's being created.
     *
     * @param context the created CamelContext
     */
    void configure(CamelContext context);

    /**
     * Callback after the CamelContext(s) has been started.
     *
     * @param main  the main instance
     */
    void afterStart(MainSupport main);

    /**
     * Callback before the CamelContext(s) is being stopped.
     *
     * @param main  the main instance
     */
    void beforeStop(MainSupport main);

    /**
     * Callback after the CamelContext(s) has been stopped.
     *
     * @param main  the main instance
     */
    void afterStop(MainSupport main);
}
