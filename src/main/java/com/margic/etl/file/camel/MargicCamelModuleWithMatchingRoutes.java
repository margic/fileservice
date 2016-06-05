package com.margic.etl.file.camel;

import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.matcher.Matcher;
import com.google.inject.matcher.Matchers;
import org.apache.camel.CamelContext;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.guice.CamelModule;
import org.apache.camel.guice.inject.Injectors;
import org.apache.camel.impl.DefaultCamelBeanPostProcessor;

import java.util.Set;

/**
 * Created by paulcrofts on 6/5/16.
 * Adds post processing to route builder instances to enable PropertyInject annotation on routes
 */
public class MargicCamelModuleWithMatchingRoutes extends CamelModule {

    /**
     * Matcher used in matching specific routes.
     */
    private final Matcher<Class> matcher;

    /**
     * Camel bean post processor to process objects created by the injector.
     */
    private DefaultCamelBeanPostProcessor postProcessor;

    /**
     * Creates a module that scans for all routebuilder classes.
     */
    public MargicCamelModuleWithMatchingRoutes() {
        this(Matchers.subclassesOf(RoutesBuilder.class));
    }

    /**
     * Creates a module that only matches specific routebuilders.
     * @param argMatcher matcher to filter routebuilders
     */
    public MargicCamelModuleWithMatchingRoutes(final Matcher<Class> argMatcher) {
        this.matcher = argMatcher;
    }

    /**
     * Creats instances of the route builders.
     * @param injector injector used to instantiate routes
     * @return a set of routebuilders
     */
    @Provides
    final Set<RoutesBuilder> routes(final Injector injector) {
        Set<RoutesBuilder> routes = Injectors.getInstancesOf(injector, matcher);
        if (postProcessor == null) {
            CamelContext context = injector.getInstance(CamelContext.class);
            PropertiesComponent pc = new PropertiesComponent();
            pc.setLocation("classpath:camel.properties");
            context.addComponent("properties", pc);
            postProcessor = new DefaultCamelBeanPostProcessor(injector.getInstance(CamelContext.class));
        }
        for (RoutesBuilder r: routes) {
            if (r != null) {
                try {
                    postProcessor.postProcessBeforeInitialization(r, r.getClass().getName());
                    postProcessor.postProcessAfterInitialization(r, r.getClass().getName());
                } catch (Exception e) {
                    throw new RuntimeCamelException("Error during post processing of bean " + r, e);
                }
            }
        }
        return routes;
    }
}
