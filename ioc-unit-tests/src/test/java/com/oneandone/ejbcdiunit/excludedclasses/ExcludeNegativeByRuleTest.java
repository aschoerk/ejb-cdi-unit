package com.oneandone.ejbcdiunit.excludedclasses;

import static org.hamcrest.Matchers.is;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import com.oneandone.cdi.testanalyzer.InitialConfiguration;
import com.oneandone.cdi.testanalyzer.annotations.SutPackages;
import com.oneandone.cdi.tester.CdiUnit2Rule;
import com.oneandone.ejbcdiunit.ejbs.SingletonTimerEJB;
import com.oneandone.ejbcdiunit.excludedclasses.pcktoinclude.ToExclude;
import com.oneandone.ejbcdiunit.excludedclasses.pcktoinclude.ToInclude;

/**
 * @author aschoerk
 */
@SutPackages({ ToInclude.class })
public class ExcludeNegativeByRuleTest {

    @Inject
    ToInclude toInclude;
    @Inject
    ToExclude.ToExcludeProduced toExcludeProduced; // produced by not excluded ToExclude

    @Rule
    public CdiUnit2Rule getEjbUnitRule() {
        ToInclude.count = 0;
        return new CdiUnit2Rule(this, new InitialConfiguration().exclude(SingletonTimerEJB.class));
    }

    @Test
    public void test() {
        Assert.assertThat(toInclude.count, is(2));
        Assert.assertThat(toExcludeProduced.getValue(), is(10));
    }
}
