package com.oneandone.iocunitejb.persistencefactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.sql.DataSource;

import org.junit.runner.RunWith;

import com.oneandone.iocunit.analyzer.annotations.TestClasses;
import com.oneandone.iocunit.IocUnitRunner;
import com.oneandone.iocunit.ejb.persistence.SinglePersistenceFactory;

/**
 * @author aschoerk
 */
@RunWith(IocUnitRunner.class)
@TestClasses({ PFH2Test.TestDbSinglePersistenceFactory.class })
public class PFH2Test extends PersistenceFactoryTestBase {

    @ApplicationScoped
    public static class TestDbSinglePersistenceFactory extends SinglePersistenceFactory {

        @Produces
        @Override
        public EntityManager newEm() {
            return produceEntityManager();
        }

        /**
         * create a jdbc-Datasource using the same driver url user and password as the entityManager
         *
         * @return a jdbc-Datasource using the same driver url user and password as the entityManager
         */
        @Produces
        @Override
        public DataSource produceDataSource() {
            return super.produceDataSource();
        }
    }

}
