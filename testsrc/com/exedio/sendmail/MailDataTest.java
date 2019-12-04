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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.Date;
import javax.mail.internet.AddressException;
import org.junit.jupiter.api.Test;

public class MailDataTest
{
	@Test
	void testCreate() throws AddressException
	{
		new MailData("from", "subject");

		try
		{
			new MailData(null, "subject");
			fail();
		}
		catch(final NullPointerException e)
		{
			assertEquals("from", e.getMessage());
		}
		try
		{
			new MailData("from", null);
			fail();
		}
		catch(final NullPointerException e)
		{
			assertEquals("subject", e.getMessage());
		}
	}

	@Test
	void testDate() throws AddressException
	{
		final MailData d = new MailData("from", "subject");
		assertEquals(null, d.getDate());

		final Date DATE = new Date(1234567);
		d.setDate(DATE);
		assertEquals(DATE, d.getDate());
		assertNotSame(DATE, d.getDate());

		final Date DATE2 = new Date(DATE.getTime());
		DATE.setTime(7654321);
		assertEquals(DATE2, d.getDate());
		assertNotSame(DATE, d.getDate());
		assertNotSame(DATE2, d.getDate());

		d.setDate(null);
		assertEquals(null, d.getDate());
	}

	@Test
	void testReceipents() throws AddressException
	{
		final MailData d = new MailData("from", "subject");
		d.addTo("to");
		d.addCarbonCopy("cc");
		d.addBlindCarbonCopy("bcc");
		d.addReplyTo("replyTo");

		try
		{
			d.addTo(null);
			fail();
		}
		catch(final NullPointerException e)
		{
			assertEquals(null, e.getMessage());
		}
		try
		{
			d.addCarbonCopy(null);
			fail();
		}
		catch(final NullPointerException e)
		{
			assertEquals(null, e.getMessage());
		}
		try
		{
			d.addBlindCarbonCopy(null);
			fail();
		}
		catch(final NullPointerException e)
		{
			assertEquals(null, e.getMessage());
		}
		try
		{
			d.addReplyTo(null);
			fail();
		}
		catch(final NullPointerException e)
		{
			assertEquals(null, e.getMessage());
		}
	}

	@Test
	void testAttachment() throws AddressException
	{
		final MailData d = new MailData("from", "subject");
		d.addAttachment(new AssertionFailedDataSource());

		try
		{
			d.addAttachment(null);
			fail();
		}
		catch(final NullPointerException e)
		{
			assertEquals(null, e.getMessage());
		}
	}

	@Test
	void testMessageID() throws AddressException
	{
		final MailData d = new MailData("from", "subject");
		d.setMessageID("message-id");
		d.setMessageID(null);
	}

	@Test
	void testText() throws AddressException
	{
		final MailData d = new MailData("from", "subject");
		d.setTextPlain("textPlain");
		d.setTextHtml("textHtml");
		d.setTextPlain(null);
		d.setTextHtml(null);
	}

	@Test
	public void testMailingListHeaderData() throws AddressException
	{
		final MailData d = new MailData("from", "subject");
		assertNotNull(d.mailingListHeaderData());
	}
}
