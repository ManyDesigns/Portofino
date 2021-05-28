package com.manydesigns.portofino.persistence;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public class CriteriaDefinition<T, R> {

    public final CriteriaQuery<T> query;
    public final CriteriaBuilder builder;
    public final Root<R> root;

    public CriteriaDefinition(CriteriaQuery<T> query, CriteriaBuilder builder, Root<R> root) {
        this.query = query;
        this.builder = builder;
        this.root = root;
    }
}
