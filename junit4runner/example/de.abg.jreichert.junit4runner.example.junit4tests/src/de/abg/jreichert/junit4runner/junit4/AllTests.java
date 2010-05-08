package de.abg.jreichert.junit4runner.junit4;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(value = Suite.class)
@SuiteClasses(value = {
		// $JUnit-BEGIN$
		AdderTest.class, DividerTest.class, MultiplierTest.class,
		SubtractorTest.class
// $JUnit-END$
})
public class AllTests {
}
