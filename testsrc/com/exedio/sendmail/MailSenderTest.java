package com.exedio.sendmail;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.activation.DataSource;
import javax.activation.URLDataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;

import com.sun.mail.pop3.POP3Store;


public class MailSenderTest extends SendmailTest
{

	private Account user1;
	private Account user2;
	private Account user3;

	private String fail;
	
	private static boolean countDebug = false;
	
	public void setUp() throws Exception
	{
		super.setUp();

		user1 = new Account("user1");
		user2 = new Account("user2");
		user3 = new Account("user3");
		
		fail=(String)properties.get("fail");
		
		cleanPOP3Account(user1);
		cleanPOP3Account(user2);
		cleanPOP3Account(user3);
	}
	
	private static final class TestMail implements Mail
	{
		private final String from;
		private final String[] to;
		private final String[] cc;
		private final String[] bcc;
		private final String subject;
		private boolean html = false;
		private final String text;
		private final DataSource[] attachements;

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
			this(from, to, cc, bcc, subject, text, null);
		}
		
		TestMail(final String from,
				final String to,
				final String subject,
				final String text)
		{
			this(from, ta(to), null, null, subject, text, (DataSource[])null);
		}
		
		TestMail(final String from,
				final String to,
				final String subject,
				final String text,
				final DataSource attachement)
		{
			this(from, ta(to), null, null, subject, text, new DataSource[]{attachement});
		}
		
		TestMail(final String from,
				final String to,
				final String subject,
				final String text,
				final DataSource attachement1,
				final DataSource attachement2)
		{
			this(from, ta(to), null, null, subject, text, new DataSource[]{attachement1, attachement2});
		}
		
		TestMail(final String from,
				final String[] to,
				final String[] cc,
				final String[] bcc,
				final String subject,
				final String text,
				final DataSource[] attachements)
		{
			this.from = from;
			this.to = to;
			this.cc = cc;
			this.bcc = bcc;
			this.subject = subject;
			this.text = text;
			this.attachements = attachements;
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
		
		public boolean isHTML()
		{
			return html;
		}
		
		public String getText()
		{
			return text;
		}
		
		public DataSource[] getAttachements()
		{
			return attachements;
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
	
	/*private static final class TestDataSource implements DataSource
	{
		final String resource;
		final String name;
		final String contentType;
		
		TestDataSource(final Class resource, final String name, final String contentType)
		{
			final String resourceName = resource.getName();
			this.resource = resourceName.substring(resourceName.lastIndexOf('.'));
			this.name = name;
			this.contentType = contentType;
			
			if(this.resource==null)
				throw new RuntimeException();
			if(this.name==null)
				throw new RuntimeException();
			if(this.contentType==null)
				throw new RuntimeException();
		}
		
		public String getContentType()
		{
			return contentType;
		}
		
		public String getName()
		{
			return name;
		}
		
		public InputStream getInputStream()
		{
			return getClass().getResourceAsStream(resource);
		}
		
		public OutputStream getOutputStream()
		{
			throw new RuntimeException(name);
		}
	}*/
	
	private static final class TestURLDataSource extends URLDataSource
	{
		final String name;
		
		TestURLDataSource(final String name)
		{
			super(MailSenderTest.class.getResource(name));
			this.name = name;
		}
		
		public String getName()
		{
			return name;
		}
		
	}

	private static final int MAXIMUM_RESULT_SIZE = 345;
	
	private final static String SUBJECT1 = "subject text";
	private final static String SUBJECT2 = "subject html";
	private final static String TEXT_APPENDIX = "\r\n\r\n";
	private final static String TEXT1 = "text for test mail";
	private final static String TEXT2 =
		"<html><body>text for test mail with multiple recipients and with html features " +
		"such as <b>bold</b>, <i>italic</i> and <font color=\"#FF0000\">red</font> text.</body></html>";
	
	public void testSendMail() throws InterruptedException
	{
		final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S ");
		final String ts = df.format(new Date());
		final TestMail m1 = new TestMail(from, user1.email, user2.email, user3.email, ts+SUBJECT1, TEXT1);
		final TestMail f1 = new TestMail(from, fail, null, null, "subject for failure test mail"+ts, "text for failure test mail");
		final TestMail f2 = new TestMail(from, (String)null, null, null);
		final TestMail m2 = new TestMail(from, user2.email, null, null, ts+SUBJECT2, TEXT2);
		m2.html = true;
		final TestMail x12 = new TestMail(from, new String[]{user1.email, user2.email}, null, null, ts+"subject 1+2", TEXT1);
		final TestMail x13 = new TestMail(from, null, new String[]{user1.email, user3.email}, null, ts+"subject 1+3", TEXT1);
		final TestMail x23 = new TestMail(from, null, null, new String[]{user2.email, user3.email}, ts+"subject 2+3", TEXT1);
		final TestMail ma1 = new TestMail(from, user1.email, ts+"subject text attach", TEXT1,
				new TestURLDataSource("MailSenderTest.class"));
				//new TestDataSource(MailSenderTest.class, "hallo1.class", "application/java-vm"));
		final TestMail ma2 = new TestMail(from, user1.email, ts+"subject html attach", TEXT2,
				new TestURLDataSource("PackageTest.class"),
				new TestURLDataSource("CompositeMailSourceTest.class"));
				//new TestDataSource(PackageTest.class, "hallo21.zick", "application/java-vm"),
				//new TestDataSource(CompositeMailSourceTest.class, "hallo22.zock", "application/java-vm"));
		ma2.html = true;

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
				result.add(x12);
				result.add(x13);
				result.add(x23);
				result.add(ma1);
				result.add(ma2);
				return result;
			}
		};
		MailSender.sendMails(p, smtpHost, smtpDebug, MAXIMUM_RESULT_SIZE);

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
		
		assertEquals(null, x12.failedException);
		assertEquals(1, x12.sentCounter);
		assertEquals(0, x12.failedCounter);
		
		assertEquals(null, x13.failedException);
		assertEquals(1, x13.sentCounter);
		assertEquals(0, x13.failedCounter);
		
		assertEquals(null, x23.failedException);
		assertEquals(1, x23.sentCounter);
		assertEquals(0, x23.failedCounter);
		
		assertEquals(null, ma1.failedException);
		assertEquals(1, ma1.sentCounter);
		assertEquals(0, ma1.failedCounter);
		
		assertEquals(null, ma2.failedException);
		assertEquals(1, ma2.sentCounter);
		assertEquals(0, ma2.failedCounter);

		boolean complete1 = false;
		boolean complete2 = false;
		boolean complete3 = false;
		for(int i = 0; i<30; i++)
		{
			Thread.sleep(1000);
			if(countDebug)
			{
				System.out.println();
				System.out.print("---------"+i+"--");
			}
			if(
					(complete1 || (complete1=countPOP3(user1, 5))) &&
					(complete2 || (complete2=countPOP3(user2, 4))) &&
					(complete3 || (complete3=countPOP3(user3, 3))) )
			{
				break;
			}
		}
		if(countDebug)
			System.out.println();
		
		assertPOP3(user1, new TestMail[]{m1, x12, x13, ma1, ma2});
		assertPOP3(user2, new TestMail[]{m1, m2, x12, x23});
		assertPOP3(user3, new TestMail[]{m1, x13, x23});
	}
	
