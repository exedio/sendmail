
package com.exedio.sendmail;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import javax.mail.MessagingException;

import junit.framework.TestCase;


public class SendMailTest extends TestCase
{
	public SendMailTest(final String name)
	{
		super(name);
	}
	
	private String smtp;
	private String from;
	private String to;
	private String cc;
	private String bcc;
	
	public void setUp() throws Exception
	{
		super.setUp();
		final Properties properties = new Properties();
		properties.load(new FileInputStream("test.properties"));
		smtp=(String)properties.get("smtp");
		from=(String)properties.get("from");
		to=(String)properties.get("to");
		cc=(String)properties.get("cc");
		bcc=(String)properties.get("bcc");
	}
	
	private static class Email implements EmailToBeSent
	{
		private final String from;
		private final String to;
		private final String cc;
		private final String bcc;
		private final String subject;
		private final String text;

		int sentCounter = 0;
		int failedCounter = 0;
		Exception failedException = null;
		
		Email(final String from,
				final String to,
				final String cc,
				final String bcc,
				final String subject,
				final String text)
		{
			this.from = from;
			this.to = to;
			this.cc = cc;
			this.bcc = bcc;
			this.subject = subject;
			this.text = text;
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
			return cc;
		}
		
		public String getBlindCarbonCopy()
		{
			return bcc;
		}
		
		public String getSubject()
		{
			return subject;
		}
		
		public String getText()
		{
			return text;
		}
		
		public void notifySent()
		{
			sentCounter++;
		}

		public void notifyFailed(final MessagingException exception)
		{
			failedCounter++;
			failedException = exception;
		}
		
	}
	
	private static final int MAXIMUM_RESULT_SIZE = 345;
	
	public void testSendMail()
	{
		final Email m1 = new Email(from, to, cc, bcc, "subject for test mail", "text for test mail");

		final EmailProvider p = new EmailProvider()
		{
			public Collection getEmailsToBeSent(final int maximumResultSize)
			{
				assertEquals(MAXIMUM_RESULT_SIZE, maximumResultSize);
				final ArrayList result = new ArrayList();
				result.add(m1);
				return result;
			}
			
			public String getSMTPHost()
			{
				return smtp;
			}
			
		};
		EmailRunner.sendMails(p, MAXIMUM_RESULT_SIZE);

		assertEquals(null, m1.failedException);
		assertEquals(1, m1.sentCounter);
		assertEquals(0, m1.failedCounter);
	}

}
