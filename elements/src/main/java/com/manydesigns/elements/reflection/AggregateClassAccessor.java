package com.manydesigns.elements.reflection;

import com.manydesigns.elements.util.ReflectionUtil;
import org.apache.commons.lang.StringUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class AggregateClassAccessor implements ClassAccessor {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    protected final List<ClassAccessor> accessors;
    protected final List<String> aliases;
    protected String name;
    protected Class<? extends List> collectionClass = ArrayList.class;

    public AggregateClassAccessor(List<ClassAccessor> accessors) {
        this.accessors = accessors;
        aliases = Arrays.asList(new String[accessors.size()]);
    }

    public AggregateClassAccessor(ClassAccessor... accessors) {
        this(Arrays.asList(accessors));
    }

    public void alias(int index, String alias) {
        aliases.set(index, alias);
    }

    public List<ClassAccessor> getAccessors() {
        return accessors;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<? extends List> getCollectionClass() {
        return collectionClass;
    }

    public void setCollectionClass(Class<? extends List> collectionClass) {
        this.collectionClass = collectionClass;
    }

    @Override
    public PropertyAccessor getProperty(String propertyName) throws NoSuchFieldException {
        int index = 0;
        for(ClassAccessor accessor : accessors) {
            try {
                return new PropertyAccessorWrapper(accessor, accessor.getProperty(propertyName), index);
            } catch (NoSuchFieldException e) {}
            index++;
        }
        throw new NoSuchFieldException(propertyName);
    }

    @Override
    public PropertyAccessor[] getProperties() {
        List<PropertyAccessor> allProperties = new ArrayList<PropertyAccessor>();
        int index = 0;
        for(ClassAccessor accessor : getAccessors()) {
            PropertyAccessor[] properties = accessor.getProperties();
            PropertyAccessor[] wrappers = new PropertyAccessor[properties.length];
            for(int i = 0; i < properties.length; i++) {
                wrappers[i] = new PropertyAccessorWrapper(accessor, properties[i], index);
            }
            Collections.addAll(allProperties, wrappers);
            index++;
        }
        return allProperties.toArray(new PropertyAccessor[allProperties.size()]);
    }

    @Override
    public PropertyAccessor[] getKeyProperties() {
        List<PropertyAccessor> allProperties = new ArrayList<PropertyAccessor>();
        int index = 0;
        for(ClassAccessor accessor : getAccessors()) {
            PropertyAccessor[] properties = accessor.getKeyProperties();
            PropertyAccessor[] wrappers = new PropertyAccessor[properties.length];
            for(int i = 0; i < properties.length; i++) {
                wrappers[i] = new PropertyAccessorWrapper(accessor, properties[i], index);
            }
            Collections.addAll(allProperties, wrappers);
            index++;
        }
        return allProperties.toArray(new PropertyAccessor[allProperties.size()]);
    }

    @Override
    public Object newInstance() {
        if(collectionClass != null) {
            List list = (List) ReflectionUtil.newInstance(collectionClass);
            for(ClassAccessor accessor : accessors) {
                list.add(accessor.newInstance());
            }
            return list;
        } else {
            Object[] objects = new Object[accessors.size()];
            int i = 0;
            for(ClassAccessor accessor : accessors) {
                objects[i++] = (accessor.newInstance());
            }
            return objects;
        }
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        for(ClassAccessor accessor : accessors) {
            if(accessor.isAnnotationPresent(annotationClass)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        for(ClassAccessor accessor : accessors) {
            if(accessor.isAnnotationPresent(annotationClass)) {
                return accessor.getAnnotation(annotationClass);
            }
        }
        return null;
    }

    @Override
    public Annotation[] getAnnotations() {
        List<Annotation> allAnnotations = new ArrayList<Annotation>();
        for(ClassAccessor accessor : getAccessors()) {
            Annotation[] annotations = accessor.getAnnotations();
            Collections.addAll(allAnnotations, annotations);
        }
        return allAnnotations.toArray(new Annotation[allAnnotations.size()]);
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        List<Annotation> allAnnotations = new ArrayList<Annotation>();
        for(ClassAccessor accessor : getAccessors()) {
            Annotation[] annotations = accessor.getDeclaredAnnotations();
            Collections.addAll(allAnnotations, annotations);
        }
        return allAnnotations.toArray(new Annotation[allAnnotations.size()]);
    }

    public String getAccessorName(ClassAccessor classAccessor) {
        return getAccessorName(classAccessor, accessors.indexOf(classAccessor));
    }

    public String getAccessorName(int index) {
        return getAccessorName(accessors.get(index), index);
    }

    protected String getAccessorName(ClassAccessor classAccessor, int index) {
        String alias = aliases.get(index);
        if(alias != null) {
            return alias;
        }
        return StringUtils.defaultString(
                classAccessor.getName(),
                StringUtils.defaultString(name) + "[" + index + "]");
    }

    public class PropertyAccessorWrapper implements PropertyAccessor {

        protected final ClassAccessor classAccessor;
        protected final PropertyAccessor delegate;
        protected final int index;

        public PropertyAccessorWrapper(ClassAccessor accessor, PropertyAccessor propertyAccessor, int index) {
            this.classAccessor = accessor;
            this.delegate = propertyAccessor;
            this.index = index;
        }

        @Override
        public String getName() {
            String accessorName = getAccessorName(classAccessor, index);
            return accessorName + "." + delegate.getName();
        }

        @Override
        public Class getType() {
            return delegate.getType();
        }

        @Override
        public int getModifiers() {
            return delegate.getModifiers();
        }

        protected Object getObject(Object collection) {
            if(collection instanceof List) {
                return ((List) collection).get(index);
            } else {
                return ((Object[]) collection)[index];
            }
        }

        @Override
        public Object get(Object obj) {
            return delegate.get(getObject(obj));
        }

        @Override
        public void set(Object obj, Object value) {
            delegate.set(getObject(obj), value);
        }

        @Override
        public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
            return delegate.isAnnotationPresent(annotationClass);
        }

        @Override
        public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
            return delegate.getAnnotation(annotationClass);
        }

        @Override
        public Annotation[] getAnnotations() {
            return delegate.getAnnotations();
        }

        @Override
        public Annotation[] getDeclaredAnnotations() {
            return delegate.getDeclaredAnnotations();
        }
    }

}
