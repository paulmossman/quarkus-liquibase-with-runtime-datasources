package org.acme.liquibase;

import io.quarkus.arc.All;
import io.quarkus.arc.InstanceHandle;
import io.quarkus.liquibase.LiquibaseFactory;
import liquibase.Liquibase;
import liquibase.changelog.ChangeSetStatus;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class MigrationService {
    // You can Inject the object if you want to use it manually
    @Inject
    @All
    List<InstanceHandle<LiquibaseFactory>> liquibaseFactoryInstanceHandles;

    private static final Logger LOG = Logger.getLogger(MigrationService.class);

    public List<ChangeSetStatus> checkMigration() throws Exception {

        List<ChangeSetStatus> statuses = new ArrayList<ChangeSetStatus>();
        LOG.info("liquibaseFactoryInstanceHandles.size(): " + liquibaseFactoryInstanceHandles.size());
        for (InstanceHandle<LiquibaseFactory> handle : liquibaseFactoryInstanceHandles) {
            if (handle.getBean().getScope().equals(ApplicationScoped.class)) {
                LiquibaseFactory liquibaseFactory = handle.get();

                // Dev profile:
                // - liquibaseFactoryInstanceHandles.size(): 2 (# of tenants)
                // - Liquibase migrations are re-run.

                // Prod profile:
                // - liquibaseFactoryInstanceHandles.size(): 1
                // - Request failed: javax.enterprise.inject.UnsatisfiedResolutionException: No
                // datasource has been configured.

                // Use the liquibase instance manually
                try (Liquibase liquibase = liquibaseFactory.createLiquibase()) {
                    liquibase.dropAll();
                    liquibase.validate();
                    liquibase.update(liquibaseFactory.createContexts(), liquibaseFactory.createLabels());
                    // Get the list of liquibase change set statuses
                    statuses.addAll(liquibase.getChangeSetStatuses(liquibaseFactory.createContexts(), liquibaseFactory.createLabels()));
                }
            }
        }

        return statuses;
    }
}