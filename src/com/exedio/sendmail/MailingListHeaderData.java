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

import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * This class provides a wrapper for all mailing list relevant header entries
 * according to <a href="https://www.ietf.org/rfc/rfc2369.txt" target="_top">RFC2369</a>.
 *
 * @author Andreas
 */
public final class MailingListHeaderData
{
	final List<URI> help = new ArrayList<>();
	final List<URI> unsubscribe = new ArrayList<>();
	final List<URI> subscribe = new ArrayList<>();
	final List<URI> post = new ArrayList<>();
	final List<URI> owner = new ArrayList<>();
	final List<URI> archive = new ArrayList<>();

	MailingListHeaderData()
	{
		// Prevent public instantiation
	}

	public void addHelp(final URI uri)
	{
		help.add(check(uri, "help"));
	}

	public void addUnsubscribe(final URI uri)
	{
		unsubscribe.add(check(uri, "unsubscribe"));
	}

	public void addSubscribe(final URI uri)
	{
		subscribe.add(check(uri, "subscribe"));
	}

	public void addPost(final URI uri)
	{
		post.add(check(uri, "post"));
	}

	public void addOwner(final URI uri)
	{
		owner.add(check(uri, "owner"));
	}

	public void addArchive(final URI uri)
	{
		archive.add(check(uri, "archive"));
	}

	private static URI check(final URI uri, final String name)
	{
		requireNonNull(uri, name);
		final String uriScheme = uri.getScheme();
		if(uriScheme == null || uriScheme.trim().isEmpty())
			throw new IllegalArgumentException(name +": URI must not have empty scheme: " + uri);
		if(!("mailto".equals(uriScheme) || "http".equals(uriScheme) || "https".equals(uriScheme) || "ftp".equals(uriScheme)))
			throw new IllegalArgumentException(name + ": URI must define scheme mailto, http, https or ftp: " + uri);
		return uri;
	}

	void addToMessage(final MimeMessage message) throws MessagingException
	{
		addHeader(message, help, "List-Help");
		addHeader(message, unsubscribe, "List-Unsubscribe");
		addHeader(message, subscribe, "List-Subscribe");
		addHeader(message, post, "List-Post");
		addHeader(message, owner, "List-Owner");
		addHeader(message, archive, "List-Archive");
	}

	private static void addHeader(final MimeMessage message,
								  final List<URI> l,
								  final String headerName) throws MessagingException
	{
		if(!l.isEmpty())
		{
			message.setHeader(headerName, toString(l));
		}
	}

	private static String toString(final List<URI> l)
	{
		return l.stream().map(uri -> "<" + uri.toASCIIString() + ">").collect(Collectors.joining(","));
	}
}
