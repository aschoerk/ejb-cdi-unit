package com.oneandone.ejbcdiunit.asynchmdb;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import javax.inject.Inject;

import org.junit.Rule;
import org.junit.Test;

import com.oneandone.cdi.testanalyzer.InitialConfiguration;
import com.oneandone.cdi.testanalyzer.annotations.TestClasses;
import com.oneandone.cdi.tester.CdiUnit2Rule;
import com.oneandone.cdi.tester.ejb.AsynchronousManager;
import com.oneandone.ejbcdiunit.ejbs.CountingBean;
import com.oneandone.ejbcdiunit.ejbs.SingletonTimerEJB;
import com.oneandone.ejbcdiunit.ejbs.StatelessTimerEJB;

/**
 * @author aschoerk
 */
@TestClasses({ StatelessTimerEJB.class, SingletonTimerEJB.class })
public class TestTimerExclusion {
    @Inject
    AsynchronousManager asynchronousManager;

    @Rule
    public CdiUnit2Rule getEjbUnitRule() {
        return new CdiUnit2Rule(this, new InitialConfiguration().exclude(SingletonTimerEJB.class));
    }

    @Test
    public void testTimer() {
        SingletonTimerEJB a = new SingletonTimerEJB();
        a.logcall();
        assertThat(CountingBean.get(), is(CountingBean.INITIALCOUNT + 1));
        asynchronousManager.once();
        // 1 calls of timer beans
        // inner call of SingletonTimerEJB to StatelessAsynchEJB not yet done
        assertThat(CountingBean.get(), is(CountingBean.INITIALCOUNT + 2));
        asynchronousManager.once();
        // 1 calls of timer beans
        // previous inner call of SingletonTimerEJB to StatelessAsynchEJB added
        assertThat(CountingBean.get(), is(CountingBean.INITIALCOUNT + 3));
    }

}
