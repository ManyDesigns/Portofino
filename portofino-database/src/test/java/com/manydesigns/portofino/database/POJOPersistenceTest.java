package com.manydesigns.portofino.database;

import com.manydesigns.portofino.persistence.hibernate.SessionFactoryBuilder;
import org.hibernate.EntityMode;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Map;

@Test
public class POJOPersistenceTest extends PersistenceTest {

    @BeforeMethod
    @Override
    public void setup() throws Exception {
        super.setup();
        persistence.getModel().getDatabases().forEach(d -> {
            d.setEntityMode(EntityMode.POJO.getExternalName());
        });
        persistence.initModel();
    }

    @Override
    protected Serializable makeEntity(String className, Map<String, Object> data) {
        Object entity;
        try {
            String databaseName = className.substring(0, className.indexOf('.'));
            String actualClassName = SessionFactoryBuilder.ensureValidJavaName(className);
            Class entityClass = persistence.getDatabaseSetup(databaseName).getCodeBase().loadClass(actualClassName);
            entity = entityClass.getConstructor().newInstance();
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                Field field = entityClass.getDeclaredField(entry.getKey());
                field.setAccessible(true);
                field.set(entity, entry.getValue());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return (Serializable) entity;
    }

    @Override
    protected void set(Object entity, String property, Object value) {
        try {
            Field field = entity.getClass().getDeclaredField(property);
            field.setAccessible(true);
            field.set(entity, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected <T> T get(Object entity, String property) {
        try {
            Field field = entity.getClass().getDeclaredField(property);
            field.setAccessible(true);
            return (T) field.get(entity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
