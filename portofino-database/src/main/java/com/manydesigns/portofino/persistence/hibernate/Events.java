package com.manydesigns.portofino.persistence.hibernate;

import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import org.hibernate.event.spi.*;

public class Events {

    public final Subject<PreLoadEvent> preLoad$ = PublishSubject.create();
    public final Subject<PostLoadEvent> postLoad$ = PublishSubject.create();

    public final Subject<PreInsertEvent> preInsert$ = PublishSubject.create();
    public final Subject<PostInsertEvent> postInsert$ = PublishSubject.create();

    public final Subject<PreUpdateEvent> preUpdate$ = PublishSubject.create();
    public final Subject<PostUpdateEvent> postUpdate$ = PublishSubject.create();

    public final Subject<PreDeleteEvent> preDelete$ = PublishSubject.create();
    public final Subject<PostDeleteEvent> postDelete$ = PublishSubject.create();

}
