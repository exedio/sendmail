
package com.exedio.sendmail;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class PackageTest extends TestCase
{
	public PackageTest(String name)
	{
		super(name);
	}

	public static Test suite()
	{
		TestSuite suite = new TestSuite();
		suite.addTest(new TestSuite(SendMailTest.class));
		suite.addTest(new TestSuite(ErrorMailSourceTest.class));
		suite.addTest(new TestSuite(CompositeMailSourceTest.class));
		return suite;
	}
}
