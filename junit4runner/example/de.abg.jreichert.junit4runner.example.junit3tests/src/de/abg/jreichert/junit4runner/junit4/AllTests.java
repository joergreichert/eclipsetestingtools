package de.abg.jreichert.junit4runner.junit4;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		//$JUnit-BEGIN$
		suite.addTestSuite(MultiplierTest.class);
		suite.addTestSuite(SubtratorTest.class);
		suite.addTestSuite(DividerTest.class);
		suite.addTestSuite(AdderTest.class);
		//$JUnit-END$
		return suite;
	}

}
