package com.manydesigns.portofino.model.diff;

import com.manydesigns.portofino.model.annotations.ModelAnnotation;

/**
 * File created on Oct 26, 2010 at 12:09:28 PM
 * Copyright Paolo Predonzani (paolo.predonzani@gmail.com)
 * All rights reserved
 */
public class ModelAnnotationDiff {
    private final ModelAnnotation sourceModelAnnotation;
    private final ModelAnnotation targetModelAnnotation;

    public ModelAnnotationDiff(ModelAnnotation sourceModelAnnotation,
                                     ModelAnnotation targetModelAnnotation) {
        this.sourceModelAnnotation = sourceModelAnnotation;
        this.targetModelAnnotation = targetModelAnnotation;
    }

    public ModelAnnotation getSourceModelAnnotation() {
        return sourceModelAnnotation;
    }

    public ModelAnnotation getTargetModelAnnotation() {
        return targetModelAnnotation;
    }
}
