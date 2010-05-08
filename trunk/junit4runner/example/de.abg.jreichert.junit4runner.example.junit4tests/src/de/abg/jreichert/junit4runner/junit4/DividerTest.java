package de.abg.jreichert.junit4runner.junit4;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;

import de.abg.jreichert.junit4runner.Divider;

public class DividerTest {

	@Test
	public void testDivide() {
		Divider divider = new Divider();
		BigDecimal quotient = divider.divide(new BigDecimal(45), new BigDecimal(9));
		Assert.assertEquals("expected quotient", new BigDecimal(5), quotient);
	}
}
