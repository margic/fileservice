package com.margic.etl.file.camel;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultModelJAXBContextFactory;
import org.apache.camel.main.MainLifecycleStrategy;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.spi.ModelJAXBContextFactory;
import org.apache.camel.support.ServiceSupport;
import org.apache.camel.util.ServiceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base class for main implementations to allow starting up a JVM with Camel embedded.
 * Cleaned this up to remove some uncessary methods from camel mainsupport.
 * Suppressing checkstyle on this file in mysuppressions.xml it's a mess.
 * // TODO finish clean up and remove uncessary methods
 * @version
 */
@Slf4j
public abstract class MainSupport extends ServiceSupport {
    private static final int UNINITIALIZED_EXIT_CODE = Integer.MIN_VALUE;
    private static final int DEFAULT_EXIT_CODE = 0;
    private final List<MainListener> listeners = new ArrayList<>();
    private final CountDownLatch latch = new CountDownLatch(1);
    private final AtomicBoolean completed = new AtomicBoolean(false);
    private final AtomicInteger exitCode = new AtomicInteger(UNINITIALIZED_EXIT_CODE);
    private long duration = -1;
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;
    private boolean trace;
    private List<RouteBuilder> routeBuilders = new ArrayList<RouteBuilder>();
    private String routeBuilderClasses;
    private final List<CamelContext> camelContexts = new ArrayList<CamelContext>();
    private ProducerTemplate camelTemplate;
    private boolean hangupInterceptorEnabled = true;
    private int durationHitExitCode = DEFAULT_EXIT_CODE;

    /**
     * A class for intercepting the hang up signal and do a graceful shutdown of the Camel.
     */
    private static final class HangupInterceptor extends Thread {
        Logger log = LoggerFactory.getLogger(this.getClass());
        private final MainSupport mainInstance;

        HangupInterceptor(MainSupport main) {
            mainInstance = main;
        }

        @Override
        public void run() {
            log.info("Received hang up - stopping the main instance.");
            try {
                mainInstance.stop();
            } catch (Exception ex) {
                log.warn("Error during stopping the main instance.", ex);
            }
        }
    }

    protected MainSupport() {
    }

    /**
     * Runs this process with the given arguments, and will wait until completed, or the JVM terminates.
     */
    private void run() throws Exception {
        if (!completed.get()) {
            internalBeforeStart();
            // if we have an issue starting then propagate the exception to caller
            beforeStart();
            start();
            try {
                afterStart();
                waitUntilCompleted();
                internalBeforeStop();
                beforeStop();
                stop();
                afterStop();
            } catch (Exception e) {
                // however while running then just log errors
                log.error("Failed: " + e, e);
            }
        }
    }

    /**
     * Disable the hangup support. No graceful stop by calling stop() on a
     * Hangup signal.
     */
    public void disableHangupSupport() {
        hangupInterceptorEnabled = false;
    }

    /**
     * Hangup support is enabled by default.
     *
     * @deprecated is enabled by default now, so no longer need to call this method.
     */
    @Deprecated
    public void enableHangupSupport() {
        hangupInterceptorEnabled = true;
    }

