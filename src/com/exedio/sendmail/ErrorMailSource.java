/*
 * Copyright (C) 2004-2006  exedio GmbH (www.exedio.com)
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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.activation.DataSource;

public final class ErrorMailSource implements MailSource
{
	private static final int DEFAULT_OVERFLOW_THRESHOLD = 100;

	private final String from;
	private final String[] to;
	private final String subject;
	private final int overflowThreshold;
	
	public ErrorMailSource(final String from, final String to, final String subject)
	{
		this(from, to, subject, DEFAULT_OVERFLOW_THRESHOLD);
	}
	
	public ErrorMailSource(final String from, final String to, final String subject, final int overflowThreshold)
	{
		this(from, new String[]{to}, subject, overflowThreshold);
	}
	
	public ErrorMailSource(final String from, final String[] to, final String subject)
	{
		this(from, to, subject, DEFAULT_OVERFLOW_THRESHOLD);
	}

	public ErrorMailSource(final String from, final String[] to, final String subject, final int overflowThreshold)
	{
		this.from = from;
		this.to = to;
		this.subject = subject;
		this.overflowThreshold = overflowThreshold;
	}
	
	private final List<ErrorMail> mailsToSend = new ArrayList<ErrorMail>();
	
	public final Collection<? extends Mail> getMailsToSend(final int maximumResultSize)
	{
		synchronized(mailsToSend)
		{
			final int size = mailsToSend.size();
			
			if(size==0)
				return Collections.<ErrorMail>emptyList();
			else if(size<=maximumResultSize)
				return new ArrayList<ErrorMail>(mailsToSend);
			else
				return new ArrayList<ErrorMail>(mailsToSend.subList(0, maximumResultSize));
		}
	}
	
	public Mail createMail(final Exception exception)
	{
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw);
		exception.printStackTrace(pw);
		pw.flush();
		return createMail(sw.getBuffer().toString());
	}
	
	public Mail createMail(final String text)
	{
		synchronized(mailsToSend)
		{
			if(mailsToSend.size()>=overflowThreshold)
				return null;
		}

		return new ErrorMail(text);
	}
	
	private final class ErrorMail implements Mail
	{
		final long timestamp;
		final String text;
		
		private ErrorMail(final String text)
		{
			this.timestamp = System.currentTimeMillis();
			this.text = text;
			
			synchronized(mailsToSend)
			{
				mailsToSend.add(this);
			}
		}

		public String getMessageID()
		{
			return null;
		}
		
		public String getFrom()
		{
			return from;
		}
		
		public String[] getTo()
		{
			return to;
		}
		
		public String[] getCarbonCopy()
		{
			return null;
		}
		
		public String[] getBlindCarbonCopy()
		{
			return null;
		}
		
		public final String getSubject()
		{
			return subject;
		}
		
		public String getTextPlain()
		{
			final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			return df.format(new Date(timestamp)) + '\n' + text;
		}
		
		public String getTextHtml()
		{
			return null;
		}
		
		public DataSource[] getAttachments()
		{
			return null;
		}
		
		public void notifySent()
		{
			mailsToSend.remove(this);
		}

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
