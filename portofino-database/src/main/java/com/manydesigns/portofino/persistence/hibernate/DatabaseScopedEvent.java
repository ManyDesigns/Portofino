package com.manydesigns.portofino.persistence.hibernate;

import com.manydesigns.portofino.database.model.Database;
import org.hibernate.event.spi.AbstractEvent;

public class DatabaseScopedEvent<E extends AbstractEvent> {
    public final E event;
    public final Database database;

    public DatabaseScopedEvent(E event, Database database) {
        this.event = event;
        this.database = database;
    }
}