    /**
     * Adds a {@link org.apache.camel.main.MainListener} to receive callbacks when the main is started or stopping
     *
     * @param listener the listener
     */
    public void addMainListener(MainListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes the {@link org.apache.camel.main.MainListener}
     *
     * @param listener the listener
     */
    public void removeMainListener(MainListener listener) {
        listeners.remove(listener);
    }

    /**
     * Callback to run custom logic before CamelContext is being started.
     * <p/>
     * It is recommended to use {@link org.apache.camel.main.MainListener} instead.
     */
    private void beforeStart() throws Exception {
        for (MainListener listener : listeners) {
            listener.beforeStart(this);
        }
    }

    /**
     * Callback to run custom logic after CamelContext has been started.
     * <p/>
     * It is recommended to use {@link org.apache.camel.main.MainListener} instead.
     */
    private void afterStart() throws Exception {
        for (MainListener listener : listeners) {
            listener.afterStart(this);
        }
    }

    private void internalBeforeStart() {
        if (hangupInterceptorEnabled) {
            Runtime.getRuntime().addShutdownHook(new MainSupport.HangupInterceptor(this));
        }
    }

    /**
     * Callback to run custom logic before CamelContext is being stopped.
     * <p/>
     * It is recommended to use {@link org.apache.camel.main.MainListener} instead.
     */
    private void beforeStop() throws Exception {
        for (MainListener listener : listeners) {
            listener.beforeStop(this);
        }
    }

    /**
     * Callback to run custom logic after CamelContext has been stopped.
     * <p/>
     * It is recommended to use {@link org.apache.camel.main.MainListener} instead.
     */
    private void afterStop() throws Exception {
        for (MainListener listener : listeners) {
            listener.afterStop(this);
        }
    }

    private void internalBeforeStop() {
        try {
            if (camelTemplate != null) {
                ServiceHelper.stopService(camelTemplate);
                camelTemplate = null;
            }
        } catch (Exception e) {
            log.debug("Error stopping camelTemplate due " + e.getMessage() + ". This exception is ignored.", e);
        }
    }

    /**
     * Marks this process as being completed.
     */
    public void completed() {
        completed.set(true);
        exitCode.compareAndSet(UNINITIALIZED_EXIT_CODE, DEFAULT_EXIT_CODE);
        latch.countDown();
    }

    public long getDuration() {
        return duration;
    }

    /**
     * Sets the duration to run the application for in milliseconds until it
     * should be terminated. Defaults to -1. Any value <= 0 will run forever.
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    /**
     * Sets the time unit duration.
     */
    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    /**
     * Sets the exit code for the application if duration was hit
     */
    public void setDurationHitExitCode(int durationHitExitCode) {
        this.durationHitExitCode = durationHitExitCode;
    }

    public int getDurationHitExitCode() {
        return durationHitExitCode;
    }

    public int getExitCode() {
        return exitCode.get();
    }


    public void setRouteBuilderClasses(String builders) {
        this.routeBuilderClasses = builders;
    }

    public String getRouteBuilderClasses() {
        return routeBuilderClasses;
    }

    public boolean isTrace() {
        return trace;
    }

    public void enableTrace() {
        this.trace = true;
    }

    protected void doStop() throws Exception {
        // call completed to properly stop as we count down the waiting latch
        completed();
    }

    protected void doStart() throws Exception {
    }

    private void waitUntilCompleted() {
        while (!completed.get()) {
            try {
                if (duration > 0) {
                    TimeUnit unit = getTimeUnit();
                    log.info("Waiting for: " + duration + " " + unit);
                    latch.await(duration, unit);
                    exitCode.compareAndSet(UNINITIALIZED_EXIT_CODE, durationHitExitCode);
                    completed.set(true);
                } else {
                    latch.await();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Parses the command line arguments then runs the program.
     */
    protected void run(String[] args) throws Exception {
        run();
    }

    public List<CamelContext> getCamelContexts() {
        return camelContexts;
    }

    private List<RouteBuilder> getRouteBuilders() {
        return routeBuilders;
    }

    public void setRouteBuilders(List<RouteBuilder> routeBuilders) {
        this.routeBuilders = routeBuilders;
    }

    public List<RouteDefinition> getRouteDefinitions() {
        List<RouteDefinition> answer = new ArrayList<RouteDefinition>();
        for (CamelContext camelContext : camelContexts) {
            answer.addAll(((ModelCamelContext)camelContext).getRouteDefinitions());
        }
        return answer;
    }

    public ProducerTemplate getCamelTemplate() throws Exception {
        if (camelTemplate == null) {
            camelTemplate = findOrCreateCamelTemplate();
        }
        return camelTemplate;
    }

    protected abstract ProducerTemplate findOrCreateCamelTemplate();

    protected abstract Map<String, CamelContext> getCamelContextMap();

    protected void postProcessContext() throws Exception {
        Map<String, CamelContext> map = getCamelContextMap();
        Set<Map.Entry<String, CamelContext>> entries = map.entrySet();
        for (Map.Entry<String, CamelContext> entry : entries) {
            CamelContext camelContext = entry.getValue();
            camelContexts.add(camelContext);
            postProcessCamelContext(camelContext);
        }
    }

    public ModelJAXBContextFactory getModelJAXBContextFactory() {
        return new DefaultModelJAXBContextFactory();
    }

    private void loadRouteBuilders(CamelContext camelContext) throws Exception {
        if (routeBuilderClasses != null) {
            // get the list of route builder classes
            String[] routeClasses = routeBuilderClasses.split(",");
            for (String routeClass : routeClasses) {
                Class<?> routeClazz = camelContext.getClassResolver().resolveClass(routeClass);
                RouteBuilder builder = (RouteBuilder) routeClazz.newInstance();
                getRouteBuilders().add(builder);
            }
        }
    }

    private void postProcessCamelContext(CamelContext camelContext) throws Exception {
        if (trace) {
            camelContext.setTracing(true);
        }
        // try to load the route builders from the routeBuilderClasses
        loadRouteBuilders(camelContext);
        for (RouteBuilder routeBuilder : routeBuilders) {
            camelContext.addRoutes(routeBuilder);
        }
        // register lifecycle so we are notified in Camel is stopped from JMX or somewhere else
        camelContext.addLifecycleStrategy(new MainLifecycleStrategy(completed, latch));
        // allow to do configuration before its started
        for (MainListener listener : listeners) {
            listener.configure(camelContext);
        }
    }

    public void addRouteBuilder(RouteBuilder routeBuilder) {
        getRouteBuilders().add(routeBuilder);
    }
}
