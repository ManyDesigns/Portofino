package com.manydesigns.portofino.model.diff;

import com.manydesigns.portofino.model.annotations.Annotation;

/**
 * File created on Oct 26, 2010 at 12:09:28 PM
 * Copyright Paolo Predonzani (paolo.predonzani@gmail.com)
 * All rights reserved
 */
public class ModelAnnotationDiff {
    private final Annotation sourceAnnotation;
    private final Annotation targetAnnotation;

    public ModelAnnotationDiff(Annotation sourceAnnotation,
                                     Annotation targetAnnotation) {
        this.sourceAnnotation = sourceAnnotation;
        this.targetAnnotation = targetAnnotation;
    }

    public Annotation getSourceModelAnnotation() {
        return sourceAnnotation;
    }

    public Annotation getTargetModelAnnotation() {
        return targetAnnotation;
    }
}
