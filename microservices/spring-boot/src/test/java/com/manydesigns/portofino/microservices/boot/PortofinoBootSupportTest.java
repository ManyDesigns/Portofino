package com.manydesigns.portofino.microservices.boot;

import com.manydesigns.portofino.modules.DatabaseModule;
import com.manydesigns.portofino.persistence.Persistence;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.springframework.test.util.AssertionErrors.assertEquals;

@SpringBootTest(classes = { PortofinoSupport.class, DatabaseModule.class })
class PortofinoBootSupportTest {

	@Test
	void contextLoads() {
		assertEquals("Persistence status", Persistence.Status.STARTED, persistence.status.getValue());
	}

	@Autowired
	public Persistence persistence;

}