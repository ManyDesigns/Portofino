package com.manydesigns.elements.reflection;

import com.manydesigns.elements.util.ReflectionUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
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
            "Copyright (C) 2005-2019 ManyDesigns srl";

    protected final List<ClassAccessor> accessors;
    protected final List<String> aliases;
    protected String name;
    protected Class<? extends List> collectionClass = ArrayList.class;
    protected PropertyAccessor[] properties;
    protected PropertyAccessor[] keyProperties;

    public AggregateClassAccessor(List<ClassAccessor> accessors) {
        this.accessors = accessors;
        aliases = Arrays.asList(new String[accessors.size()]);
        computeProperties();
    }

    public AggregateClassAccessor(ClassAccessor... accessors) {
        this(Arrays.asList(accessors));
    }

    public void alias(int index, String alias) {
        aliases.set(index, alias);
        computeProperties();
    }

    public String getAlias(int index) {
        return aliases.get(index);
    }

    public List<ClassAccessor> getAccessors() {
        return accessors;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<?> getType() {
        return collectionClass != null ? collectionClass : Object[].class;
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

    protected void computeProperties() {
        List<PropertyAccessor> allProperties = new ArrayList<PropertyAccessor>();
        int index = 0;
        for(ClassAccessor accessor : getAccessors()) {
            String alias = aliases.get(index);
            if(alias != null) {
                allProperties.add(new AliasPropertyAccessor(alias, index, accessor));
            }

            PropertyAccessor[] properties = accessor.getProperties();
            PropertyAccessor[] wrappers = new PropertyAccessor[properties.length];
            for(int i = 0; i < properties.length; i++) {
                wrappers[i] = new PropertyAccessorWrapper(accessor, properties[i], index);
            }
            Collections.addAll(allProperties, wrappers);
            index++;
        }
        properties = allProperties.toArray(new PropertyAccessor[allProperties.size()]);
        computeKeyProperties();
    }

    protected void computeKeyProperties() {
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
        keyProperties = allProperties.toArray(new PropertyAccessor[allProperties.size()]);
    }

    @Override
    public PropertyAccessor getProperty(String propertyName) throws NoSuchFieldException {
        for(PropertyAccessor propertyAccessor : properties) {
            if(propertyAccessor.getName().equals(propertyName)) {
                return propertyAccessor;
            }
        }
        throw new NoSuchFieldException(propertyName);
    }

    @Override
    public PropertyAccessor[] getProperties() {
        return properties;
    }

    @Override
    public PropertyAccessor[] getKeyProperties() {
        return keyProperties;
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
            String accessorName;
            String alias = aliases.get(index);
            if(alias != null) {
                accessorName = alias;
            } else {
                accessorName = "[" + index + "]";
            }
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

    public class AliasPropertyAccessor implements PropertyAccessor {

        protected final String name;
        protected final int index;
        protected final ClassAccessor accessor;

        public AliasPropertyAccessor(String name, int index, ClassAccessor accessor) {
            this.name = name;
            this.index = index;
            this.accessor = accessor;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Class getType() {
            return accessor.getType();
        }

        @Override
        public int getModifiers() {
            return Modifier.PUBLIC;
        }

        @Override
        public Object get(Object obj) {
            if(obj instanceof List) {
                return ((List) obj).get(index);
            } else {
                return ((Object[]) obj)[index];
            }
        }

        @Override
        public void set(Object obj, Object value) {
            if(obj instanceof List) {
                ((List) obj).set(index, value);
            } else {
                ((Object[]) obj)[index] = value;
            }
        }

        @Override
        public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
            return accessor.isAnnotationPresent(annotationClass);
        }

        @Override
        public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
            return accessor.getAnnotation(annotationClass);
        }

        @Override
        public Annotation[] getAnnotations() {
            return accessor.getAnnotations();
        }

        @Override
        public Annotation[] getDeclaredAnnotations() {
            return accessor.getDeclaredAnnotations();
        }
    }

}
