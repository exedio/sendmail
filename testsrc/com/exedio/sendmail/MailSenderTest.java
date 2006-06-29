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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
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
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import com.sun.mail.pop3.POP3Store;


public class MailSenderTest extends SendmailTest
{

	private Account user1;
	private Account user2;
	private Account user3;

	private String fail;
	
	private static boolean countDebug = false;
	
	@Override
	public void setUp() throws Exception
	{
		super.setUp();
		
		if(skipTest)
			return;

		user1 = new Account("user1");
		user2 = new Account("user2");
		user3 = new Account("user3");
		
		fail=(String)properties.get("fail");
		
		cleanPOP3Account(user1);
		cleanPOP3Account(user2);
		cleanPOP3Account(user3);
	}
	
	private static final class MockMail implements Mail
	{
		private final MockChecker checker;
		private final String id;
		private final String from;
		private final String[] to;
		private final String[] cc;
		private final String[] bcc;
		private final String subject;
		private final String text;
		private final String textAsHtml;
		private final DataSource[] attachments;
		boolean specialMessageID = false;

		int sentCounter = 0;
		int failedCounter = 0;
		Exception failedException = null;
		
		public static final String[] ta(final String s)
		{
			return s==null ? null : new String[]{s};
		}
		
		MockMail(
				final String id,
				final String from,
				final String to,
				final String cc,
				final String bcc,
				final String subject,
				final String text,
				final MockChecker checker)
		{
			this(id, from, ta(to), ta(cc), ta(bcc), subject, text, (String)null, (DataSource[])null, checker);
		}
		
		MockMail(
				final String id,
				final String from,
				final String to,
				final String cc,
				final String bcc,
				final String subject,
				final String text,
				final String textAsHtml,
				final MockChecker checker)
		{
			this(id, from, ta(to), ta(cc), ta(bcc), subject, text, textAsHtml, (DataSource[])null, checker);
		}
		
		MockMail(
				final String id,
				final String from,
				final String[] to,
				final String[] cc,
				final String[] bcc,
				final String subject,
				final String text,
				final MockChecker checker)
		{
			this(id, from, to, cc, bcc, subject, text, (String)null, (DataSource[])null, checker);
		}
		
		MockMail(
				final String id,
				final String from,
				final String to,
				final String subject,
				final String text,
				final MockChecker checker)
		{
			this(id, from, ta(to), null, null, subject, text, (String)null, (DataSource[])null, checker);
		}
		
		MockMail(
				final String id,
				final String from,
				final String to,
				final String subject,
				final String text,
				final DataSource attachement,
				final MockChecker checker)
		{
			this(id, from, ta(to), null, null, subject, text, (String)null, new DataSource[]{attachement}, checker);
		}
		
		MockMail(
				final String id,
				final String from,
				final String to,
				final String subject,
				final String text,
				final String textAsHtml,
				final DataSource attachement1,
				final DataSource attachement2,
				final MockChecker checker)
		{
			this(id, from, ta(to), null, null, subject, text, textAsHtml, new DataSource[]{attachement1, attachement2}, checker);
		}
		
		MockMail(
				final String id,
				final String from,
				final String[] to,
				final String[] cc,
				final String[] bcc,
				final String subject,
				final String text,
				final String textAsHtml,
				final DataSource[] attachments,
				final MockChecker checker)
		{
			if(checker==null)
				throw new RuntimeException("checker must not be null");
			if(id==null)
				throw new RuntimeException("id must not be null");
			if(to!=null && text==null && textAsHtml==null)
				throw new NullPointerException("both text and textAsHtml is null");

			this.checker = checker;
			this.id = id;
			this.from = from;
			this.to = to;
			this.cc = cc;
			this.bcc = bcc;
			this.subject = subject;
			this.text = text;
			this.textAsHtml = textAsHtml;
			this.attachments = attachments;
		}
		
		public String getMessageID()
		{
			return specialMessageID ? "messageid-" + id + '-' + System.currentTimeMillis() : null;
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
			return subject + '[' + id + ']' ;
		}
		
		public String getText()
		{
			return text;
		}
		
		public String getTextAsHtml()
		{
			return textAsHtml;
		}
		
		public DataSource[] getAttachments()
		{
			return attachments;
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
		
		void checkBody(final Message m) throws IOException, MessagingException
		{
			checker.checkBody(m);;
		}
		
	}
	
