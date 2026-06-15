package databaseControllers;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import common.Order;

class OrderConnectionTest {
	OrderConnection oc;
	
	@BeforeEach
	void setUp() throws Exception {
		OrderConnection.setPassword("Aa123456");
	}
	
	@Test
	void testBookOrder() {
		oc = OrderConnection.getInstance();
		LocalDate orderDate = LocalDate.of(2028,4,6);
		int visitorNumber = 12, usedId = 764937601, orderHour = 14;
		String parkName = "", email="111@sss.com";
		
		Order o = new Order(orderDate, visitorNumber, usedId, parkName, orderHour, email);
		o.setIsSubscribedToTrue();
		o.setParkId(1);
		o.setOrderStatus("approved");
		o.setPlacementDate(LocalDate.now());
		o.setOrderType("private");

		try {
			oc.bookOrder(o);
		} catch(Exception e) {fail(e.getMessage());}
		
		int expectedOrderId = 777798;
		
		assertEquals(expectedOrderId, o.getOrderId());
		assertEquals(expectedOrderId%100000+100000, o.getConfCode());
	}

}
