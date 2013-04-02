/*
 * Copyright (C) 2004-2012  exedio GmbH (www.exedio.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.exedio.copedemo.feature.sendmail;

import com.exedio.copedemo.feature.util.MyProperties;
import com.exedio.sendmail.MailSender;

public final class MailSenderProperties extends MyProperties
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

	private static final class Auth extends MyProperties
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
