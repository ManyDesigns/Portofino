package com.manydesigns.portofino.persistence.hibernate;

import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostLoadEvent;
import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreLoadEvent;

public class Events {

    public final Subject<PreLoadEvent> preLoad$ = PublishSubject.create();
    public final Subject<PostLoadEvent> postLoad$ = PublishSubject.create();

    public final Subject<PreInsertEvent> preInsert$ = PublishSubject.create();
    public final Subject<PostInsertEvent> postInsert$ = PublishSubject.create();

}
