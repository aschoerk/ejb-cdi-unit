package com.oneandone.cdi.testanalyzer;

import com.oneandone.cdi.testanalyzer.extensions.TestScopeExtension;
import com.oneandone.cdi.weldstarter.WeldSetupClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.Extension;
import java.lang.reflect.Method;

/**
 * @author aschoerk
 */
public class ConfigCreator extends ConfigCreatorBase {

    static private Logger logger = LoggerFactory.getLogger(ConfigCreator.class);

    Configuration configuration = new Configuration();

    private void init(InitialConfiguration initial, TesterExtensionsConfigsFinder testerExtensionsConfigsFinder) {
        if(initial.testClass != null) {
            if (initial.testClass.getAnnotation(ApplicationScoped.class) == null) {
                configuration.getElseClasses().extensionObjects.add(new TestScopeExtension(initial.testClass));
            }
            configuration.testClass(initial.testClass).candidate(initial.testClass);
        }
        configuration.setTesterExtensionsConfigsFinder(testerExtensionsConfigsFinder);

        configuration.testClassCandidates(testerExtensionsConfigsFinder.initialClasses)
                .testClassCandidates(initial.initialClasses)
                .testClassCandidates(initial.testClasses);
        for (Class<?> c : initial.sutClasses) {
            configuration
                    .candidate(c);
        }
        for (Class<?> c : initial.enabledAlternatives) {
            configuration.testClass(c)
                    .candidate(c)
                    .enabledAlternative(c);
        }
        if(initial.excludedClasses != null) {
            for (Class<?> c : initial.excludedClasses) {
                configuration.excluded(c);
            }
        }
        if(initial.sutClasspath != null && !initial.sutClasspath.isEmpty()) {
            throw new RuntimeException("not implemented yet");
            // addClasspaths(setToArray(initial.sutClasspath), true);
        }
        if(initial.testClasspath != null && !initial.testClasspath.isEmpty()) {
            throw new RuntimeException("not implemented yet");
            // addClasspaths(setToArray(initial.testClasspath), false);
        }
        if(initial.sutPackages != null && !initial.sutPackages.isEmpty()) {
            throw new RuntimeException("not implemented yet");
            // addPackages(setToArray(initial.sutPackages), true);
        }
        if(initial.testPackages != null && !initial.testPackages.isEmpty()) {
            throw new RuntimeException("not implemented yet");
            // addPackages(setToArray(initial.sutPackages), false);
        }

    }

    public void create(InitialConfiguration initial) {
        this.init(initial, new TesterExtensionsConfigsFinder());
        Phase1Analyzer phase1Analyzer = new Phase1Analyzer(configuration);
        do {
            if (phase1Analyzer.work()) {
                new Phase2Matcher(configuration).work();
                new Phase3Fixer(configuration).work();

                logger.trace("One Level done candidates size: {} injects.size: {}", !configuration.emptyCandidates(), configuration.getInjects().size());
            } else {
                break;
            }
        }
        while (!configuration.emptyCandidates() && configuration.getInjects().size() > 0);
        new Phase4Warner(initial, configuration).work();
    }


    public Configuration getConfiguration() {
        return configuration;
    }

    public WeldSetupClass buildWeldSetup(Method method) {

        return new SetupCreator(configuration).buildWeldSetup(method);
    }
}
