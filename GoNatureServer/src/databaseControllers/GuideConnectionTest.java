package databaseControllers;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GuideConnectionTest {
	GuideConnection gc;
	
	@BeforeEach
	void setUp() throws Exception {
		GuideConnection.setPassword("Aa123456");
	}

	@Test
	void testIsActiveGuide() {
		try {
			gc = GuideConnection.getInstance();
			assertTrue(gc.isActiveGuide(345672184) != null);
		}catch(Exception e) {fail();}
	}

}
