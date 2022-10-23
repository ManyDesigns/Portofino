package com.manydesigns.portofino.microservices.boot;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.portofino.model.service.ModelModule;
import com.manydesigns.portofino.modules.DatabaseModule;
import com.manydesigns.portofino.modules.H2Module;
import com.manydesigns.portofino.persistence.Persistence;
import org.h2.tools.RunScript;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertNotNull;

@SpringBootTest(classes = { PortofinoSupport.class, ModelModule.class, DatabaseModule.class, H2Module.class })
class PortofinoBootSupportTest {

	@Test
	void contextLoads() {
		assertEquals("Persistence status", Persistence.Status.STARTED, persistence.status.getValue());
	}

	@Test
	void canRunQuery() throws Exception {
		ElementsThreadLocals.setupDefaultElementsContext();
		setupTestDb();

		Map<String, Object> testItemData = new HashMap<>();
		testItemData.put("testo", "esempio");
		Session session = persistence.getSession("hibernatetest");
		session.persist("table1", testItemData);
		session.getTransaction().commit();

		List<?> table1 = session.createQuery("from table1", Object.class).list();
		assertEquals("Saved one entity", 12, table1.size());
	}

	private void setupTestDb() throws Exception {
		Session session = persistence.getSession("hibernatetest");
		session.doWork(new Work() {
			@Override
			public void execute(Connection connection) throws SQLException {
				InputStreamReader reader =
						new InputStreamReader(
								getClass().getResourceAsStream("/portofino/hibernatetest.sql"));
				RunScript.execute(connection, reader);
			}
		});
		session.getTransaction().commit();
		persistence.syncDataModel("hibernatetest");
		persistence.initModel();
		assertEquals("Database created",  1, persistence.getDatabases().size());
		assertNotNull("Table1 entity", persistence.getTableAccessor("hibernatetest", "table1"));
	}

	@Autowired
	public Persistence persistence;

}