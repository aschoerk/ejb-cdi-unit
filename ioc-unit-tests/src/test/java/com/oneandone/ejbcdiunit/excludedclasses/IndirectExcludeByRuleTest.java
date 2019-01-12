package com.oneandone.ejbcdiunit.excludedclasses;

import static org.junit.Assert.fail;

import javax.inject.Inject;

import org.junit.Rule;
import org.junit.Test;

import com.oneandone.cdi.testanalyzer.annotations.SutClasses;
import com.oneandone.cdi.testanalyzer.annotations.TestClasses;
import com.oneandone.cdi.tester.CdiUnit2Rule;
import com.oneandone.cdi.weldstarter.StarterDeploymentException;
import com.oneandone.ejbcdiunit.excludedclasses.pcktoinclude.ToExclude;

/**
 * @author aschoerk
 */
@TestClasses({ IndirectExcluding.class })
@SutClasses({ ToExclude.class })
public class IndirectExcludeByRuleTest {
    @Rule
    public CdiUnit2Rule getEjbUnitRule() {
        return new CdiUnit2Rule(this);
    }

    @Inject
    ToExclude toExclude;

    @Test(expected = StarterDeploymentException.class)
    public void test() {
        fail("test should not start");
    }
}