	private void assertPOP3(final Account account, final TestMail[] expectedMails)
	{
		final TreeMap expectedMessages = new TreeMap();
		for(int i = 0; i<expectedMails.length; i++)
		{
			final TestMail m = expectedMails[i];
			if(expectedMessages.put(m.getSubject(), m)!=null)
				throw new RuntimeException(m.getSubject());
		}

		POP3Store store = null;
		Folder inboxFolder = null;
		try
		{
			final Session session = getPOP3Session(account);
	
			store = getPOP3Store(session, account);
			store.connect();
			final Folder defaultFolder = store.getDefaultFolder();
			assertEquals("", defaultFolder.getFullName());
			inboxFolder = defaultFolder.getFolder("INBOX");
			assertEquals("INBOX", inboxFolder.getFullName());
			inboxFolder.open(Folder.READ_ONLY);
			final Message[] inboxMessages = inboxFolder.getMessages();
			
			final TreeMap actualMessages = new TreeMap();
			for(int i = 0; i<inboxMessages.length; i++)
			{
				final Message m = inboxMessages[i];
				if(actualMessages.put(m.getSubject(), m)!=null)
					throw new RuntimeException(m.getSubject());
			}
			
			for(Iterator i = expectedMessages.keySet().iterator(); i.hasNext(); )
			{
				final String subject = (String)i.next();
				final Message m = (Message)actualMessages.get(subject);
				final TestMail expected = (TestMail)expectedMessages.get(subject);
				final String message = account.pop3User + " - " + subject;
				
				assertNotNull(message, m);
				assertEquals(message, expected.getSubject(), m.getSubject());
				assertEquals(message, list(new InternetAddress(expected.getFrom())), Arrays.asList(m.getFrom()));
				assertEquals(message, ((expected.getTo()==null)&&(expected.getCarbonCopy()==null)) ? list(new InternetAddress("undisclosed-recipients:;")) : addressList(expected.getTo()), addressList(m.getRecipients(Message.RecipientType.TO)));
				assertEquals(message, addressList(expected.getCarbonCopy()), addressList(m.getRecipients(Message.RecipientType.CC)));
				assertEquals(message, null, addressList(m.getRecipients(Message.RecipientType.BCC)));
				final DataSource[] attachements = expected.getAttachements();
				if(attachements==null)
				{
					assertEquals(message, (expected.html ? "text/html" : "text/plain")+"; charset=us-ascii", m.getContentType());
					assertEquals(message, expected.getText() + TEXT_APPENDIX, m.getContent());
				}
				else
				{
					assertTrue(message+"-"+m.getContentType(), m.getContentType().startsWith("multipart/alternative;"));
					final MimeMultipart multipart = (MimeMultipart)m.getContent();
					final BodyPart mainBody = multipart.getBodyPart(0);
					assertEquals(message, (expected.html ? "text/html" : "text/plain")+"; charset=us-ascii", mainBody.getContentType());
					assertEquals(message, expected.getText(), mainBody.getContent());
					for(int j = 0; j<attachements.length; j++)
					{
						final BodyPart attachBody = multipart.getBodyPart(j+1);
						assertEquals(message, attachements[j].getName(), attachBody.getFileName());
						assertTrue(message+"-"+attachBody.getContentType(), attachBody.getContentType().startsWith("application/java-vm"));
						assertEquals(message, bytes(attachements[j].getInputStream()), bytes((InputStream)attachBody.getContent()));
					}
				}
			}
			assertEquals(account.pop3User, expectedMails.length, inboxMessages.length);

			inboxFolder.close(false);
			inboxFolder = null;
			store.close();
			store = null;
		}
		catch(MessagingException e)
		{
			throw new RuntimeException(e);
		}
		catch(IOException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			if(inboxFolder!=null)
			{
				try
				{
					inboxFolder.close(false);
				}
				catch(MessagingException e)
				{}
			}
			if(store!=null)
			{
				try
				{
					store.close();
				}
				catch(MessagingException e)
				{}
			}
		}
	}

