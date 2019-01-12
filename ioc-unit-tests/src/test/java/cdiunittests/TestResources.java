package cdiunittests;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.mockito.Mock;

import com.oneandone.cdi.testanalyzer.annotations.ProducesAlternative;

import cditester.cdiunit.tobetestedcode.HelperClassInTestResources;

/**
 * @author aschoerk
 */
@ApplicationScoped
public class TestResources {

    @ProducesAlternative
    @Produces
    @Mock
    HelperClassInTestResources helperClassInTestResourcesMock;

    public HelperClassInTestResources getHelperClassInTestResourcesMock() {
        return helperClassInTestResourcesMock;
    }
}
