package com.manydesigns.portofino.persistence.hibernate;

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

    public EventsIntegrator(Events events) {
        this.events = events;
    }

    @Override
    public void integrate(Metadata metadata, BootstrapContext bootstrapContext, SessionFactoryImplementor sessionFactory) {
        final EventListenerRegistry eventListenerRegistry =
                sessionFactory.getServiceRegistry().getService(EventListenerRegistry.class);

        eventListenerRegistry.appendListeners(EventType.PRE_LOAD, events.preLoad$::onNext);
        eventListenerRegistry.appendListeners(EventType.POST_LOAD, events.postLoad$::onNext);
        eventListenerRegistry.appendListeners(EventType.PRE_INSERT, event -> {
            events.preInsert$.onNext(event);
            return false;
        });
        eventListenerRegistry.appendListeners(EventType.POST_INSERT, new PostInsertEventListener() {
            @Override
            public void onPostInsert(PostInsertEvent event) {
                events.postInsert$.onNext(event);
            }

            @Override
            public boolean requiresPostCommitHandling(EntityPersister persister) {
                return false;
            }
        });
        eventListenerRegistry.appendListeners(EventType.PRE_UPDATE, event -> {
            events.preUpdate$.onNext(event);
            return false;
        });
        eventListenerRegistry.appendListeners(EventType.POST_UPDATE, new PostUpdateEventListener() {
            @Override
            public void onPostUpdate(PostUpdateEvent event) {
                events.postUpdate$.onNext(event);
            }

            @Override
            public boolean requiresPostCommitHandling(EntityPersister persister) {
                return false;
            }
        });
        eventListenerRegistry.appendListeners(EventType.PRE_DELETE, preDeleteEvent -> {
            events.preDelete$.onNext(preDeleteEvent);
            return false;
        });
        eventListenerRegistry.appendListeners(EventType.POST_DELETE, new PostDeleteEventListener() {
            @Override
            public void onPostDelete(PostDeleteEvent postDeleteEvent) {
                events.postDelete$.onNext(postDeleteEvent);
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
