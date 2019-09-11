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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public final class ErrorMailSource implements MailSource
{
	private static final int DEFAULT_OVERFLOW_THRESHOLD = 100;

	final String from;
	final String[] to;
	final String fallbackSubject;
	private final int overflowThreshold;
	@SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized") // TODO
	private int overflowCount = 0;

	@SuppressWarnings("unused") // TODO
	public ErrorMailSource(final String from, final String to, final String subject)
	{
		this(from, to, subject, DEFAULT_OVERFLOW_THRESHOLD);
	}

	public ErrorMailSource(final String from, final String to, final String subject, final int overflowThreshold)
	{
		this(from, new String[]{to}, subject, overflowThreshold);
	}

	@SuppressWarnings("unused") // TODO
	public ErrorMailSource(final String from, final String[] to, final String subject)
	{
		this(from, to, subject, DEFAULT_OVERFLOW_THRESHOLD);
	}

	public ErrorMailSource(final String from, final String[] to, final String fallbackSubject, final int overflowThreshold)
	{
		this.from = from;
		this.to = (to!=null) ? Arrays.copyOf(to, to.length) : null;
		this.fallbackSubject = fallbackSubject;
		this.overflowThreshold = overflowThreshold;
	}

	final List<ErrorMail> mailsToSend = new ArrayList<>();

	@Override
	public Collection<? extends Mail> getMailsToSend(final int maximumResultSize)
	{
		synchronized(mailsToSend)
		{
			final int size = mailsToSend.size();

			if(size==0)
				return Collections.<ErrorMail>emptyList();
			else if(size<=maximumResultSize)
				return new ArrayList<>(mailsToSend);
			else
				return new ArrayList<>(mailsToSend.subList(0, maximumResultSize));
		}
	}

	public int getOverflowCount()
	{
		return overflowCount;
	}

	public Mail createMail(final Throwable exception)
	{
		return createMailWithSubject(null, exception);
	}

	public Mail createMailWithSubject(final String subject, final Throwable exception)
	{
		return createMailWithSubject(subject, null, exception);
	}

	public Mail createMail(final String text, final Throwable exception)
	{
		return createMailWithSubject(null, text, exception);
	}

	public Mail createMailWithSubject(final String subject, final String text, final Throwable exception)
	{
		final StringWriter sw = new StringWriter();
		if(text!=null)
		{
			sw.write(text);
			//noinspection HardcodedLineSeparator
			sw.write('\n');
		}
		final PrintWriter pw = new PrintWriter(sw);
		exception.printStackTrace(pw);
		pw.flush();
		return createMailWithSubject(subject, sw.getBuffer().toString());
	}

	public Mail createMail(final String text)
	{
		return createMailWithSubject(null, text);
	}

	public Mail createMailWithSubject(final String subject, final String text)
	{
		final int overflowCount;
		synchronized(mailsToSend)
		{
			if(mailsToSend.size()>=overflowThreshold)
			{
				this.overflowCount++;
				return null;
			}
			else
			{
				overflowCount = this.overflowCount;
			}
		}

		return new ErrorMail(subject, text, overflowCount);
	}

	private final class ErrorMail extends EmptyMail
	{
		final long timestamp;
		final String mailSubject;
		final String text;
		private final int overflowCountOfMail;

		ErrorMail(final String subject, final String text, final int overflowCountOfMail)
		{
			this.timestamp = System.currentTimeMillis();
			this.mailSubject = subject;
			this.text = text;
			this.overflowCountOfMail = overflowCountOfMail;

			synchronized(mailsToSend)
			{
				//noinspection ThisEscapedInObjectConstruction OK: at the end of the only constructor of a final class
				mailsToSend.add(this);
			}
		}

		@Override
		public String getFrom()
		{
			return from;
		}

		@Override
		@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType") // TODO
		public String[] getTo()
		{
			return to;
		}

		@Override
		public String getSubject()
		{
			final String actualSubject = mailSubject==null ? fallbackSubject : mailSubject;
			return (overflowCountOfMail>0) ? (actualSubject + " (ov" + overflowCountOfMail + ')') : actualSubject;
		}

		@Override
		public String getTextPlain()
		{
			final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			//noinspection HardcodedLineSeparator
			return df.format(new Date(timestamp)) + '\n' + text;
		}

		@Override
		public Date getDate()
		{
			return new Date(timestamp);
		}

		@Override
		public void notifySent()
		{
			mailsToSend.remove(this);
		}

		@Override
		public void notifyFailed(final Exception exception)
		{
			exception.printStackTrace();
			mailsToSend.remove(this);
		}

		@Override
		public String toString()
		{
			return text;
		}
	}
}