	/*private static final class MockURLDataSource implements DataSource
	{
		final String resource;
		final String name;
		final String contentType;
		
		TestDataSource(final Class resource, final String name, final String contentType)
		{
			this.resource = resource.getSimpleName();
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
	
	private static final class MockURLDataSource extends URLDataSource
	{
		final String name;
		final String contentType;
		
		MockURLDataSource(final String name, final String contentType)
		{
			super(MailSenderTest.class.getResource(name));
			this.name = name;
			this.contentType = contentType;
			
			if(this.contentType==null)
				throw new RuntimeException();
		}
		
		@Override
		public String getName()
		{
			return name;
		}
		
		@Override
		public String getContentType()
		{
			return contentType;
		}
	}
	
	private static interface MockChecker
	{
		void checkBody(Message m) throws IOException, MessagingException;
	}

	private static final int MAXIMUM_RESULT_SIZE = 345;
	
	private final static String NON_ASCII_SUBJECT = " (\u00e4\u00f6\u00fc\u00df\u0102\u05d8\u20ac)";
	private final static String NON_ASCII_TEXT = " (auml-\u00e4 ouml-\u00f6 uuml-\u00fc szlig-\u00df abreve-\u0102 hebrew-\u05d8 euro-\u20ac)";
	private final static String SUBJECT1 = "subject text" + NON_ASCII_SUBJECT;
	private final static String SUBJECT2 = "subject html" + NON_ASCII_SUBJECT;
	private final static String TEXT_APPENDIX = "\r\n\r\n";
	private final static String TEXT1 = "text for test mail" + NON_ASCII_TEXT;
	private final static String TEXT2 =
		"<html>" +
		"<head>" +
		"<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">" +
		"</head>" +
		"<body>" +
		"text for test mail with multiple recipients and with html features " +
		"such as <b>bold</b>, <i>italic</i> and <font color=\"#FF0000\">red</font> text " +
		"and special characters" + NON_ASCII_TEXT + "." +
		"</body>" +
		"</html>";
	
	public void testSendMail() throws InterruptedException
	{
		if(skipTest)
			return;
		
		final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S ");
		final String ts = df.format(new Date());
		final MockMail m1  = new MockMail("m1",  from, user1.email, user2.email, user3.email, ts+SUBJECT1, TEXT1, new MockChecker(){
			public void checkBody(final Message m) throws IOException, MessagingException
			{
				assertEquals("text/plain; charset="+CHARSET, m.getContentType());
				assertEquals(TEXT1 + TEXT_APPENDIX, m.getContent());
			}
		});
		final MockMail f1  = new MockMail("f1",  from, fail, null, null, "subject for failure test mail"+ts, "text for failure test mail", new MockChecker(){
			public void checkBody(final Message actual)
			{
				fail("should not be sent");
			}
		});
		final MockMail f2  = new MockMail("f2",  from, (String)null, null, null, new MockChecker(){
			public void checkBody(final Message actual)
			{
				fail("should not be sent");
			}
		});
		final MockMail m2  = new MockMail("m2",  from, user2.email, null, null, ts+SUBJECT2, (String)null, TEXT2, new MockChecker(){
			public void checkBody(final Message m) throws IOException, MessagingException
			{
				assertEquals("text/html; charset="+CHARSET, m.getContentType());
				assertEquals(TEXT2 + TEXT_APPENDIX, m.getContent());
			}
		});
		m2.specialMessageID = true;
		final MockMail m3  = new MockMail("m3",  from, user2.email, null, null, ts+SUBJECT2, TEXT1, TEXT2, new MockChecker(){
			public void checkBody(final Message m) throws IOException, MessagingException
			{
				assertTrue(m.getContentType(), m.getContentType().startsWith("multipart/alternative;"));
				final MimeMultipart multipart = (MimeMultipart)m.getContent();
				{
					final BodyPart textBody = multipart.getBodyPart(0);
					assertEquals("text/plain; charset="+CHARSET, textBody.getContentType());
					assertEquals(TEXT1, textBody.getContent());
				}
				{
					final BodyPart htmlBody = multipart.getBodyPart(1);
					assertEquals("text/html; charset="+CHARSET, htmlBody.getContentType());
					assertEquals(TEXT2, htmlBody.getContent());
				}
			}
		});
		final MockMail x12 = new MockMail("x12", from, new String[]{user1.email, user2.email}, null, null, ts+"subject 1+2", TEXT1, new MockChecker(){
			public void checkBody(final Message m) throws IOException, MessagingException
			{
				assertEquals("text/plain; charset="+CHARSET, m.getContentType());
				assertEquals(TEXT1 + TEXT_APPENDIX, m.getContent());
			}
		});
		final MockMail x13 = new MockMail("x13", from, null, new String[]{user1.email, user3.email}, null, ts+"subject 1+3", TEXT1, new MockChecker(){
			public void checkBody(final Message m) throws IOException, MessagingException
			{
				assertEquals("text/plain; charset="+CHARSET, m.getContentType());
				assertEquals(TEXT1 + TEXT_APPENDIX, m.getContent());
			}
		});
		final MockMail x23 = new MockMail("x23", from, null, null, new String[]{user2.email, user3.email}, ts+"subject 2+3", TEXT1, new MockChecker(){
			public void checkBody(final Message m) throws IOException, MessagingException
			{
				assertEquals("text/plain; charset="+CHARSET, m.getContentType());
				assertEquals(TEXT1 + TEXT_APPENDIX, m.getContent());
			}
		});
		final MockMail ma1 = new MockMail("ma1", from, user1.email, ts+"subject text attach", TEXT1,
				//new MockDataSource(MailSenderTest.class, "hallo1.class", "application/java-vm"));
				new MockURLDataSource("osorno.png", "image/png"), new MockChecker(){
			public void checkBody(final Message m) throws IOException, MessagingException
			{
				assertTrue(m.getContentType(), m.getContentType().startsWith("multipart/mixed;"));
				final MimeMultipart multipart = (MimeMultipart)m.getContent();
				final BodyPart mainBody = multipart.getBodyPart(0);
				assertEquals("text/plain; charset="+CHARSET, mainBody.getContentType());
				assertEquals(TEXT1, mainBody.getContent());
				{
					final BodyPart attachBody = multipart.getBodyPart(1);
					assertEquals("osorno.png", attachBody.getFileName());
					assertTrue(attachBody.getContentType(), attachBody.getContentType().startsWith("image/png;"));
					assertEquals(bytes("osorno.png"), bytes((InputStream)attachBody.getContent()));
				}
				assertEquals(2, multipart.getCount());
			}
		});
		final MockMail ma2 = new MockMail("ma2", from, user1.email, ts+"subject html attach", (String)null, TEXT2,
				//new MockDataSource(PackageTest.class, "hallo21.zick", "application/java-vm"),
				//new MockDataSource(CascadingMailSourceTest.class, "hallo22.zock", "application/java-vm"));
				new MockURLDataSource("tree.jpg", "image/jpeg"),
				new MockURLDataSource("dummy.txt", "text/plain"), new MockChecker(){
			public void checkBody(final Message m) throws IOException, MessagingException
			{
				assertTrue(m.getContentType(), m.getContentType().startsWith("multipart/mixed;"));
				final MimeMultipart multipart = (MimeMultipart)m.getContent();
				final BodyPart mainBody = multipart.getBodyPart(0);
				assertEquals("text/html; charset="+CHARSET, mainBody.getContentType());
				assertEquals(TEXT2, mainBody.getContent());
				{
					final BodyPart attachBody = multipart.getBodyPart(1);
					assertEquals("tree.jpg", attachBody.getFileName());
					assertTrue(attachBody.getContentType(), attachBody.getContentType().startsWith("image/jpeg;"));
					assertEquals(bytes("tree.jpg"), bytes((InputStream)attachBody.getContent()));
				}
				{
					final BodyPart attachBody = multipart.getBodyPart(2);
					assertEquals("dummy.txt", attachBody.getFileName());
					assertTrue(attachBody.getContentType(), attachBody.getContentType().startsWith("text/plain;"));
					assertEquals("This is an example file\r\nfor testing attachments\r\nin sendmail.\r\n", (String)attachBody.getContent());
				}
				assertEquals(3, multipart.getCount());
			}
		});
		final MockMail ma3 = new MockMail("ma3", from, user1.email, ts+"subject html+text attach", TEXT1, TEXT2,
				new MockURLDataSource("dummy.txt", "text/plain"),
				new MockURLDataSource("osorno.png", "image/png"), new MockChecker(){
			public void checkBody(final Message m) throws IOException, MessagingException
			{
				assertTrue(m.getContentType(), m.getContentType().startsWith("multipart/mixed;"));
				final MimeMultipart multipart = (MimeMultipart)m.getContent();
				final MimeMultipart mainPart = (MimeMultipart)((MimeBodyPart)multipart.getBodyPart(0)).getContent();
				assertTrue(mainPart.getContentType(), mainPart.getContentType().startsWith("multipart/alternative;"));
				{
					final BodyPart mainText = mainPart.getBodyPart(0);
					assertEquals("text/plain; charset="+CHARSET, mainText.getContentType());
					assertEquals(TEXT1, mainText.getContent());
				}
				{
					final BodyPart mainHtml = mainPart.getBodyPart(1);
					assertEquals("text/html; charset="+CHARSET, mainHtml.getContentType());
					assertEquals(TEXT2, mainHtml.getContent());
				}
				assertEquals(2, mainPart.getCount());
				{
					final BodyPart attachBody = multipart.getBodyPart(1);
					assertEquals("dummy.txt", attachBody.getFileName());
					assertTrue(attachBody.getContentType(), attachBody.getContentType().startsWith("text/plain;"));
					assertEquals("This is an example file\r\nfor testing attachments\r\nin sendmail.\r\n", (String)attachBody.getContent());
				}
				{
					final BodyPart attachBody = multipart.getBodyPart(2);
					assertEquals("osorno.png", attachBody.getFileName());
					assertTrue(attachBody.getContentType(), attachBody.getContentType().startsWith("image/png;"));
					assertEquals(bytes("osorno.png"), bytes((InputStream)attachBody.getContent()));
				}
				assertEquals(3, multipart.getCount());
			}
		});

		final MailSource p = new MailSource()
		{
			boolean done = false;
			
			public Collection<? extends Mail> getMailsToSend(final int maximumResultSize)
			{
				assertEquals(MAXIMUM_RESULT_SIZE, maximumResultSize);
				
				if(done)
					return Collections.<Mail>emptyList();
				
				final ArrayList<MockMail> result = new ArrayList<MockMail>();
				result.add(m1);
				result.add(f1);
				result.add(f2);
				result.add(m2);
				result.add(m3);
				result.add(x12);
				result.add(x13);
				result.add(x23);
				result.add(ma1);
				result.add(ma2);
				result.add(ma3);
				
				done = true;
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
		
		assertEquals(null, m3.failedException);
		assertEquals(1, m3.sentCounter);
		assertEquals(0, m3.failedCounter);
		
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
		
		assertPOP3(user1, new MockMail[]{m1, x12, x13, ma1, ma2, ma3});
		assertPOP3(user2, new MockMail[]{m1, m2, m3, x12, x23});
		assertPOP3(user3, new MockMail[]{m1, x13, x23});
	}
	
	private static final String CHARSET = "UTF-8";
	
	private void assertPOP3(final Account account, final MockMail[] expectedMails)
	{
		final TreeMap<String, MockMail> expectedMessages = new TreeMap<String, MockMail>();
		for(int i = 0; i<expectedMails.length; i++)
		{
			final MockMail m = expectedMails[i];
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
			
			final TreeMap<String, Message> actualMessages = new TreeMap<String, Message>();
			for(int i = 0; i<inboxMessages.length; i++)
			{
				final Message m = inboxMessages[i];
				if(actualMessages.put(m.getSubject(), m)!=null)
					throw new RuntimeException(m.getSubject());
			}
			
			for(final String subject : expectedMessages.keySet())
			{
				final Message m = actualMessages.get(subject);
				final MockMail expected = expectedMessages.get(subject);
				final String message = account.pop3User + " - " + subject;
				
				assertNotNull(message, m);
				assertEquals(message, expected.getSubject(), m.getSubject());
				assertEquals(message, list(new InternetAddress(expected.getFrom())), Arrays.asList(m.getFrom()));
				assertEquals(message, ((expected.getTo()==null)&&(expected.getCarbonCopy()==null)) ? list(new InternetAddress("undisclosed-recipients:;")) : addressList(expected.getTo()), addressList(m.getRecipients(Message.RecipientType.TO)));
				assertEquals(message, addressList(expected.getCarbonCopy()), addressList(m.getRecipients(Message.RecipientType.CC)));
				assertEquals(message, null, addressList(m.getRecipients(Message.RecipientType.BCC)));
				expected.checkBody(m);
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
				{/*IGNORE*/}
			}
			if(store!=null)
			{
				try
				{
					store.close();
				}
				catch(MessagingException e)
				{/*IGNORE*/}
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
				{/*IGNORE*/}
			}
			if(store!=null)
			{
				try
				{
					store.close();
				}
				catch(MessagingException e)
				{/*IGNORE*/}
			}
		}
	}
	
	protected final static byte[] bytes(final String name)
	{
		return bytes(MailSenderTest.class.getResourceAsStream(name));
	}
	
	protected final static byte[] bytes(final InputStream in)
	{
		try
		{
			final byte[] buf = new byte[1024];
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			for(int len = in.read(buf); len>=0; len = in.read(buf))
				baos.write(buf, 0, len);
			return baos.toByteArray();
		}
		catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	void assertEquals(byte[] expected, byte[] actual)
	{
		assertEquals(null, expected, actual);
	}
	
	void assertEquals(String message, byte[] expected, byte[] actual)
	{
		if(!Arrays.equals(expected, actual))
			fail("expected " + Arrays.toString(expected) + ", but was " + Arrays.toString(actual));
	}

	protected final static ArrayList<InternetAddress> addressList(final String[] addresses) throws MessagingException
	{
		if(addresses==null)
			return null;
		
		final ArrayList<InternetAddress> result = new ArrayList<InternetAddress>(addresses.length);
		for(int i = 0; i<addresses.length; i++)
			result.add(new InternetAddress(addresses[i]));
		return result;
	}

	protected final static List<Address> addressList(final Address[] addresses) throws MessagingException
	{
		if(addresses==null)
			return null;
		
		return Arrays.asList(addresses);
	}

	protected final static <T> List<T> list(final T... o)
	{
		return Arrays.asList(o);
	}
	
}
