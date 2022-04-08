package com.manydesigns.portofino.database;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.jdbc.Work;
import org.hibernate.service.ServiceRegistry;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

@Test
public class HibernateTest {

    @Test
    public void testImplicitCompositeIdInDynamicMapMode() {
        HashMap<String, Object> settings = new HashMap<>();
        settings.put("hibernate.connection.url", "jdbc:h2:mem:hbtest");
        ServiceRegistry standardRegistry =
                new StandardServiceRegistryBuilder().applySettings(settings).build();

        MetadataSources sources = new MetadataSources(standardRegistry);
        sources.addResource("/com/manydesigns/portofino/database/hibernate/CompId.hbm.xml");

        Metadata metadata = sources.buildMetadata();
        SessionFactory sessionFactory = metadata.buildSessionFactory();
        Session session = sessionFactory.openSession();
        session.doWork(new Work() {
            @Override
            public void execute(Connection connection) throws SQLException {
                connection.createStatement().execute(
                        "create table CompId (id1 varchar(32), id2 varchar (32), primary key (id1, id2))");
                connection.createStatement().execute("insert into CompId values ('1', '2')");
                connection.commit();
            }
        });
        HashMap<Object, Object> id = new HashMap<>();
        id.put("id1", "1");
        id.put("id2", "2");
        session.get("CompId", id);
    }

}
