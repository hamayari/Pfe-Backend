package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test d'intégration - Vérifie que le contexte Spring démarre correctement
 * Utilise MongoDB embarqué (Flapdoodle) configuré dans application-test.properties
 * Aucune dépendance externe nécessaire
 */
@SpringBootTest
@ActiveProfiles("test")
class DemoApplicationTests {

	@Test
	void contextLoads() {
		// Ce test vérifie que le contexte Spring démarre correctement
		// avec MongoDB embarqué (Flapdoodle)
	}

}
