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

import static com.exedio.cope.util.Sources.view;
import static com.exedio.sendmail.MailSenderProperties.factory;

import com.exedio.cope.util.IllegalPropertiesException;
import com.exedio.cope.util.Properties.Source;
import java.util.Properties;
import junit.framework.TestCase;

public class MailSenderPropertiesTest extends TestCase
{
	public void testSet()
	{
		final Properties source = new Properties();
		source.setProperty("host", "testHost.invalid");
		source.setProperty("port", "777");
		source.setProperty("debug", "true");
		source.setProperty("connectTimeout", "8888");
		source.setProperty("readTimeout", "9999");
		final MailSender sender = FACTORY.create(view(source, "DESC")).get();
		assertEquals("testHost.invalid", sender.getHost());
		assertEquals(false, sender.isSSL());
		assertEquals(false, sender.isEnableStarttls());
		assertEquals(777, sender.port);
		assertEquals(true, sender.isDebug());
		assertEquals(8888, sender.getConnectTimeout());
		assertEquals(9999, sender.getReadTimeout());
	}
	public void testDefault()
	{
		final Properties source = new Properties();
		source.setProperty("host", "testHost.invalid");
		final MailSender sender = FACTORY.create(view(source, "DESC")).get();
		assertEquals("testHost.invalid", sender.getHost());
		assertEquals(false, sender.isSSL());
		assertEquals(false, sender.isEnableStarttls());
		assertEquals(25, sender.port);
		assertEquals(false, sender.isDebug());
		assertEquals(5000, sender.getConnectTimeout());
		assertEquals(5000, sender.getReadTimeout());
	}
	public void testSSL()
	{
		final Properties source = new Properties();
		source.setProperty("host", "testHost.invalid");
		source.setProperty("ssl", "true");
		final MailSender sender = FACTORY.create(view(source, "DESC")).get();
		assertEquals("testHost.invalid", sender.getHost());
		assertEquals(true, sender.isSSL());
		assertEquals(false, sender.isEnableStarttls());
		assertEquals(25, sender.port);
	}
	public void testStarttls()
	{
		final Properties source = new Properties();
		source.setProperty("host", "testHost.invalid");
		source.setProperty("enableStarttls", "true");
		final MailSender sender = FACTORY.create(view(source, "DESC")).get();
		assertEquals("testHost.invalid", sender.getHost());
		assertEquals(false, sender.isSSL());
		assertEquals(true, sender.isEnableStarttls());
		assertEquals(25, sender.port);
	}
	public void testSSLandStarttls()
	{
		final Properties source = new Properties();
		source.setProperty("host", "testHost.invalid");
		source.setProperty("ssl", "true");
		source.setProperty("enableStarttls", "true");
		final Source desc = view(source, "DESC");
		try
		{
			FACTORY.create(desc);
			fail();
		}
		catch(final IllegalPropertiesException e)
		{
			assertEquals("property ssl in DESC must be false if enableStarttls is true", e.getMessage());
		}
	}

	private static final com.exedio.cope.util.Properties.Factory<MailSenderProperties> FACTORY = factory();
}
