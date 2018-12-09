package com.oneandone.ejbcdiunit.example3;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.oneandone.cdi.testanalyzer.annotations.SutClasses;
import com.oneandone.cdi.tester.CdiUnit2Runner;
import com.oneandone.cdi.tester.ejb.AsynchronousManager;

/**
 * @author aschoerk
 */
@RunWith(CdiUnit2Runner.class)
@SutClasses({ AsynchonousService.class })
public class AsynchronousServiceMultithreadedTest {
    @Inject
    AsynchronousServiceIntf sut;

    @Inject
    AsynchronousManager asynchronousManager;

    @Produces
    RemoteServiceIntf remoteService = new RemoteServiceSimulator();

    @Before
    public void beforeReallyAsynchronousServiceTest() {
        asynchronousManager.setEnqueAsynchronousCalls(true); // asynchronous futures don't handle the call themselves
        asynchronousManager.startThread(); // a thread will periodically check for actions to be handled
    }

    @Test
    public void canServiceInsertEntity1Remotely() throws ExecutionException, InterruptedException {
        AsynchronousServiceIntf.CorrelationId correlationId = sut.newRemoteEntity1(1, "test1");
        Long id = null;
        while (id == null) {
            id = sut.pollId(correlationId);
        }
        assertThat(id, is(1L));
    }

    @Test
    public void canReadEntity1AfterInsertion() throws ExecutionException, InterruptedException {
        List<AsynchronousServiceIntf.CorrelationId> correlationIds = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            correlationIds.add(sut.newRemoteEntity1(i, "string: " + i));
        }
        // fetch the 6th inserted entity.
        Long id = null;
        while (id == null) {
            id = sut.pollId(correlationIds.get(5));
        }
        final AsynchronousServiceIntf.CorrelationId correlationId = sut.getRemoteStringValueFor(id);
        String s = null;
        while (s == null) {
            s = sut.pollString(correlationId);
        }
        assertThat(s, is(remoteService.getStringValueFor(id)));
    }

}
