package com.exedio.sendmail;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class ErrorEmailProvider implements EmailProvider
{
	final String from;
	final String to;
	final String subject;
	
	public ErrorEmailProvider(final String from, final String to, final String subject)
	{
		this.from = from;
		this.to = to;
		this.subject = subject;
	}
	
	private final List emailsToBeSent = Collections.synchronizedList(new ArrayList());
	
	public final Collection getEmailsToBeSent(final int maximumResultSize)
	{
		if(emailsToBeSent.isEmpty())
			return Collections.EMPTY_LIST;
		else
			return new ArrayList(emailsToBeSent);
	}
	
	public String getSMTPHost()
	{
		throw new RuntimeException();
	}

	public EmailToBeSent createMail(final Exception exception)
	{
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw);
		exception.printStackTrace(pw);
		pw.flush();
		return new Mail(sw.getBuffer().toString());
	}
	
	public EmailToBeSent createMail(final String text)
	{
		return new Mail(text);
	}
	
	private final class Mail implements EmailToBeSent
	{
		final String text;
		
		private Mail(final String text)
		{
			this.text = text;
			emailsToBeSent.add(this);
		}

		public String getFrom()
		{
			return from;
		}
		
		public String getTo()
		{
			return to;
		}
		
		public String getCarbonCopy()
		{
			return null;
		}
		
		public String getBlindCarbonCopy()
		{
			return null;
		}
		
		public final String getSubject()
		{
			return subject;
		}
		
		public String getText()
		{
			return text;
		}
		
		public void notifySent()
		{
			emailsToBeSent.remove(this);
		}

		public void notifyFailed(final Exception exception)
		{
			exception.printStackTrace();
			emailsToBeSent.remove(this);
		}
		
	}
	
}
