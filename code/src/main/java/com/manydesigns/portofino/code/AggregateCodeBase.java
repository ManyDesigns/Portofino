package com.manydesigns.portofino.code;

import io.reactivex.disposables.Disposable;
import org.apache.commons.vfs2.FileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AggregateCodeBase extends AbstractCodeBase {

    private static final Logger logger = LoggerFactory.getLogger(AggregateCodeBase.class);
    protected final List<CodeBaseWithSubscription> codeBases = new CopyOnWriteArrayList<>();

    public AggregateCodeBase() {
        super(null);
    }

    public AggregateCodeBase(CodeBase parent, ClassLoader classLoader) {
        super(null, parent, classLoader);
    }

    @Override
    protected Class loadLocalClass(String className) throws IOException, ClassNotFoundException {
        for(CodeBaseWithSubscription c : codeBases) {
            try {
                Class aClass = c.codeBase.loadClass(className, SearchScope.LOCAL);
                if(aClass != null) {
                    return aClass;
                }
            } catch (ClassNotFoundException e) { //Don't catch IOExceptions. Is this the right thing to do?
                logger.debug("Class " + className + " not found in codebase " + c.codeBase, e);
            }
        }
        return null;
    }

    @Override
    public void clear(boolean recursively) throws Exception {
        super.clear(recursively);
        for(CodeBaseWithSubscription c : codeBases) {
            c.codeBase.clear(recursively);
        }
    }

    @Override
    public void close() {
        super.close();
        codeBases.forEach(c -> {
            c.subscription.dispose();
            c.codeBase.close();
        });
        codeBases.clear();
    }

    public void add(CodeBase codeBase) {
        codeBases.add(new CodeBaseWithSubscription(codeBase, codeBase.getReloads().subscribe(reloads::onNext)));
    }

    public boolean remove(CodeBase codeBase) {
        return codeBases.removeIf(c -> {
            if(c.codeBase == codeBase) {
                c.subscription.dispose();
                return true;
            }
            return false;
        });
    }
}

class CodeBaseWithSubscription {
    CodeBase codeBase;
    Disposable subscription;
    CodeBaseWithSubscription(CodeBase codeBase, Disposable subscription) {
        this.codeBase = codeBase;
        this.subscription = subscription;
    }
}
