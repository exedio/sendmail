/*
 * Copyright (C) 2004-2007  exedio GmbH (www.exedio.com)
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;


public class MailSenderConnectionCloseTest extends SendmailTest
{

	private String failclose;
	
	@Override
	public void setUp() throws Exception
	{
		super.setUp();
		
		if(skipTest)
			return;

		failclose=(String)properties.get("failclose");
	}
	
	public static final String[] ta(final String s)
	{
		return s==null ? null : new String[]{s};
	}
	
	private final class MockMail implements Mail
	{
		private final String id;
		private final String to;
		private final String textPlain;

		int sentCounter = 0;
		int failedCounter = 0;
		Exception failedException = null;
		
		MockMail(
				final String id,
				final String to,
				final String textPlain)
		{
			if(id==null)
				throw new RuntimeException("id must not be null");
			if(to!=null && textPlain==null)
				throw new NullPointerException("both textPlain and textAsHtml is null");

			this.id = id;
			this.to = to;
			this.textPlain = textPlain;
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
			return new String[]{to};
		}
		
		public String[] getCarbonCopy()
		{
			return null;
		}
		
		public String[] getBlindCarbonCopy()
		{
			return null;
		}
		
		public String getSubject()
		{
			return "subject (\u00e4\u00f6\u00fc\u00df\u0102\u05d8\u20ac)" + '[' + id + ']' ;
		}
		
		public String getTextPlain()
		{
			return textPlain;
		}
		
		public String getTextHtml()
		{
			return null;
		}
		
		public DataSource[] getAttachments()
		{
			return null;
		}
		
		public String getCharset()
		{
			return null;
		}
		
		public void notifySent()
		{
			sentCounter++;
		}

		public void notifyFailed(final Exception exception)
		{
			failedCounter++;
			failedException = exception;
		}
		
		@Override
		public String toString()
		{
			return "MockMail(" + id + ')';
		}
		
	}
	
	private static interface MockChecker
	{
		void checkBody(Message m) throws IOException, MessagingException;
	}

	private static final int MAXIMUM_RESULT_SIZE = 345;
	
	private final static String TEXT = "text for test mail";
	
	public void testSendMail()
	{
		if(skipTest)
			return;
		
		final ArrayList<MockMail> mails = new ArrayList<MockMail>();
		for(int i = 0; i<50; i++)
			mails.add(new MockMail("mp"+i, failclose, TEXT));

		final MailSource p = new MailSource()
		{
			boolean done = false;
			
			public Collection<? extends Mail> getMailsToSend(final int maximumResultSize)
			{
				assertEquals(MAXIMUM_RESULT_SIZE, maximumResultSize);
				
				if(done)
					return Collections.<Mail>emptyList();
				
				done = true;
				return mails;
			}
		};
		MailSender.sendMails(p, smtpHost, smtpDebug, MAXIMUM_RESULT_SIZE);
		
		for(MockMail m : mails)
			assertSame(SendFailedException.class, m.failedException.getClass());
	}
	
}
