package com.manydesigns.portofino.model.io.dsl;

import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.issues.Issue;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.eclipse.emf.ecore.EObject;

public class ModelIssueErrorListener extends BaseErrorListener {

    private final Model model;
    private final EObject object;

    public ModelIssueErrorListener(Model model, EObject object) {
        this.model = model;
        this.object = object;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        model.getIssues().add(new Issue(Issue.Severity.ERROR, object, msg, "TODO", line, charPositionInLine));
    }
}
