package de.abg.jreichert.junit4runner.junit4;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;

import de.abg.jreichert.junit4runner.Adder;

public class AdderTest {

	@Test
	public void testSum() {
		Adder adder = new Adder();
		BigDecimal sum = adder.add(new BigDecimal(5), new BigDecimal(7));
		Assert.assertEquals("expected sum", new BigDecimal(12), sum);
	}
}
