package com.oneandone.iocunit.analyzer.specializing.testsamples;

import javax.inject.Inject;

import com.oneandone.iocunit.analyzer.annotations.SutClasses;
import com.oneandone.iocunit.analyzer.specializing.sutsamples.BaseClass;
import com.oneandone.iocunit.analyzer.specializing.sutsamples.SpecializingClass;

/**
 * @author aschoerk
 */
@SutClasses(BaseClass.class)
public class TestWithBaseAsSutClassSpecAsInject {
    @Inject
    SpecializingClass specializingClass;
}
