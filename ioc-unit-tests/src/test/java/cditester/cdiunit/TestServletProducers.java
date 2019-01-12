package cditester.cdiunit;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.oneandone.cdi.tester.CdiUnit2Runner;
import com.oneandone.cdi.tester.contexts.servlet.CdiUnitServlet;

/**
 * Taken from cdiunit-tests.
 * Originally no Qualifier @CdiUnitServlet so the SE-builtin (from >weld2) objects were used.
 * 
 */
@RunWith(CdiUnit2Runner.class)
public class TestServletProducers {
    @Inject
    @CdiUnitServlet
    private HttpServletRequest request;

    @Inject
    @CdiUnitServlet
    private HttpSession session;

    @Inject
    @CdiUnitServlet
    private ServletContext context;


    @Test
    public void testServletException() {
        Assert.assertNotNull(request);
        Assert.assertNotNull(session);
        Assert.assertNotNull(context);
        ServletException.class.getClass();
    }
}
