
package com.exedio.sendmail;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;

import junit.framework.TestCase;

import com.sun.mail.pop3.POP3Store;


public class SendMailTest extends TestCase
{
	public SendMailTest(final String name)
	{
		super(name);
	}
	
	private String smtpHost;
	private boolean smtpDebug;

	private String pop3Host;
	private boolean pop3Debug;

	private String user1Email;
	private String user1Pop3User;
	private String user1Pop3Password;
	private String user2Email;
	private String user2Pop3User;
	private String user2Pop3Password;
	private String user3Email;
	private String user3Pop3User;
	private String user3Pop3Password;

	private String from;
	private String fail;
	
	private static boolean countDebug = false;
	
	public void setUp() throws Exception
	{
		super.setUp();

		final Properties properties = new Properties();
		properties.load(new FileInputStream("test.properties"));

		smtpHost=(String)properties.get("smtp.host");
		smtpDebug=properties.get("smtp.debug")!=null;
		
		pop3Host=(String)properties.get("pop3.host");
		pop3Debug=properties.get("pop3.debug")!=null;

		user1Email=       (String)properties.get("user1.email");
		user1Pop3User=    (String)properties.get("user1.pop3.user");
		user1Pop3Password=(String)properties.get("user1.pop3.password");
		user2Email=       (String)properties.get("user2.email");
		user2Pop3User=    (String)properties.get("user2.pop3.user");
		user2Pop3Password=(String)properties.get("user2.pop3.password");
		user3Email=       (String)properties.get("user3.email");
		user3Pop3User=    (String)properties.get("user3.pop3.user");
		user3Pop3Password=(String)properties.get("user3.pop3.password");
		
		from=(String)properties.get("from");
		fail=(String)properties.get("fail");
		
		cleanPOP3Account(user1Pop3User, user1Pop3Password);
		cleanPOP3Account(user2Pop3User, user2Pop3Password);
		cleanPOP3Account(user3Pop3User, user3Pop3Password);
	}
	
	private Session getPOP3Session(final String pop3User)
	{
		final Properties properties = new Properties();
		properties.put("mail.pop3.host", pop3Host);
		properties.put("mail.pop3.user", pop3User);
		final Session session = Session.getInstance(properties);
		if(pop3Debug)
			session.setDebug(true);
		return session;
	}
	
	private POP3Store getPOP3Store(final Session session, final String pop3User, final String pop3Password)
	{
		return new POP3Store(session, new URLName("pop3://"+pop3User+":"+pop3Password+"@"+pop3Host+"/INBOX"));
	}
	
	private void cleanPOP3Account(final String pop3User, final String pop3Password)
	{
		POP3Store store = null;
		Folder inboxFolder = null;
		try
		{
			final Session session = getPOP3Session(pop3User);
			store = getPOP3Store(session, pop3User, pop3Password);
			store.connect();
			final Folder defaultFolder = store.getDefaultFolder();
			assertEquals("", defaultFolder.getFullName());
			inboxFolder = defaultFolder.getFolder("INBOX");
			assertEquals("INBOX", inboxFolder.getFullName());
			inboxFolder.open(Folder.READ_WRITE);
			final Message[] inboxMessages = inboxFolder.getMessages();
			//System.out.println("--------removing "+inboxMessages.length+" messages --------");
			for(int i = 0; i<inboxMessages.length; i++)
			{
				final Message message = inboxMessages[i];
				//System.out.println("-----------------removing message "+i);
				message.setFlag(Flags.Flag.DELETED, true);
			}

			inboxFolder.close(true); // expunge
			inboxFolder = null;
			store.close();
			store = null;
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
					inboxFolder.close(false); // not expunge, just close and release the resources
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
	
	private static class TestMail implements Mail
	{
		private final String from;
		private final String[] to;
		private final String[] cc;
		private final String[] bcc;
		private final String subject;
		private boolean html;
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
		
		public boolean isHTML()
		{
			return html;
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
		final TestMail m1 = new TestMail(from, user1Email, user2Email, user3Email, ts+SUBJECT1, TEXT1);
		final TestMail f1 = new TestMail(from, fail, null, null, "subject for failure test mail"+ts, "text for failure test mail");
		final TestMail f2 = new TestMail(from, (String)null, null, null, null, null);
		final TestMail m2 = new TestMail(from, new String[]{user2Email}, null, null, ts+SUBJECT2, TEXT2);
		m2.html = true;
		final TestMail x12 = new TestMail(from, new String[]{user1Email, user2Email}, null, null, ts+"subject 1+2", TEXT1);
		final TestMail x13 = new TestMail(from, null, new String[]{user1Email, user3Email}, null, ts+"subject 1+3", TEXT1);
		final TestMail x23 = new TestMail(from, null, null, new String[]{user2Email, user3Email}, ts+"subject 2+3", TEXT1);

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
					(complete1 || (complete1=countPOP3(user1Pop3User, user1Pop3Password, 3))) &&
					(complete2 || (complete2=countPOP3(user2Pop3User, user2Pop3Password, 4))) &&
					(complete3 || (complete3=countPOP3(user3Pop3User, user3Pop3Password, 3))) )
			{
				break;
			}
		}
		if(countDebug)
			System.out.println();
		
		assertPOP3(user1Pop3User, user1Pop3Password, new TestMail[]{m1, x12, x13});
		assertPOP3(user2Pop3User, user2Pop3Password, new TestMail[]{m1, m2, x12, x23});
		assertPOP3(user3Pop3User, user3Pop3Password, new TestMail[]{m1, x13, x23});
	}
	
	private void assertPOP3(final String pop3User, final String pop3Password, final TestMail[] expectedMails)
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
			final Session session = getPOP3Session(pop3User);
	
			store = getPOP3Store(session, pop3User, pop3Password);
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
				final String message = pop3User + " - " + subject;
				
				assertNotNull(message, m);
				assertEquals(message, expected.getSubject(), m.getSubject());
				assertEquals(message, list(new InternetAddress(expected.getFrom())), Arrays.asList(m.getFrom()));
				assertEquals(message, ((expected.getTo()==null)&&(expected.getCarbonCopy()==null)) ? list(new InternetAddress("undisclosed-recipients:;")) : addressList(expected.getTo()), addressList(m.getRecipients(Message.RecipientType.TO)));
				assertEquals(message, addressList(expected.getCarbonCopy()), addressList(m.getRecipients(Message.RecipientType.CC)));
				assertEquals(message, null, addressList(m.getRecipients(Message.RecipientType.BCC)));
				assertEquals(message, (expected.html ? "text/html" : "text/plain")+"; charset=us-ascii", m.getContentType());
				assertEquals(message, expected.getText() + TEXT_APPENDIX, m.getContent());
			}
			assertEquals(pop3User, expectedMails.length, inboxMessages.length);

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

	private boolean countPOP3(final String pop3User, final String pop3Password, final int expected)
	{
		POP3Store store = null;
		Folder inboxFolder = null;
		try
		{
			final Session session = getPOP3Session(pop3User);
	
			store = getPOP3Store(session, pop3User, pop3Password);
			store.connect();
			final Folder defaultFolder = store.getDefaultFolder();
			assertEquals("", defaultFolder.getFullName());
			inboxFolder = defaultFolder.getFolder("INBOX");
			assertEquals("INBOX", inboxFolder.getFullName());
			inboxFolder.open(Folder.READ_ONLY);
			final int inboxMessages = inboxFolder.getMessageCount();
			
			if(countDebug)
				System.out.print(" "+pop3User+":"+inboxMessages+"/"+expected);

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
