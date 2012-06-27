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

import java.sql.Date;

import javax.mail.internet.AddressException;

import junit.framework.TestCase;

public class MailDataTest extends TestCase
{
	public void testDate() throws AddressException
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

	public void testReceipents() throws AddressException
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
			// ok
		}
		try
		{
			d.addCarbonCopy(null);
			fail();
		}
		catch(final NullPointerException e)
		{
			// ok
		}
		try
		{
			d.addBlindCarbonCopy(null);
			fail();
		}
		catch(final NullPointerException e)
		{
			// ok
		}
		try
		{
			d.addReplyTo(null);
			fail();
		}
		catch(final NullPointerException e)
		{
			// ok
		}
	}

	public void testAttachement() throws AddressException
	{
		final MailData d = new MailData("from", "subject");
		d.addAttachement(new AssertionFailedDataSource());

		try
		{
			d.addAttachement(null);
			fail();
		}
		catch(final NullPointerException e)
		{
			// ok
		}
	}

	public void testMessageID() throws AddressException
	{
		final MailData d = new MailData("from", "subject");
		d.setMessageID("message-id");
		d.setMessageID(null);
	}
}
