/*
 * Copyright (C) 2004-2009  exedio GmbH (www.exedio.com)
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

import static java.lang.System.lineSeparator;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ErrorMailSourceTest
{
	@SuppressWarnings("deprecation") // OK: testing MailSource API
	private ErrorMailSource ep;

	@SuppressWarnings("deprecation") // OK: testing MailSource API
	@BeforeEach
	void setUp()
	{
		ep = new ErrorMailSource("error-mail-from@test.exedio.com", "error-mail-to@test.exedio.com", "error-subject", 3);
	}

	private static void assertText(final String expectedText, final Mail actualMail)
	{
		final String actualText = actualMail.getTextPlain();
		assertTrue(actualText.indexOf(expectedText) > 0, "TEXT:" + actualText);
	}

	@Test
	void testErrorMail()
	{
		assertEquals(list(), ep.getMailsToSend(10));

		final Mail m1 = ep.createMail("test-Text");
		assertEquals("error-mail-from@test.exedio.com", m1.getFrom());
		assertArrayEquals(new String[]{"error-mail-to@test.exedio.com"}, m1.getTo());
		assertArrayEquals(null, m1.getCarbonCopy());
		assertArrayEquals(null, m1.getBlindCarbonCopy());
		assertEquals("error-subject", m1.getSubject());
		assertText("test-Text", m1);

		final Mail m2 = ep.createMail(new NullPointerException("test-exception-message"));
		assertEquals("error-mail-from@test.exedio.com", m2.getFrom());
		assertArrayEquals(new String[]{"error-mail-to@test.exedio.com"}, m2.getTo());
		assertArrayEquals(null, m2.getCarbonCopy());
		assertArrayEquals(null, m2.getBlindCarbonCopy());
		assertEquals("error-subject", m2.getSubject());
		assertText("java.lang.NullPointerException: test-exception-message"+lineSeparator(), m2);

		final Mail m3 = ep.createMail("test3-Text", new NullPointerException("test3-exception-message"));
		assertEquals("error-mail-from@test.exedio.com", m3.getFrom());
		assertArrayEquals(new String[]{"error-mail-to@test.exedio.com"}, m3.getTo());
		assertArrayEquals(null, m3.getCarbonCopy());
		assertArrayEquals(null, m3.getBlindCarbonCopy());
		assertEquals("error-subject", m3.getSubject());
		//noinspection HardcodedLineSeparator
		assertText("test3-Text\njava.lang.NullPointerException: test3-exception-message"+lineSeparator(), m3);

		assertEquals(list(m1, m2, m3), ep.getMailsToSend(10));
		assertEquals(list(m1), ep.getMailsToSend(1));

		m2.notifySent();
		assertEquals(list(m1, m3), ep.getMailsToSend(10));

		m1.notifySent();
		assertEquals(list(m3), ep.getMailsToSend(10));
	}

	@Test
	void testOverflow()
	{
		assertEquals(list(), ep.getMailsToSend(10));

		final Mail m1 = ep.createMail("test overflow 1");
		assertEquals("error-subject", m1.getSubject());
		assertText("test overflow 1", m1);
		assertEquals(list(m1), ep.getMailsToSend(10));
		assertEquals(0, ep.getOverflowCount());

		final Mail m2 = ep.createMail("test overflow 2");
		assertEquals("error-subject", m2.getSubject());
		assertText("test overflow 2", m2);
		assertEquals(list(m1, m2), ep.getMailsToSend(10));
		assertEquals(0, ep.getOverflowCount());

		final Mail m3 = ep.createMail("test overflow 3");
		assertEquals("error-subject", m3.getSubject());
		assertText("test overflow 3", m3);
		assertEquals(list(m1, m2, m3), ep.getMailsToSend(10));
		assertEquals(0, ep.getOverflowCount());

		assertEquals(null, ep.createMail("test overflow 4"));
		assertEquals(list(m1, m2, m3), ep.getMailsToSend(10));
		assertEquals(1, ep.getOverflowCount());

		assertEquals(null, ep.createMail(new NullPointerException("test overflow 5")));
		assertEquals(list(m1, m2, m3), ep.getMailsToSend(10));
		assertEquals(2, ep.getOverflowCount());

		m1.notifySent();
		assertEquals(list(m2, m3), ep.getMailsToSend(10));
		assertEquals(2, ep.getOverflowCount());

		final Mail m4 = ep.createMail("test overflow 4");
		assertEquals("error-subject (ov2)", m4.getSubject());
		assertText("test overflow 4", m4);
		assertEquals(list(m2, m3, m4), ep.getMailsToSend(10));
		assertEquals(2, ep.getOverflowCount());
	}

	protected static final List<Object> list(final Object... o)
	{
		return Arrays.asList(o);
	}

	@Test
	void testSubject()
	{
		final Mail m1 = ep.createMail("text");
		assertEquals( "error-subject", m1.getSubject() );
		m1.notifySent();

		final Mail m2 = ep.createMailWithSubject("subject", "text");
		//noinspection ConstantConditions
		assertEquals( "subject", m2.getSubject() );
		m2.notifySent();

		final Mail m3 = ep.createMail("text", new Exception());
		assertEquals( "error-subject", m3.getSubject() );
		m3.notifySent();

		final Mail m4 = ep.createMailWithSubject("xyz", "text", new Exception());
		assertEquals( "xyz", m4.getSubject() );
		m4.notifySent();

		final Mail m5 = ep.createMail(new Exception());
		assertEquals( "error-subject", m5.getSubject() );
		m5.notifySent();

		final Mail m6 = ep.createMailWithSubject("abc", new Exception());
		assertEquals( "abc", m6.getSubject() );
		m6.notifySent();
	}

	@Test
	void testSubjectOverflow()
	{
		final Mail m1 = ep.createMail("fill up");
		ep.createMail("fill up");
		ep.createMail("fill up");

		assertEquals( null, ep.createMail("overflow") );

		m1.notifySent();
		final Mail m4 = ep.createMailWithSubject("m4", "text m4");
		//noinspection ConstantConditions
		assertEquals( "m4 (ov1)", m4.getSubject());
	}

}
