package de.abg.jreichert.junit4runner.junit4;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;

import de.abg.jreichert.junit4runner.Multiplier;

public class MultiplierTest {

	@Test
	public void testSum() {
		Multiplier multiplier = new Multiplier();
		BigDecimal product = multiplier.multiply(new BigDecimal(5), new BigDecimal(7));
		Assert.assertEquals("expected product", new BigDecimal(35), product);
	}
}
