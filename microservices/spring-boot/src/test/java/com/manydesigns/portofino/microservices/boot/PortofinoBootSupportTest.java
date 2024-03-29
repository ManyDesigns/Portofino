package com.manydesigns.portofino.microservices.boot;

import com.manydesigns.elements.blobs.BlobManager;
import com.manydesigns.elements.blobs.HierarchicalBlobManager;
import com.manydesigns.portofino.ResourceActionsModule;
import com.manydesigns.portofino.microservices.boot.support.PortofinoDispatcherSupport;
import com.manydesigns.portofino.microservices.boot.support.PortofinoSupport;
import com.manydesigns.portofino.modules.CrudModule;
import com.manydesigns.portofino.modules.DatabaseModule;
import com.manydesigns.portofino.modules.H2Module;
import com.manydesigns.portofino.persistence.Persistence;
import org.h2.tools.RunScript;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.groovy.template.GroovyTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
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
import static org.springframework.test.util.AssertionErrors.assertTrue;

@SpringBootTest(
		classes = {
				PortofinoSupport.class, DatabaseModule.class, H2Module.class,
				ElementsAutoConfiguration.class,
				PortofinoDispatcherSupport.class, PortofinoJerseyAutoConfiguration.class, ResourceActionsModule.class,
				CrudModule.class
		},
		properties = {
				"portofino.dispatcher.enabled=true", "spring.jersey.type=filter", "spring.jersey.application-path=/"
		},
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = { ErrorMvcAutoConfiguration.class, GroovyTemplateAutoConfiguration.class })
class PortofinoBootSupportTest {

	@LocalServerPort
	public int port;

	@Autowired
	public BlobManager blobManager;

	@Test
	void contextLoads() {
		assertEquals("Persistence status", Persistence.Status.STARTED, persistence.status.getValue());
		assertTrue("Default blob manager is injected", blobManager instanceof HierarchicalBlobManager);
	}

	@Test
	public void canRunQuery() throws Exception {
		setupTestDb();

		Map<String, Object> testItemData = new HashMap<>();
		testItemData.put("testo", "esempio");
		Session session = persistence.getSession("hibernatetest");
		session.save("table1", testItemData);
		session.getTransaction().commit();

		List table1 = session.createQuery("from table1").list();
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
	}

	@Test
	public void canInvokeRESTResources() throws Exception {
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target("http://localhost:" + port + "/:auth");
		Response response = target.request().get();
		assertEquals("HTTP OK", 200, response.getStatus());

		setupTestDb();
		target = client.target("http://localhost:" + port + "/crud");
		response = target.request().get();
		assertEquals("HTTP OK", 200, response.getStatus());
		JSONObject body = new JSONObject(response.readEntity(String.class));
		assertEquals("Records returned", 10, body.getInt("recordsReturned"));
		assertEquals("Total records", 11, body.getInt("totalRecords"));

		assertEquals("Resource doesn't exist", 404,
				client.target("http://localhost:" + port + "/fake").request().get().getStatus());
	}

	@Autowired
	public Persistence persistence;

}