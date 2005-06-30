/*
 * Copyright (C) 2004-2005  exedio GmbH (www.exedio.com)
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.activation.DataSource;

public final class ErrorMailSource implements MailSource
{
	private final String from;
	private final String[] to;
	private final String subject;
	
	public ErrorMailSource(final String from, final String to, final String subject)
	{
		this.from = from;
		this.to = new String[]{to};
		this.subject = subject;
	}
	
	public ErrorMailSource(final String from, final String[] to, final String subject)
	{
		this.from = from;
		this.to = to;
		this.subject = subject;
	}
	
	private final List mailsToSend = Collections.synchronizedList(new ArrayList());
	
	public final Collection getMailsToSend(final int maximumResultSize)
	{
		final int size = mailsToSend.size();
		
		if(size==0)
			return Collections.EMPTY_LIST;
		else if(size<=maximumResultSize)
			return new ArrayList(mailsToSend);
		else
			return new ArrayList(mailsToSend.subList(0, maximumResultSize));
	}
	
	public Mail createMail(final Exception exception)
	{
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw);
		exception.printStackTrace(pw);
		pw.flush();
		return new ErrorMail(sw.getBuffer().toString());
	}
	
	public Mail createMail(final String text)
	{
		return new ErrorMail(text);
	}
	
	private final class ErrorMail implements Mail
	{
		final String text;
		
		private ErrorMail(final String text)
		{
			this.text = text;
			mailsToSend.add(this);
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
		
		public boolean isHTML()
		{
			return false;
		}
		
		public String getText()
		{
			return text;
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
		
	}
	
}
