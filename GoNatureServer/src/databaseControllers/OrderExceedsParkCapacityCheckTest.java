package databaseControllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import common.Order;

class OrderExceedsParkCapacityCheckTest {
	ParkConnection pc;
	OrderConnection oc;
	
	@BeforeEach
	void setUp() throws Exception {
		String p = "Aa123456";
		ParkConnection.setPassword(p);
		OrderConnection.setPassword(p);
	}

	@Test
	void testCheck() {
		try {
			pc = ParkConnection.getInstance();
			oc = OrderConnection.getInstance();
			OrderExceedsParkCapacityCheck occ = OrderExceedsParkCapacityCheck.getInstance(pc, oc);
			
			LocalDate orderDate = LocalDate.of(2028,4,6);
			int visitorNumber = 13, usedId = 1, orderHour = 14;
			String parkName = "", email="";
			
			Order o = new Order(orderDate, visitorNumber, usedId, parkName, orderHour, email);
			o.setParkId(1);
			
			int ret = occ.check(null);
			assertEquals(-1, ret);
			ret = occ.check(o);
			assertEquals(1, ret);
			o.setNumberOfVisitors(12);
			ret = occ.check(o);
			assertEquals(0, ret);
		}catch(Exception e) {fail();}
	}

}
