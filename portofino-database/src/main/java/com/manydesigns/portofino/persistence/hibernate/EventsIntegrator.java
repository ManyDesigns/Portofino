package com.manydesigns.portofino.persistence.hibernate;

import com.manydesigns.portofino.database.model.Database;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.*;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

public class EventsIntegrator implements Integrator {

    protected final Events events;
    protected final Database database;

    public EventsIntegrator(Events events, Database database) {
        this.events = events;
        this.database = database;
    }

    @Override
    public void integrate(Metadata metadata, BootstrapContext bootstrapContext, SessionFactoryImplementor sessionFactory) {
        final EventListenerRegistry eventListenerRegistry =
                sessionFactory.getServiceRegistry().getService(EventListenerRegistry.class);

        eventListenerRegistry.appendListeners(EventType.PRE_LOAD,
                event -> events.preLoad$.onNext(new DatabaseScopedEvent<>(event, database)));
        eventListenerRegistry.appendListeners(EventType.POST_LOAD,
                event -> events.postLoad$.onNext(new DatabaseScopedEvent<>(event, database)));
        eventListenerRegistry.appendListeners(EventType.PRE_INSERT, event -> {
            events.preInsert$.onNext(new DatabaseScopedEvent<>(event, database));
            return false;
        });
        eventListenerRegistry.appendListeners(EventType.POST_INSERT, new PostInsertEventListener() {
            @Override
            public void onPostInsert(PostInsertEvent event) {
                events.postInsert$.onNext(new DatabaseScopedEvent<>(event, database));
            }

            @Override
            public boolean requiresPostCommitHandling(EntityPersister persister) {
                return false;
            }
        });
        eventListenerRegistry.appendListeners(EventType.PRE_UPDATE, event -> {
            events.preUpdate$.onNext(new DatabaseScopedEvent<>(event, database));
            return false;
        });
        eventListenerRegistry.appendListeners(EventType.POST_UPDATE, new PostUpdateEventListener() {
            @Override
            public void onPostUpdate(PostUpdateEvent event) {
                events.postUpdate$.onNext(new DatabaseScopedEvent<>(event, database));
            }

            @Override
            public boolean requiresPostCommitHandling(EntityPersister persister) {
                return false;
            }
        });
        eventListenerRegistry.appendListeners(EventType.PRE_DELETE, event -> {
            events.preDelete$.onNext(new DatabaseScopedEvent<>(event, database));
            return false;
        });
        eventListenerRegistry.appendListeners(EventType.POST_DELETE, new PostDeleteEventListener() {
            @Override
            public void onPostDelete(PostDeleteEvent event) {
                events.postDelete$.onNext(new DatabaseScopedEvent<>(event, database));
            }

            @Override
            public boolean requiresPostCommitHandling(EntityPersister entityPersister) {
                return false;
            }
        });
    }

    @Override
    public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {}
}