	private boolean countPOP3(final Account account, final int expected)
	{
		POP3Store store = null;
		Folder inboxFolder = null;
		try
		{
			final Session session = getPOP3Session(account);
	
			store = getPOP3Store(session, account);
			store.connect();
			final Folder defaultFolder = store.getDefaultFolder();
			assertEquals("", defaultFolder.getFullName());
			inboxFolder = defaultFolder.getFolder("INBOX");
			assertEquals("INBOX", inboxFolder.getFullName());
			inboxFolder.open(Folder.READ_ONLY);
			final int inboxMessages = inboxFolder.getMessageCount();
			
			if(countDebug)
				System.out.print(" "+account.pop3User+":"+inboxMessages+"/"+expected);

			inboxFolder.close(false);
			inboxFolder = null;
			store.close();
			store = null;
			
			return inboxMessages>=expected;
		}
		catch(MessagingException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			if(inboxFolder!=null)
			{
				try
				{
					inboxFolder.close(false);
				}
				catch(MessagingException e)
				{}
			}
			if(store!=null)
			{
				try
				{
					store.close();
				}
				catch(MessagingException e)
				{}
			}
		}
	}
	
	protected final static byte[] bytes(final InputStream in)
	{
		try
		{
			final byte[] buf = new byte[1024];
			in.read(buf);
			return buf;
		}
		catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	void assertEquals(String message, byte[] expected, byte[] actual)
	{
		assertEquals(message, expected.length, actual.length);
		for(int i = 0; i<expected.length; i++)
			assertEquals(message+'-'+i, expected[i], actual[i]);
	}

	protected final static ArrayList addressList(final String[] addresses) throws MessagingException
	{
		if(addresses==null)
			return null;
		
		final ArrayList result = new ArrayList(addresses.length);
		for(int i = 0; i<addresses.length; i++)
			result.add(new InternetAddress(addresses[i]));
		return result;
	}

	protected final static ArrayList addressList(final Address[] addresses) throws MessagingException
	{
		if(addresses==null)
			return null;
		
		final ArrayList result = new ArrayList(addresses.length);
		for(int i = 0; i<addresses.length; i++)
			result.add(addresses[i]);
		return result;
	}

	protected final static List list()
	{
		return Collections.EMPTY_LIST;
	}

	protected final static List list(final Object o)
	{
		return Collections.singletonList(o);
	}
	
	protected final static List list(final Object o1, final Object o2)
	{
		return Arrays.asList(new Object[]{o1, o2});
	}
	
	protected final static List list(final Object o1, final Object o2, final Object o3)
	{
		return Arrays.asList(new Object[]{o1, o2, o3});
	}
	
	protected final static List list(final Object o1, final Object o2, final Object o3, final Object o4)
	{
		return Arrays.asList(new Object[]{o1, o2, o3, o4});
	}
	
}
