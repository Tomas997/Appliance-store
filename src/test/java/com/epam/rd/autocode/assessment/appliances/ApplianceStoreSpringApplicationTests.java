package com.epam.rd.autocode.assessment.appliances;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
		"JWT_SECRET=dGVzdC1vbmx5LXNlY3JldC1rZXktZG8tbm90LXVzZS1pbi1wcm9kdWN0aW9uLTEyMzQ1Ng==",
		"ADMIN_EMAIL=admin@test.local",
		"ADMIN_PASSWORD=TestAdminPass1",
		"spring.datasource.url=jdbc:h2:mem:contextLoadsTestDb;DB_CLOSE_DELAY=-1"
})
class ApplianceStoreSpringApplicationTests {

	@Test
	void contextLoads() {
	}

}
