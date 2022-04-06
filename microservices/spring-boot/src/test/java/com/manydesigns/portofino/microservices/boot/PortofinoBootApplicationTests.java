package com.manydesigns.portofino.microservices.boot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"spring.jersey.application-path", "/api/"})
class PortofinoBootApplicationTests {

	@Test
	void contextLoads() {
	}

}
