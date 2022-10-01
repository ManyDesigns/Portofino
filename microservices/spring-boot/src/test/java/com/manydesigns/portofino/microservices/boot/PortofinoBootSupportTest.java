package com.manydesigns.portofino.microservices.boot;

import com.manydesigns.portofino.modules.DatabaseModule;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = { PortofinoSupport.class, DatabaseModule.class })
class PortofinoBootSupportTest {

	@Test
	void contextLoads() {}

}