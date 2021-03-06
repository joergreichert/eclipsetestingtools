package de.abg.jreichert.junit4runner.junit4;

import java.math.BigDecimal;

import junit.framework.Assert;
import junit.framework.TestCase;
import de.abg.jreichert.junit4runner.Subtractor;

public class SubtratorTest extends TestCase {

	public void testSum() {
		Subtractor subtractor = new Subtractor();
		BigDecimal difference = subtractor.subtract(new BigDecimal(7), new BigDecimal(5));
		Assert.assertEquals("expected difference", new BigDecimal(2), difference);
	}
}
