package com.manydesigns.portofino.code;

import io.reactivex.disposables.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ListIterator;
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
    public URL findResource(String name) throws IOException {
        for(CodeBaseWithSubscription c : codeBases) {
            URL resource = c.codeBase.findResource(name);
            if(resource != null) {
                return resource;
            }
        }
        if(parent != null) {
            return parent.findResource(name);
        } else {
            return null;
        }
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
        codeBases.add(subscribeToCodeBase(codeBase));
        reloads.onNext(getClass());
    }

    protected CodeBaseWithSubscription subscribeToCodeBase(CodeBase codeBase) {
        return new CodeBaseWithSubscription(codeBase, codeBase.getReloads().subscribe(reloads::onNext));
    }

    public boolean remove(CodeBase codeBase) {
        return codeBases.removeIf(c -> {
            if(c.codeBase.equals(codeBase)) {
                c.subscription.dispose();
                reloads.onNext(getClass());
                return true;
            }
            return false;
        });
    }

    public boolean replace(CodeBase oldCodeBase, CodeBase newCodeBase) {
        ListIterator<CodeBaseWithSubscription> iterator = codeBases.listIterator();
        while(iterator.hasNext()) {
            CodeBaseWithSubscription next = iterator.next();
            if(next.codeBase.equals(oldCodeBase)) {
                next.subscription.dispose();
                iterator.remove();
                iterator.add(subscribeToCodeBase(newCodeBase));
                reloads.onNext(getClass());
                return true;
            }
        }
        return false;
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
