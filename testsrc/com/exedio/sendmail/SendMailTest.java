
package com.exedio.sendmail;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

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
	private String fail;
	
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
		fail=(String)properties.get("fail");
	}
	
	private static class TestMail implements Mail
	{
		private final String from;
		private final String[] to;
		private final String[] cc;
		private final String[] bcc;
		private final String subject;
		private final String text;

		int sentCounter = 0;
		int failedCounter = 0;
		Exception failedException = null;
		
		public static final String[] ta(final String s)
		{
			return s==null ? null : new String[]{s};
		}
		
		TestMail(final String from,
				final String to,
				final String cc,
				final String bcc,
				final String subject,
				final String text)
		{
			this(from, ta(to), ta(cc), ta(bcc), subject, text);
		}
		
		TestMail(final String from,
				final String[] to,
				final String[] cc,
				final String[] bcc,
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
		
		public String[] getTo()
		{
			return to;
		}
		
		public String[] getCarbonCopy()
		{
			return cc;
		}
		
		public String[] getBlindCarbonCopy()
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

		public void notifyFailed(final Exception exception)
		{
			failedCounter++;
			failedException = exception;
		}
		
	}
	
	private static final int MAXIMUM_RESULT_SIZE = 345;
	
	public void testSendMail()
	{
		final TestMail m1 = new TestMail(from, to, cc, bcc, "subject for test mail", "text for test mail");
		final TestMail f1 = new TestMail(from, fail, null, null, "subject for failure test mail", "text for failure test mail");
		final TestMail f2 = new TestMail(from, (String)null, null, null, null, null);
		final TestMail m2 = new TestMail(from, new String[]{to,to}, new String[]{cc,cc}, new String[]{bcc,bcc}, "subject for test mail with multiple recipients", "text for test mail with multiple recipients");

		final MailSource p = new MailSource()
		{
			public Collection getMailsToSend(final int maximumResultSize)
			{
				assertEquals(MAXIMUM_RESULT_SIZE, maximumResultSize);
				final ArrayList result = new ArrayList();
				result.add(m1);
				result.add(f1);
				result.add(f2);
				result.add(m2);
				return result;
			}
		};
		MailSender.sendMails(p, smtp, MAXIMUM_RESULT_SIZE);

		assertEquals(null, m1.failedException);
		assertEquals(1, m1.sentCounter);
		assertEquals(0, m1.failedCounter);

		final String fm1 = f1.failedException.getMessage();
		assertTrue(fm1+"--------"+fail, fm1.indexOf(fail)>=0);
		assertEquals(0, f1.sentCounter);
		assertEquals(1, f1.failedCounter);

		assertEquals(NullPointerException.class, f2.failedException.getClass());
		assertEquals(0, f2.sentCounter);
		assertEquals(1, f2.failedCounter);

		assertEquals(null, m2.failedException);
		assertEquals(1, m2.sentCounter);
		assertEquals(0, m2.failedCounter);
	}

}
