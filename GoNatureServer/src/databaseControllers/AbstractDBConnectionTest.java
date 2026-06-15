package databaseControllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
/*basic test for specific queries*/
class AbstractDBConnectionTest {
	ParkConnection pc;
	@BeforeEach
    void setUp() {
		ParkConnection.setPassword("Aa123456");
    }
	
	@Test
	void testSelectByFields() {
		try {
			pc = ParkConnection.getInstance();
			String sql = pc.selectByFields(new String[] {"park_name"}, new String[] {"is_active"});
			assertEquals("SELECT park_name FROM `park` WHERE is_active = ?;", sql);
		}catch(Exception e) {fail();}
		
	}
	
	@Test
	void testParkNames() {
		String[] p = new String[3];
		p[0] = "Carmel National Park";
		p[1] = "Banias Nature Reserve";
		p[2] = "Ein Gedi Nature Reserve";
		try {
			pc = ParkConnection.getInstance();
			List<String> parkNames = pc.getActiveParksNames();
			for(int i = 0; i < 3; i++)
				assertEquals(p[i], parkNames.get(i));
		}catch(Exception e) {fail(e.getMessage());}	
	}
	
	@Test
	void testParkIdByName() {
		String[] p = new String[4];
		p[0] = "Carmel National Park";
		p[1] = "Banias Nature Reserve";
		p[2] = "Ein Gedi Nature Reserve";
		p[3] = "x";
		try {
			pc = ParkConnection.getInstance();
			int i = 0;
			for(; i < 3; i++) {
				int parkId= pc.getParkIdByName(p[i]);
				assertEquals(i+1, parkId);
			}
			int parkId = pc.getParkIdByName(p[i]);
			assertEquals(-1, parkId);
		}catch(Exception e) {fail(e.getMessage());}
		
	}

}
