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
import com.exedio.sendmail.MailSender;

@SuppressWarnings("synthetic-access")
public final class MailSenderProperties extends Properties
{
	private final String host = value("host", (String)null);
	private final int port = value("port", 25, 0);
	private final boolean debug = value("debug", false);
	private final int connectTimeout = value("connectTimeout", 5000, 1000);
	private final int    readTimeout = value(   "readTimeout", 5000, 1000);
	private final Auth auth = value("auth", false, Auth.factory());
	private final MailSender value =
			new MailSender(
					host,
					port,
					connectTimeout,
					readTimeout,
					debug,
					auth==null ? null : auth.username,
					auth==null ? null : auth.password);

	public MailSender get()
	{
		return value;
	}

	public static Factory<MailSenderProperties> factory()
	{
		return new Factory<MailSenderProperties>()
		{
			@Override
			public MailSenderProperties create(final Source source)
			{
				return new MailSenderProperties(source);
			}
		};
	}

	private MailSenderProperties(final Source source)
	{
		super(source);
	}

	private static final class Auth extends Properties
	{
		private final String username = value      ("username", (String)null);
		private final String password = valueHidden("password", (String)null);

		private static Factory<Auth> factory()
		{
			return new Factory<Auth>()
			{
				@Override
				public Auth create(final Source source)
				{
					return new Auth(source);
				}
			};
		}

		private Auth(final Source source)
		{
			super(source);
		}
	}
}
