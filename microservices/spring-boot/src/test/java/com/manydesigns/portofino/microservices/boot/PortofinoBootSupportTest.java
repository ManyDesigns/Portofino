package com.manydesigns.portofino.microservices.boot;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.portofino.ResourceActionsModule;
import com.manydesigns.portofino.microservices.boot.support.PortofinoDispatcherSupport;
import com.manydesigns.portofino.microservices.boot.support.PortofinoSupport;
import com.manydesigns.portofino.model.service.ModelModule;
import com.manydesigns.portofino.modules.DatabaseModule;
import com.manydesigns.portofino.modules.H2Module;
import com.manydesigns.portofino.persistence.Persistence;
import org.h2.tools.RunScript;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertNotNull;

@SpringBootTest(
		classes = {
				PortofinoSupport.class, ModelModule.class, DatabaseModule.class, H2Module.class,
				PortofinoDispatcherSupport.class, ResourceActionsModule.class
		},
		properties = {
				"portofino.dispatcher.enabled=true", "spring.jersey.type=filter", "spring.jersey.application-path=/"
		},
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@EnableAutoConfiguration
class PortofinoBootSupportTest {

	@LocalServerPort
	public int port;

	@Test
	void contextLoads() {
		assertEquals("Persistence status", Persistence.Status.STARTED, persistence.status.getValue());
	}

	@Test
	public void canRunQuery() throws Exception {
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

	@Test
	public void canInvokeRootResource() {
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target("http://localhost:" + port);
		Response response = target.request().get();
		assertEquals("HTTP OK", 200, response.getStatus());
	}

	@Autowired
	public Persistence persistence;

}