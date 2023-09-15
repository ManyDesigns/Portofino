package com.manydesigns.portofino.persistence.hibernate;

import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import org.hibernate.event.spi.*;

public class Events {

    public final Subject<DatabaseScopedEvent<PreLoadEvent>> preLoad$ = PublishSubject.create();
    public final Subject<DatabaseScopedEvent<PostLoadEvent>> postLoad$ = PublishSubject.create();

    public final Subject<DatabaseScopedEvent<PreInsertEvent>> preInsert$ = PublishSubject.create();
    public final Subject<DatabaseScopedEvent<PostInsertEvent>> postInsert$ = PublishSubject.create();

    public final Subject<DatabaseScopedEvent<PreUpdateEvent>> preUpdate$ = PublishSubject.create();
    public final Subject<DatabaseScopedEvent<PostUpdateEvent>> postUpdate$ = PublishSubject.create();

    public final Subject<DatabaseScopedEvent<PreDeleteEvent>> preDelete$ = PublishSubject.create();
    public final Subject<DatabaseScopedEvent<PostDeleteEvent>> postDelete$ = PublishSubject.create();

}
