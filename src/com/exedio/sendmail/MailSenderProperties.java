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

import static java.lang.Math.toIntExact;
import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;

import com.exedio.cope.util.Properties;
import java.time.Duration;
import java.util.StringJoiner;

@SuppressWarnings("synthetic-access")
public final class MailSenderProperties extends Properties
{
	private final MailSender value;

	private MailSenderProperties(final Source source)
	{
		super(source);

		final String host = value("host", (String) null);
		final boolean ssl = value("ssl", false);
		final boolean enableStarttls = value("enableStarttls", false);

		final int portDefault;
		if(ssl)
			if(enableStarttls)
				throw newException("ssl", "must be false if enableStarttls is true");
			else
				portDefault = 465;
		else
			if(enableStarttls)
				portDefault = 587;
			else
				portDefault = 25;

		final int port = value("port", portDefault, 0);
		final boolean debug = value("debug", false);
		final Duration connectTimeout = valueIntMillis("connectTimeout", ofSeconds(5), ofSeconds(1));
		final Duration readTimeout    = valueIntMillis(   "readTimeout", ofSeconds(5), ofSeconds(1));
		final Auth auth = value("auth", false, Auth::new);

		final String returnPath;
		if (value("returnPath.set", false))
		{
			returnPath = value("returnPath", (String) null);
		}
		else
		{
			returnPath = null;
		}

		final boolean dsnNotifyNever = value("dsn.notifyNever", false);
		final boolean dsnNotifySuccess = value("dsn.notifySuccess", false);
		final boolean dsnNotifyFailure = value("dsn.notifyFailure", false);
		final boolean dsnNotifyDelay = value("dsn.notifyDelay", false);

		if (dsnNotifyNever && (dsnNotifySuccess || dsnNotifyFailure || dsnNotifyDelay))
			throw newException("dsn.notifyNever", "must not be set when other notifications are requested");

		final String dsnNotify;
		if (dsnNotifyNever)
		{
			dsnNotify = "NEVER";
		}
		else if (dsnNotifySuccess || dsnNotifyFailure || dsnNotifyDelay)
		{
			final StringJoiner notifyJoiner = new StringJoiner(",");
			if (dsnNotifySuccess)
			{
				notifyJoiner.add("SUCCESS");
			}
			if (dsnNotifyFailure)
			{
				notifyJoiner.add("FAILURE");
			}
			if (dsnNotifyDelay)
			{
				notifyJoiner.add("DELAY");
			}
			dsnNotify = notifyJoiner.toString();
		}
		else
		{
			dsnNotify = null;
		}

		this.value =
			new MailSender(
					host,
					port,
					ssl,
					enableStarttls,
					toIntExact(connectTimeout.toMillis()), // toIntExact cannot fail because of valueIntMillis
					toIntExact(readTimeout   .toMillis()), // toIntExact cannot fail because of valueIntMillis
					debug,
					auth==null ? null : auth.username,
					auth==null ? null : auth.password,
					returnPath,
					dsnNotify);
	}

	// copied from com.exedio.cope.ConnectProperties
	private Duration valueIntMillis(
			final String key,
			final Duration defaultValue,
			final Duration minimum)
	{
		final Duration result = value(key, defaultValue, minimum);
		final Duration maximum = ofMillis(Integer.MAX_VALUE);
		if(result.compareTo(maximum)>0)
			throw newException(key,
					"must be a duration less or equal " + maximum + ", " +
					"but was " + result);
		return result;
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
		private final String password = valueHidden("password", null);

		private Auth(final Source source)
		{
			super(source);
		}
	}
}
