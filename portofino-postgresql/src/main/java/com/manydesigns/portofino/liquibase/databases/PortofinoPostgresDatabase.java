package com.manydesigns.portofino.liquibase.databases;

import liquibase.database.core.PostgresDatabase;

/**
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class PortofinoPostgresDatabase extends PostgresDatabase {

    public PortofinoPostgresDatabase() {
        super();
        unmodifiableDataTypes.clear(); //Do read column length from the database
    }

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

}
