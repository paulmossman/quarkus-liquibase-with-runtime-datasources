package org.acme.liquibase;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.jboss.logging.Logger;

import io.quarkus.runtime.annotations.StaticInitSafe;

@StaticInitSafe
public class InMemoryConfigSource implements ConfigSource {

    private static final Logger LOG = Logger.getLogger(InMemoryConfigSource.class);

    private static final Map<String, String> configuration = new HashMap<String, String>();

    static {

        String dbSchemaList = System.getenv("DB_SCHEMAS");

        if (dbSchemaList != null && dbSchemaList.length() != 0) {

            LOG.info("DB_SCHEMAS: " + dbSchemaList);

            String[] dbSchemas = dbSchemaList.split(",");
            for (String dbSchema : dbSchemas) {
                LOG.info("   schema: '" + dbSchema + "'");
                configuration.put("quarkus.datasource." + dbSchema + ".db-kind", "postgresql");
                configuration.put("quarkus.datasource." + dbSchema + ".username", "quarkus_test");
                configuration.put("quarkus.datasource." + dbSchema + ".password", "quarkus_test");
                configuration.put("quarkus.datasource." + dbSchema + ".jdbc.url", "jdbc:postgresql://localhost:5432/quarkus_test");
                configuration.put("quarkus.liquibase." + dbSchema + ".default-schema-name", dbSchema);
                configuration.put("quarkus.liquibase." + dbSchema + ".migrate-at-start", "true");
            }
        }
    }

    @Override
    public Set<String> getPropertyNames() {
        return configuration.keySet();
    }

    @Override
    public String getValue(String propertyName) {
        return configuration.get(propertyName);
    }

    @Override
    public String getName() {
        return InMemoryConfigSource.class.getSimpleName();
    }
}
