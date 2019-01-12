package com.oneandone.ejbcdiunit.ejb;

import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.oneandone.cdi.testanalyzer.annotations.EnabledAlternatives;
import com.oneandone.cdi.testanalyzer.annotations.TestClasses;
import com.oneandone.cdi.tester.CdiUnit2Runner;
import com.oneandone.ejbcdiunit.ejbs.SingletonEJB;
import com.oneandone.ejbcdiunit.helpers.J2eeSimTest1Factory;
import com.oneandone.ejbcdiunit.helpers.LoggerGenerator;
import com.oneandone.ejbcdiunit.helpers.SessionContextFactoryAlternative;

/**
 * @author aschoerk
 */
@RunWith(CdiUnit2Runner.class)
@TestClasses({ SingletonEJB.class, J2eeSimTest1Factory.class, LoggerGenerator.class })
@EnabledAlternatives({ SessionContextFactoryAlternative.class })
public class TestEjbSpecialContext {

    @Inject
    J2eeSimTest1Factory persistenceFactory;
    @EJB
    SingletonEJB singletonEJB;

    @Produces
    @ApplicationScoped
    EntityManager createEntityManager() {
        return persistenceFactory.produceEntityManager();
    }

    @Test
    public void checkContextAlternative() {
        Assert.assertThat(singletonEJB.getPrincipal().getName(), Matchers.is(SessionContextFactoryAlternative.PRINCIPAL_NAME));
    }


}
