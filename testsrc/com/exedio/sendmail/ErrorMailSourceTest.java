package com.exedio.sendmail;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;


public class ErrorMailSourceTest extends TestCase
{
	private ErrorMailSource ep;
	
	protected void setUp() throws Exception
	{
		super.setUp();
		ep = new ErrorMailSource("error-mail-from@test.exedio.com", "error-mail-to@test.exedio.com", "error-subject");
	}
	
	public void testErrorMail()
	{
		assertEquals(list(), ep.getMailsToSend(10));

		final Mail m1 = ep.createMail("test-Text");
		assertEquals("error-mail-from@test.exedio.com", m1.getFrom());
		assertEquals(new String[]{"error-mail-to@test.exedio.com"}, m1.getTo());
		assertEquals(null, m1.getCarbonCopy());
		assertEquals(null, m1.getBlindCarbonCopy());
		assertEquals("error-subject", m1.getSubject());
		assertEquals("test-Text", m1.getText());

		final Mail m2 = ep.createMail(new NullPointerException("test-exception-message"));
		assertEquals("error-mail-from@test.exedio.com", m2.getFrom());
		assertEquals(new String[]{"error-mail-to@test.exedio.com"}, m2.getTo());
		assertEquals(null, m2.getCarbonCopy());
		assertEquals(null, m2.getBlindCarbonCopy());
		assertEquals("error-subject", m2.getSubject());
		final String m2text = m2.getText();
		assertTrue("EXCEPTION_TEXT:"+m2text, m2text.startsWith("java.lang.NullPointerException: test-exception-message\n"));

		assertEquals(list(m1, m2), ep.getMailsToSend(10));
		assertEquals(list(m1), ep.getMailsToSend(1));
		
		m2.notifySent();
		assertEquals(list(m1), ep.getMailsToSend(10));
		
		m1.notifySent();
		assertEquals(list(), ep.getMailsToSend(10));
	}

	protected final static List list()
	{
		return Collections.EMPTY_LIST;
	}

	protected final static List list(final Object o)
	{
		return Collections.singletonList(o);
	}
	
	protected final static List list(final Object o1, final Object o2)
	{
		return Arrays.asList(new Object[]{o1, o2});
	}
	
	protected void assertEquals(final Object[] expected, final Object[] actual)
	{
		if(expected==null && actual==null)
			return;
		
		assertEquals(expected.length, actual.length);
		for(int i = 0; i<expected.length; i++)
			assertEquals(expected[i], actual[i]);
	}
	
}
