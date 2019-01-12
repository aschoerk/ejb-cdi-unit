package com.oneandone.ejbcdiunit.excludedclasses;

import static org.hamcrest.Matchers.is;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.oneandone.cdi.testanalyzer.annotations.ExcludedClasses;
import com.oneandone.cdi.testanalyzer.annotations.SutPackages;
import com.oneandone.cdi.tester.CdiUnit2Runner;
import com.oneandone.ejbcdiunit.excludedclasses.pcktoinclude.ToExclude;
import com.oneandone.ejbcdiunit.excludedclasses.pcktoinclude.ToInclude;

/**
 * @author aschoerk
 */
@RunWith(CdiUnit2Runner.class)
@SutPackages({ ToInclude.class })
@ExcludedClasses({ ToExclude.class })
public abstract class AbstractExcludeTest {

    @Inject
    ToInclude toInclude;
    @Produces
    ToExclude.ToExcludeProduced tmp = new ToExclude.ToExcludeProduced(11); // no produces clash with excluded ToExclude
    @Inject
    ToExclude.ToExcludeProduced toExcludeProduced;

    @BeforeClass
    public static void initToInclude() {
        ToInclude.count = 0;
    }

    @Test
    public void test() {
        Assert.assertThat(toInclude.count, is(1));
        Assert.assertThat(toExcludeProduced.getValue(), is(11));
    }
}
