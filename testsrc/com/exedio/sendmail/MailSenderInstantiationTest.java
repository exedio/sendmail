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

import junit.framework.TestCase;

public class MailSenderInstantiationTest extends TestCase
{
	public void testIt() throws Exception
	{
		final MailSender ms = new MailSender("host", 123, 456, false);
		assertEquals("host", ms.getHost());
		assertEquals(123, ms.getConnectTimeout());
		assertEquals(456, ms.getReadTimeout());
		assertEquals(false, ms.isDebug());
		assertEquals(false, ms.isSSL());
		assertEquals(false, ms.isEnableStarttls());

		try
		{
			new MailSender(null, -1, -1, true);
		}
		catch(final IllegalArgumentException e)
		{
			assertEquals("host must not be null", e.getMessage());
		}
		try
		{
			new MailSender("host", -1, -1, -1, true, "authUser", "authPassword");
		}
		catch(final IllegalArgumentException e)
		{
			assertEquals("port must not be negative", e.getMessage());
		}
		try
		{
			new MailSender("host", -1, -1, true);
		}
		catch(final IllegalArgumentException e)
		{
			assertEquals("connectTimeout must not be negative", e.getMessage());
		}
		try
		{
			new MailSender("host", 0, -1, true);
		}
		catch(final IllegalArgumentException e)
		{
			assertEquals("readTimeout must not be negative", e.getMessage());
		}
		try
		{
			new MailSender("host", 0,  true, true, 0, 1, true, "", "");
		}
		catch(final IllegalArgumentException e)
		{
			assertEquals("ssl is expected to be false if enableStarttls is true", e.getMessage());
		}
	}
}
