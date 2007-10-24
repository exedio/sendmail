/*
 * Copyright (C) 2004-2007  exedio GmbH (www.exedio.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.exedio.sendmail;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;


public class ErrorMailSourceTest extends TestCase
{
	private ErrorMailSource ep;
	
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		ep = new ErrorMailSource("error-mail-from@test.exedio.com", "error-mail-to@test.exedio.com", "error-subject", 3);
	}
	
	private static final void assertText(final String expectedText, final Mail actualMail)
	{
		final String actualText = actualMail.getTextPlain();
		assertTrue("TEXT:"+actualText, actualText.indexOf(expectedText)>0);
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
		assertText("test-Text", m1);

		final Mail m2 = ep.createMail(new NullPointerException("test-exception-message"));
		assertEquals("error-mail-from@test.exedio.com", m2.getFrom());
		assertEquals(new String[]{"error-mail-to@test.exedio.com"}, m2.getTo());
		assertEquals(null, m2.getCarbonCopy());
		assertEquals(null, m2.getBlindCarbonCopy());
		assertEquals("error-subject", m2.getSubject());
		assertText("java.lang.NullPointerException: test-exception-message"+System.getProperty("line.separator"), m2);

		assertEquals(list(m1, m2), ep.getMailsToSend(10));
		assertEquals(list(m1), ep.getMailsToSend(1));
		
		m2.notifySent();
		assertEquals(list(m1), ep.getMailsToSend(10));
		
		m1.notifySent();
		assertEquals(list(), ep.getMailsToSend(10));
	}
	
	public void testOverflow()
	{
		assertEquals(list(), ep.getMailsToSend(10));

		final Mail m1 = ep.createMail("test overflow 1");
		assertText("test overflow 1", m1);
		assertEquals(list(m1), ep.getMailsToSend(10));

		final Mail m2 = ep.createMail("test overflow 2");
		assertText("test overflow 2", m2);
		assertEquals(list(m1, m2), ep.getMailsToSend(10));

		final Mail m3 = ep.createMail("test overflow 3");
		assertText("test overflow 3", m3);
		assertEquals(list(m1, m2, m3), ep.getMailsToSend(10));

		assertEquals(null, ep.createMail("test overflow 4"));
		assertEquals(list(m1, m2, m3), ep.getMailsToSend(10));

		assertEquals(null, ep.createMail(new NullPointerException("test overflow 5")));
		assertEquals(list(m1, m2, m3), ep.getMailsToSend(10));
	}

	protected final static List<Object> list(final Object... o)
	{
		return Arrays.asList(o);
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
