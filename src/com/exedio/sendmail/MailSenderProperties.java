/*
 * Copyright (C) 2004-2012  exedio GmbH (www.exedio.com)
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

import com.exedio.cope.util.Properties;

@SuppressWarnings("synthetic-access")
public final class MailSenderProperties extends Properties
{
	private final MailSender value;

	private MailSenderProperties(final Source source)
	{
		super(source);

		final String host = value("host", (String) null);
		final int port = value("port", 25, 0);
		final boolean ssl = value("ssl", false);
		final boolean enableStarttls = value("enableStarttls", false);
		final boolean debug = value("debug", false);
		final int connectTimeout = value("connectTimeout", 5000, 1000);
		final int readTimeout    = value(   "readTimeout", 5000, 1000);
		final Auth auth = value("auth", false, Auth::new);

		if(ssl && enableStarttls)
			throw newException("ssl", "must be false if enableStarttls is true");

		this.value =
			new MailSender(
					host,
					port,
					ssl,
					enableStarttls,
					connectTimeout,
					readTimeout,
					debug,
					auth==null ? null : auth.username,
					auth==null ? null : auth.password);
	}

	public MailSender get()
	{
		return value;
	}

	public static Factory<MailSenderProperties> factory()
	{
		return MailSenderProperties::new;
	}

	private static final class Auth extends Properties
	{
		private final String username = value      ("username", (String)null);
		private final String password = valueHidden("password", (String)null);

		private Auth(final Source source)
		{
			super(source);
		}
	}
}
