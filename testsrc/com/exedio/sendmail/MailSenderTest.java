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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.exedio.cope.util.Hex;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.activation.DataSource;
import javax.activation.URLDataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"HardcodedLineSeparator", "RedundantCast"}) // OK: just a test
public class MailSenderTest extends SendmailTest
{

	private Account user1;
	private Account user2;
	private Account user3;

	private String fail;
	String timeStamp;

	private static final boolean countDebug = false;

	@BeforeEach
	public void setUp() throws Exception
	{
		user1 = new Account("user1");
		user2 = new Account("user2");
		user3 = new Account("user3");

		fail=System.getProperty("fail");

		cleanPOP3Account(user1);
		cleanPOP3Account(user2);
		cleanPOP3Account(user3);

		final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S ", Locale.ENGLISH);
		timeStamp = df.format(new Date());
	}

	public static final String[] ta(final String s)
	{
		return s==null ? null : new String[]{s};
	}

	@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType") // OK: just a test
	private final class MockMail implements Mail
	{
		private final MailChecker checker;
		private final String id;
		private final String[] to;
		private final String[] cc;
		private final String[] bcc;
		private final String textPlain;
		private final String textHtml;
		private final DataSource[] attachments;
		private final String charset;
		boolean specialMessageID = false;
		private final long timestamp;

		int sentCounter = 0;
		int failedCounter = 0;
		Exception failedException = null;

		MockMail(
				final String id,
				final String to,
				final String textPlain,
				final MailChecker checker)
		{
			this(id, ta(to), null, null, textPlain, (String)null, (DataSource[])null, null, checker);
		}

		MockMail(
				final String id,
				final String to,
				final String textPlain,
				final String textHtml,
				final MailChecker checker)
		{
			this(id, ta(to), null, null, textPlain, textHtml, (DataSource[])null, null, checker);
		}

		MockMail(
				final String id,
				final String to,
				final String textPlain,
				final String textHtml,
				final String charset,
				final MailChecker checker)
		{
			this(id, ta(to), null, null, textPlain, textHtml, (DataSource[])null, charset, checker);
		}

		MockMail(
				final String id,
				final String[] to,
				final String[] cc,
				final String[] bcc,
				final String textPlain,
				final MailChecker checker)
		{
			this(id, to, cc, bcc, textPlain, (String)null, (DataSource[])null, null, checker);
		}

		MockMail(
				final String id,
				final String to,
				final String textPlain,
				final DataSource attachment,
				final MailChecker checker)
		{
			this(id, ta(to), null, null, textPlain, (String)null, new DataSource[]{attachment}, null, checker);
		}

		MockMail(
				final String id,
				final String to,
				final String textPlain,
				final String textHtml,
				final DataSource attachment1,
				final DataSource attachment2,
				final MailChecker checker)
		{
			this(id, ta(to), null, null, textPlain, textHtml, new DataSource[]{attachment1, attachment2}, null, checker);
		}

		MockMail(
				final String id,
				final String[] to,
				final String[] cc,
				final String[] bcc,
				final String textPlain,
				final String textHtml,
				final DataSource[] attachments,
				final String charset,
				final MailChecker checker)
		{
			if(checker==null)
				throw new RuntimeException("checker must not be null");
			if(id==null)
				throw new RuntimeException("id must not be null");
			if(to!=null && textPlain==null && textHtml==null)
				throw new NullPointerException("both textPlain and textAsHtml is null");

			this.checker = checker;
			this.id = id;
			this.to = to;
			this.cc = cc;
			this.bcc = bcc;
			this.textPlain = textPlain;
			this.textHtml = textHtml;
			this.attachments = attachments;
			this.charset = charset;
			this.timestamp = System.currentTimeMillis();
		}

		@Override
		public String getMessageID()
		{
			return specialMessageID ? "messageid-" + id + '-' + timestamp : null;
		}

		@Override
		public String getFrom()
		{
			return from;
		}

		@Override
		public String[] getTo()
		{
			return to;
		}

		@Override
		public String[] getCarbonCopy()
		{
			return cc;
		}

		@Override
		public String[] getBlindCarbonCopy()
		{
			return bcc;
		}

		@Override
		public String getSubject()
		{
			return timeStamp + "subject " + ("ISO-8859-1".equals(charset)?NON_ASCII_TEXT_ISO:NON_ASCII_TEXT) + '[' + id + ']' ;
		}

		@Override
		public String getTextPlain()
		{
			return textPlain;
		}

		@Override
		public String getTextHtml()
		{
			return textHtml;
		}

		@Override
		public DataSource[] getAttachments()
		{
			return attachments;
		}

		@Override
		public String getCharset()
		{
			return charset;
		}

		@Override
		public String getContentTransferEncoding()
		{
			return null;
		}

		@Override
		public Date getDate()
		{
			return new Date(timestamp);
		}

		@Override
		public void notifySent()
		{
			sentCounter++;
		}

		@Override
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
			checker.checkBody(m);
		}

		@Override
		public String[] getReplyTo()
		{
			return null;
		}

	}

	@SuppressWarnings("unused")
	private class ArgumentsBuilder
	{
		private String id;
		private String[] to;
		private String[] cc = null;
		private String[] bcc = null;
		private String[] replyTo = null;
		private String textPlain;
		private String textHtml = (String) null;
		private DataSource[] attachments;
		private String charset = null;
		private String contentTransferEncoding = null;
		private MailChecker mailChecker = actual -> fail("should not be sent");
		private ExceptionChecker exceptionChecker = e -> fail();
		private final List<URI> listHelp = new ArrayList<>();
		private final List<URI> listUnsubscribe = new ArrayList<>();
		private final List<URI> listSubscribe = new ArrayList<>();
		private final List<URI> listPost = new ArrayList<>();
		private final List<URI> listOwner = new ArrayList<>();
		private final List<URI> listArchive = new ArrayList<>();
		private boolean listNoPost = false;

		private <T> T[] emptyToNull(final T[] o)
		{
			return o == null || o.length == 0 ? null : o;
		}

		private ArgumentsBuilder id(final String id)
		{
			this.id = id;
			return this;
		}

		private ArgumentsBuilder to(final String... to)
		{
			this.to = emptyToNull(to);
			return this;
		}

		private ArgumentsBuilder cc(final String... cc)
		{
			this.cc = emptyToNull(cc);
			return this;
		}

		private ArgumentsBuilder bcc(final String... bcc)
		{
			this.bcc = emptyToNull(bcc);
			return this;
		}

		private ArgumentsBuilder replyTo(final String... replyTo)
		{
			this.replyTo = emptyToNull(replyTo);
			return this;
		}

		private ArgumentsBuilder textPlain(final String textPlain)
		{
			this.textPlain = textPlain;
			return this;
		}

		private ArgumentsBuilder textHtml(final String textHtml)
		{
			this.textHtml = textHtml;
			return this;
		}

		private ArgumentsBuilder attachments(final DataSource... attachments)
		{
			this.attachments = emptyToNull(attachments);
			return this;
		}

		private ArgumentsBuilder charset(final String charset)
		{
			this.charset = charset;
			return this;
		}

		private ArgumentsBuilder contentTransferEncoding(final String contentTransferEncoding)
		{
			this.contentTransferEncoding = contentTransferEncoding;
			return this;
		}

		private ArgumentsBuilder mailChecker(final MailChecker mailChecker)
		{
			this.mailChecker = mailChecker;
			return this;
		}

		private ArgumentsBuilder exceptionChecker(final ExceptionChecker exceptionChecker)
		{
			this.exceptionChecker = exceptionChecker;
			return this;
		}

		private ArgumentsBuilder listHelp(final URI listHelp)
		{
			this.listHelp.add(listHelp);
			return this;
		}

		private ArgumentsBuilder listUnsubscribe(final URI listUnsubscribe)
		{
			this.listUnsubscribe.add(listUnsubscribe);
			return this;
		}

		private ArgumentsBuilder listSubscribe(final URI listSubscribe)
		{
			this.listSubscribe.add(listSubscribe);
			return this;
		}

		private ArgumentsBuilder listPost(final URI listPost)
		{
			this.listPost.add(listPost);
			return this;
		}

		private ArgumentsBuilder listNoPost()
		{
			this.listNoPost = true;
			return this;
		}

		private ArgumentsBuilder listOwner(final URI listOwner)
		{
			this.listOwner.add(listOwner);
			return this;
		}

		private ArgumentsBuilder listArchive(final URI listArchive)
		{
			this.listArchive.add(listArchive);
			return this;
		}

		private void execute() throws InterruptedException, MessagingException, IOException
		{
			if(to != null && textPlain == null && textHtml == null)
				throw new NullPointerException("both textPlain and textAsHtml is null");
			final long timestamp = System.currentTimeMillis();
			sendAndTest(
					timeStamp + "subject " + ("ISO-8859-1".equals(charset) ? NON_ASCII_TEXT_ISO : NON_ASCII_TEXT) + '[' + id + ']', //Subject
					to,
					cc,
					bcc,
					replyTo,
					id != null ? "messageid-" + id + '-' + timestamp : null,
					new Date(timestamp),
					textPlain,
					textHtml,
					attachments,
					charset,
					contentTransferEncoding,
					listHelp, listUnsubscribe, listSubscribe, listPost, listOwner, listArchive, listNoPost, mailChecker,
					exceptionChecker
			);
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

		MockURLDataSource(final String resource, final String name, final String contentType)
		{
			super(MailSenderTest.class.getResource(resource));
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

	@FunctionalInterface
	private interface MailChecker
	{
		void checkBody(Message m) throws IOException, MessagingException;

		MailChecker CHECK_NOTHING = m -> {
		};
	}

	@FunctionalInterface
	private interface ExceptionChecker
	{
		void checkException(final Exception e);
	}

	private static final int MAXIMUM_RESULT_SIZE = 345;

	private static final String NEWLINES =
		"lf-\n" +
		"cr-\r" +
		"crlf-\r\n";
	private static final String NEWLINES_RECEIVE =
		"lf-\r\n" +
		"cr-\r\n" +
		"crlf-\r\n";
	private static final String NON_ASCII_TEXT =
		" ((utf " +
		"auml-\u00e4 " +
		"ouml-\u00f6 " +
		"uuml-\u00fc " +
		NEWLINES +
		"szlig-\u00df " +
		"abreve-\u0102 " +
		"hebrew-\u05d8 " +
		"euro-\u20ac " +
		"aelig-\u00e6))";
	private static final String NON_ASCII_TEXT_ISO =
		" ((iso " +
		"auml-\u00e4 " +
		"ouml-\u00f6 " +
		"uuml-\u00fc " +
		NEWLINES +
		"szlig-\u00df))";
	private static final String TEXT_APPENDIX = "\r\n";
	private static final String TEXT_PLAIN = "text for test mail" + NON_ASCII_TEXT;
	private static final String TEXT_PLAIN_ISO = "text for test mail" + NON_ASCII_TEXT_ISO;
	private static final String TEXT_HTML =
		"<html>" +
		"<head>" +
		"<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">" +
		"</head>" +
		"<body>" +
		"text with html features " +
		"such as <b>bold</b>, <i>italic</i> and <font color=\"#FF0000\">red</font> text " +
		"and special characters" + NON_ASCII_TEXT + "." +
		"</body>" +
		"</html>";
	private static final String TEXT_HTML_ISO =
		"<html>" +
		"<head>" +
		"<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">" +
		"</head>" +
		"<body>" +
		"text with html features " +
		"such as <b>bold</b>, <i>italic</i> and <font color=\"#FF0000\">red</font> text " +
		"and special characters" + NON_ASCII_TEXT_ISO + "." +
		"</body>" +
		"</html>";

	@SuppressWarnings("NestedAssignment")
	@Test
	void testSendMail() throws InterruptedException, IOException, MessagingException
	{
		final MockMail f1  = new MockMail("f1", fail, "text for failure test mail", actual -> fail("should not be sent"));
		final MockMail f2  = new MockMail("f2", (String)null, null, actual -> fail("should not be sent"));
		final MockMail x12 = new MockMail("x12", new String[]{user1.email, user2.email}, null, null, TEXT_PLAIN, m ->
		{
				assertEquals("text/plain; charset="+DEFAULT_CHARSET, m.getContentType());
				assertEqualsHex(replaceNewlines(TEXT_PLAIN) + TEXT_APPENDIX, m.getContent());
		});
		final MockMail x13 = new MockMail("x13", null, new String[]{user1.email, user3.email}, null, TEXT_PLAIN, m ->
		{
				assertEquals("text/plain; charset="+DEFAULT_CHARSET, m.getContentType());
				assertEqualsHex(replaceNewlines(TEXT_PLAIN) + TEXT_APPENDIX, m.getContent());
		});
		final MockMail x14 = new MockMail("x14", null, new String[]{user1.email, user3.email}, null, TEXT_PLAIN_ISO, null, null, "ISO-8859-1", m ->
		{
				assertEquals("text/plain; charset=ISO-8859-1", m.getContentType());
				assertEqualsHex(replaceNewlines(TEXT_PLAIN_ISO) + TEXT_APPENDIX, m.getContent());
		});
		final MockMail x23 = new MockMail("x23", null, null, new String[]{user2.email, user3.email}, TEXT_PLAIN, m ->
		{
				assertEquals("text/plain; charset="+DEFAULT_CHARSET, m.getContentType());
				assertEqualsHex(replaceNewlines(TEXT_PLAIN) + TEXT_APPENDIX, m.getContent());
		});
		final MockMail mp  = new MockMail("mp", user1.email, TEXT_PLAIN, m ->
		{
				assertEquals("text/plain; charset="+DEFAULT_CHARSET, m.getContentType());
				assertEqualsHex(replaceNewlines(TEXT_PLAIN) + TEXT_APPENDIX, m.getContent());
				assertEquals(null, m.getDisposition());
		});
		final MockMail mh  = new MockMail("mh", user1.email, (String)null, TEXT_HTML, m ->
		{
				assertEquals("text/html; charset="+DEFAULT_CHARSET, m.getContentType());
				assertEqualsHex(replaceNewlines(TEXT_HTML) + TEXT_APPENDIX, m.getContent());
				assertEquals(null, m.getDisposition());
		});
		mh.specialMessageID = true;
		final MockMail ma  = new MockMail("ma", user1.email, TEXT_PLAIN, TEXT_HTML, m ->
		{
				assertTrue(m.getContentType().startsWith("multipart/alternative;"), m.getContentType());
				final MimeMultipart multipart = (MimeMultipart)m.getContent();
				{
					final BodyPart textBody = multipart.getBodyPart(0);
					assertEquals("text/plain; charset="+DEFAULT_CHARSET, textBody.getContentType());
					assertEquals(replaceNewlines(TEXT_PLAIN), textBody.getContent());
					assertEquals(Part.INLINE, textBody.getDisposition());
				}
				{
					final BodyPart htmlBody = multipart.getBodyPart(1);
					assertEquals("text/html; charset="+DEFAULT_CHARSET, htmlBody.getContentType());
					assertEquals(replaceNewlines(TEXT_HTML), htmlBody.getContent());
					assertEquals(Part.INLINE, htmlBody.getDisposition());
				}
				assertEquals(2, multipart.getCount());
		});
		final MockMail ma2  = new MockMail("ma2", user1.email, TEXT_PLAIN_ISO, TEXT_HTML_ISO, "ISO-8859-1", m ->
		{
				assertTrue(m.getContentType().startsWith("multipart/alternative;"), m.getContentType());
				final MimeMultipart multipart = (MimeMultipart)m.getContent();
				{
					final BodyPart textBody = multipart.getBodyPart(0);
					assertEquals("text/plain; charset=ISO-8859-1", textBody.getContentType());
					assertEqualsHex(replaceNewlines(TEXT_PLAIN_ISO), textBody.getContent());
					assertEquals(Part.INLINE, textBody.getDisposition());
				}
				{
					final BodyPart htmlBody = multipart.getBodyPart(1);
					assertEquals("text/html; charset=ISO-8859-1", htmlBody.getContentType());
					assertEqualsHex(replaceNewlines(TEXT_HTML_ISO), htmlBody.getContent());
					assertEquals(Part.INLINE, htmlBody.getDisposition());
				}
				assertEquals(2, multipart.getCount());
		});
		final MockMail mpa = new MockMail("mpa", user1.email, TEXT_PLAIN,
				//new MockDataSource(MailSenderTest.class, "hallo1.class", "application/java-vm"));
				new MockURLDataSource("osorno.png", "osorno.png", "image/png"), m ->
			{
				assertTrue(m.getContentType().startsWith("multipart/mixed;"), m.getContentType());
				final MimeMultipart multipart = (MimeMultipart)m.getContent();
				final BodyPart mainBody = multipart.getBodyPart(0);
				assertEquals("text/plain; charset="+DEFAULT_CHARSET, mainBody.getContentType());
				assertEqualsHex(replaceNewlines(TEXT_PLAIN), mainBody.getContent());
				assertEquals(Part.INLINE, mainBody.getDisposition());
				{
					final BodyPart attachBody = multipart.getBodyPart(1);
					assertEquals("osorno.png", attachBody.getFileName());
					assertEquals("image/png; name=osorno.png", attachBody.getContentType());
					assertArrayEquals(bytes("osorno.png"), bytes((InputStream)attachBody.getContent()));
					assertEquals(Part.ATTACHMENT, attachBody.getDisposition());
				}
				assertEquals(2, multipart.getCount());
			});
		final MockMail mha = new MockMail("mha", user1.email, (String)null, TEXT_HTML,
				//new MockDataSource(PackageTest.class, "hallo21.zick", "application/java-vm"),
				//new MockDataSource(CascadingMailSourceTest.class, "hallo22.zock", "application/java-vm"));
				new MockURLDataSource("tree.jpg", null, "image/jpeg"),
				new MockURLDataSource("dummy.txt", "dummyname.txt", "text/plain"), m ->
			{
				assertTrue(m.getContentType().startsWith("multipart/mixed;"), m.getContentType());
				final MimeMultipart multipart = (MimeMultipart)m.getContent();
				final BodyPart mainBody = multipart.getBodyPart(0);
				assertEquals("text/html; charset="+DEFAULT_CHARSET, mainBody.getContentType());
				assertEqualsHex(replaceNewlines(TEXT_HTML), mainBody.getContent());
				assertEquals(Part.INLINE, mainBody.getDisposition());
				{
					final BodyPart attachBody = multipart.getBodyPart(1);
					assertEquals(null, attachBody.getFileName());
					assertEquals("image/jpeg", attachBody.getContentType());
					assertArrayEquals(bytes("tree.jpg"), bytes((InputStream)attachBody.getContent()));
					assertEquals(Part.ATTACHMENT, attachBody.getDisposition());
				}
				{
					final BodyPart attachBody = multipart.getBodyPart(2);
					assertEquals("dummyname.txt", attachBody.getFileName());
					assertEquals("text/plain; charset=us-ascii; name=dummyname.txt", attachBody.getContentType());
					assertEquals("This is an example file\r\nfor testing attachments\r\nin sendmail.\r\n", (String)attachBody.getContent());
					assertEquals(Part.ATTACHMENT, attachBody.getDisposition());
				}
				assertEquals(3, multipart.getCount());
			});
		final MockMail maa = new MockMail("maa", user1.email, TEXT_PLAIN, TEXT_HTML,
				new MockURLDataSource("dummy.txt", "dummy.txt", "text/plain"),
				new MockURLDataSource("osorno.png", "osorno.png", "image/png"), m ->
			{
				assertTrue(m.getContentType().startsWith("multipart/mixed;"), m.getContentType());
				final MimeMultipart multipart = (MimeMultipart)m.getContent();
				final MimeMultipart mainPart = (MimeMultipart)((MimeBodyPart)multipart.getBodyPart(0)).getContent();
				assertTrue(mainPart.getContentType().startsWith("multipart/alternative;"), mainPart.getContentType());
				{
					final BodyPart mainText = mainPart.getBodyPart(0);
					assertEquals("text/plain; charset="+DEFAULT_CHARSET, mainText.getContentType());
					assertEqualsHex(replaceNewlines(TEXT_PLAIN), mainText.getContent());
					assertEquals(Part.INLINE, mainText.getDisposition());
				}
				{
					final BodyPart mainHtml = mainPart.getBodyPart(1);
					assertEquals("text/html; charset="+DEFAULT_CHARSET, mainHtml.getContentType());
					assertEqualsHex(replaceNewlines(TEXT_HTML), mainHtml.getContent());
					assertEquals(Part.INLINE, mainHtml.getDisposition());
				}
				assertEquals(2, mainPart.getCount());
				{
					final BodyPart attachBody = multipart.getBodyPart(1);
					assertEquals("dummy.txt", attachBody.getFileName());
					assertEquals("text/plain; charset=us-ascii; name=dummy.txt", attachBody.getContentType());
					assertEquals("This is an example file\r\nfor testing attachments\r\nin sendmail.\r\n", (String)attachBody.getContent());
					assertEquals(Part.ATTACHMENT, attachBody.getDisposition());
				}
				{
					final BodyPart attachBody = multipart.getBodyPart(2);
					assertEquals("osorno.png", attachBody.getFileName());
					assertEquals("image/png; name=osorno.png", attachBody.getContentType());
					assertArrayEquals(bytes("osorno.png"), bytes((InputStream)attachBody.getContent()));
					assertEquals(Part.ATTACHMENT, attachBody.getDisposition());
				}
				assertEquals(3, multipart.getCount());
			});

		@SuppressWarnings("deprecation") // OK: testing MailSource API
		final MailSource p = new MailSource()
		{
			boolean done = false;

			@Override
			public Collection<? extends Mail> getMailsToSend(final int maximumResultSize)
			{
				assertEquals(MAXIMUM_RESULT_SIZE, maximumResultSize);

				if(done)
					return Collections.emptyList();

				final ArrayList<MockMail> result = new ArrayList<>();
				result.add(mp);
				result.add(f1);
				result.add(f2);
				result.add(mh);
				result.add(ma);
				result.add(ma2);
				result.add(x12);
				result.add(x13);
				result.add(x14);
				result.add(x23);
				result.add(mpa);
				result.add(mha);
				result.add(maa);

				done = true;
				return result;
			}
		};
		final CountJobContext ctx = new CountJobContext();
		sendMails(p, MAXIMUM_RESULT_SIZE, ctx);
		assertEquals(11, ctx.progress);

		assertEquals(null, mp.failedException);
		assertEquals(1, mp.sentCounter);
		assertEquals(0, mp.failedCounter);

		final String fm1 = f1.failedException.getMessage();
		assertEquals("Invalid Addresses", fm1);
		final String fm1n = f1.failedException.getCause().getMessage();
		assertTrue(fm1n.contains(fail), fm1n + "--------" + fail);
		assertEquals(0, f1.sentCounter);
		assertEquals(1, f1.failedCounter);

		assertEquals(NullPointerException.class, f2.failedException.getClass());
		assertEquals(0, f2.sentCounter);
		assertEquals(1, f2.failedCounter);

		assertEquals(null, mh.failedException);
		assertEquals(1, mh.sentCounter);
		assertEquals(0, mh.failedCounter);

		assertEquals(null, ma.failedException);
		assertEquals(1, ma.sentCounter);
		assertEquals(0, ma.failedCounter);

		assertEquals(null, ma2.failedException);
		assertEquals(1, ma2.sentCounter);
		assertEquals(0, ma2.failedCounter);

		assertEquals(null, x12.failedException);
		assertEquals(1, x12.sentCounter);
		assertEquals(0, x12.failedCounter);

		assertEquals(null, x13.failedException);
		assertEquals(1, x13.sentCounter);
		assertEquals(0, x13.failedCounter);

		assertEquals(null, x14.failedException);
		assertEquals(1, x14.sentCounter);
		assertEquals(0, x14.failedCounter);

		assertEquals(null, x23.failedException);
		assertEquals(1, x23.sentCounter);
		assertEquals(0, x23.failedCounter);

		assertEquals(null, mpa.failedException);
		assertEquals(1, mpa.sentCounter);
		assertEquals(0, mpa.failedCounter);

		assertEquals(null, mha.failedException);
		assertEquals(1, mha.sentCounter);
		assertEquals(0, mha.failedCounter);

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
			//noinspection ConstantConditions
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

		assertPOP3(user1, mp, mh, ma, ma2, x12, x13, x14, mpa, mha, maa);
		assertPOP3(user2, x12, x23);
		assertPOP3(user3, x13, x14, x23);
	}

	@Test
	void testUnknownRecipient() throws InterruptedException, MessagingException, IOException
	{
		new ArgumentsBuilder().to(fail).textPlain("text for failure test mail").exceptionChecker(e -> {
			final String fm1 = e.getMessage();
			assertEquals("Invalid Addresses", fm1);
			final String fm1n = e.getCause().getMessage();
			assertTrue(fm1n.contains(fail), fm1n + "--------" + fail);
		}).execute();
	}

	@Test
	void testNoRecipients() throws InterruptedException, MessagingException, IOException
	{
		new ArgumentsBuilder().exceptionChecker(e -> assertEquals(NullPointerException.class, e.getClass())).execute();
	}

	@Test
	void testPlainTextToUser1AndUser2() throws InterruptedException, MessagingException, IOException
	{
		new ArgumentsBuilder().to(user1.email, user2.email).textPlain(TEXT_PLAIN).mailChecker(m ->
		{
			assertEquals("text/plain; charset=" + DEFAULT_CHARSET, m.getContentType());
			assertEqualsHex(replaceNewlines(TEXT_PLAIN) + TEXT_APPENDIX, m.getContent());
		}).execute();
	}

	@Test
	void testPlainTextToUser1AndUser3() throws InterruptedException, MessagingException, IOException
	{
		new ArgumentsBuilder().cc(user1.email, user3.email).textPlain(TEXT_PLAIN).mailChecker(m ->
		{
			assertEquals("text/plain; charset=" + DEFAULT_CHARSET, m.getContentType());
			assertEqualsHex(replaceNewlines(TEXT_PLAIN) + TEXT_APPENDIX, m.getContent());
		}).execute();
	}

	@Test
	void testPlainTextToUser1AndUser3WithISOCharset() throws InterruptedException, MessagingException, IOException
	{
		new ArgumentsBuilder().cc(user1.email, user3.email).textPlain(TEXT_PLAIN_ISO).charset("ISO-8859-1").mailChecker(m ->
		{
			assertEquals("text/plain; charset=ISO-8859-1", m.getContentType());
			assertEqualsHex(replaceNewlines(TEXT_PLAIN_ISO) + TEXT_APPENDIX, m.getContent());
		}).execute();
	}

	@Test
	void testPlainTextToUser2AndUser3() throws InterruptedException, MessagingException, IOException
	{
		new ArgumentsBuilder().bcc(user2.email, user3.email).textPlain(TEXT_PLAIN).mailChecker(m ->
		{
			assertEquals("text/plain; charset=" + DEFAULT_CHARSET, m.getContentType());
			assertEqualsHex(replaceNewlines(TEXT_PLAIN) + TEXT_APPENDIX, m.getContent());
		}).execute();
	}

	@Test
	void testPlainTextToUser1() throws InterruptedException, MessagingException, IOException
	{
		new ArgumentsBuilder().to(user1.email).textPlain(TEXT_PLAIN).mailChecker(m ->
		{
			assertEquals("text/plain; charset=" + DEFAULT_CHARSET, m.getContentType());
			assertEqualsHex(replaceNewlines(TEXT_PLAIN) + TEXT_APPENDIX, m.getContent());
			assertEquals(null, m.getDisposition());
		}).execute();
	}

	@Test
	void testHTMLToUser1WithSpecialMessageID() throws InterruptedException, MessagingException, IOException
	{
		new ArgumentsBuilder().id("mh").to(user1.email).textHtml(TEXT_HTML).mailChecker(m ->
		{
			assertEquals("text/html; charset=" + DEFAULT_CHARSET, m.getContentType());
			assertEqualsHex(replaceNewlines(TEXT_HTML) + TEXT_APPENDIX, m.getContent());
			assertEquals(null, m.getDisposition());
		}).execute();
	}

	@Test
	void testPlainTextAndHTMLToUser1() throws InterruptedException, MessagingException, IOException
	{
		new ArgumentsBuilder().to(user1.email).textPlain(TEXT_PLAIN).textHtml(TEXT_HTML).mailChecker(m ->
		{
			assertTrue(m.getContentType().startsWith("multipart/alternative;"), m.getContentType());
			final MimeMultipart multipart = (MimeMultipart) m.getContent();
			{
				final BodyPart textBody = multipart.getBodyPart(0);
				assertEquals("text/plain; charset=" + DEFAULT_CHARSET, textBody.getContentType());
				assertEquals(replaceNewlines(TEXT_PLAIN), textBody.getContent());
				assertEquals(Part.INLINE, textBody.getDisposition());
			}
			{
				final BodyPart htmlBody = multipart.getBodyPart(1);
				assertEquals("text/html; charset=" + DEFAULT_CHARSET, htmlBody.getContentType());
				assertEquals(replaceNewlines(TEXT_HTML), htmlBody.getContent());
				assertEquals(Part.INLINE, htmlBody.getDisposition());
			}
			assertEquals(2, multipart.getCount());
		}).execute();
	}

	@Test
	void testPlainTextAndHTMLToUser1WithISOCharset() throws InterruptedException, MessagingException, IOException
	{
		new ArgumentsBuilder().to(user1.email).textPlain(TEXT_PLAIN_ISO).textHtml(TEXT_HTML_ISO).charset("ISO-8859-1").mailChecker(m ->
		{
			assertTrue(m.getContentType().startsWith("multipart/alternative;"), m.getContentType());
			final MimeMultipart multipart = (MimeMultipart) m.getContent();
			{
				final BodyPart textBody = multipart.getBodyPart(0);
				assertEquals("text/plain; charset=ISO-8859-1", textBody.getContentType());
				assertEqualsHex(replaceNewlines(TEXT_PLAIN_ISO), textBody.getContent());
				assertEquals(Part.INLINE, textBody.getDisposition());
			}
			{
				final BodyPart htmlBody = multipart.getBodyPart(1);
				assertEquals("text/html; charset=ISO-8859-1", htmlBody.getContentType());
				assertEqualsHex(replaceNewlines(TEXT_HTML_ISO), htmlBody.getContent());
				assertEquals(Part.INLINE, htmlBody.getDisposition());
			}
			assertEquals(2, multipart.getCount());
		}).execute();
	}

	@Test
	void testPlainTextAndOneAttachmentToUser1() throws InterruptedException, MessagingException, IOException
	{
		new ArgumentsBuilder().to(user1.email).textPlain(TEXT_PLAIN).attachments(new MockURLDataSource("osorno.png", "osorno.png", "image/png")).mailChecker(m ->
		{
			assertTrue(m.getContentType().startsWith("multipart/mixed;"), m.getContentType());
			final MimeMultipart multipart = (MimeMultipart) m.getContent();
			final BodyPart mainBody = multipart.getBodyPart(0);
			assertEquals("text/plain; charset=" + DEFAULT_CHARSET, mainBody.getContentType());
			assertEqualsHex(replaceNewlines(TEXT_PLAIN), mainBody.getContent());
			assertEquals(Part.INLINE, mainBody.getDisposition());
			{
				final BodyPart attachBody = multipart.getBodyPart(1);
				assertEquals("osorno.png", attachBody.getFileName());
				assertEquals("image/png; name=osorno.png", attachBody.getContentType());
				assertArrayEquals(bytes("osorno.png"), bytes((InputStream) attachBody.getContent()));
				assertEquals(Part.ATTACHMENT, attachBody.getDisposition());
			}
			assertEquals(2, multipart.getCount());
		}).execute();
	}

	@Test
	void testHTMLWithTwoAttachmentsToUser1() throws InterruptedException, MessagingException, IOException
	{
		new ArgumentsBuilder().to(user1.email).textHtml(TEXT_HTML).attachments(new MockURLDataSource("tree.jpg", null, "image/jpeg"), new MockURLDataSource("dummy.txt", "dummyname.txt", "text/plain")).mailChecker(m ->
		{
			assertTrue(m.getContentType().startsWith("multipart/mixed;"), m.getContentType());
			final MimeMultipart multipart = (MimeMultipart) m.getContent();
			final BodyPart mainBody = multipart.getBodyPart(0);
			assertEquals("text/html; charset=" + DEFAULT_CHARSET, mainBody.getContentType());
			assertEqualsHex(replaceNewlines(TEXT_HTML), mainBody.getContent());
			assertEquals(Part.INLINE, mainBody.getDisposition());
			{
				final BodyPart attachBody = multipart.getBodyPart(1);
				assertEquals(null, attachBody.getFileName());
				assertEquals("image/jpeg", attachBody.getContentType());
				assertArrayEquals(bytes("tree.jpg"), bytes((InputStream) attachBody.getContent()));
				assertEquals(Part.ATTACHMENT, attachBody.getDisposition());
			}
			{
				final BodyPart attachBody = multipart.getBodyPart(2);
				assertEquals("dummyname.txt", attachBody.getFileName());
				assertEquals("text/plain; charset=us-ascii; name=dummyname.txt", attachBody.getContentType());
				assertEquals("This is an example file\r\nfor testing attachments\r\nin sendmail.\r\n", (String) attachBody.getContent());
				assertEquals(Part.ATTACHMENT, attachBody.getDisposition());
			}
			assertEquals(3, multipart.getCount());
		}).execute();
	}

	@Test
	void testPlainTextAndHTMLWithTwoAttachmentsToUser1() throws InterruptedException, MessagingException, IOException
	{
		new ArgumentsBuilder().to(user1.email).textPlain(TEXT_PLAIN).textHtml(TEXT_HTML).attachments(new MockURLDataSource("dummy.txt", "dummy.txt", "text/plain"), new MockURLDataSource("osorno.png", "osorno.png", "image/png")).mailChecker(m ->
		{
			assertTrue(m.getContentType().startsWith("multipart/mixed;"), m.getContentType());
			final MimeMultipart multipart = (MimeMultipart) m.getContent();
			final MimeMultipart mainPart = (MimeMultipart) ((MimeBodyPart) multipart.getBodyPart(0)).getContent();
			assertTrue(mainPart.getContentType().startsWith("multipart/alternative;"), mainPart.getContentType());
			{
				final BodyPart mainText = mainPart.getBodyPart(0);
				assertEquals("text/plain; charset=" + DEFAULT_CHARSET, mainText.getContentType());
				assertEqualsHex(replaceNewlines(TEXT_PLAIN), mainText.getContent());
				assertEquals(Part.INLINE, mainText.getDisposition());
			}
			{
				final BodyPart mainHtml = mainPart.getBodyPart(1);
				assertEquals("text/html; charset=" + DEFAULT_CHARSET, mainHtml.getContentType());
				assertEqualsHex(replaceNewlines(TEXT_HTML), mainHtml.getContent());
				assertEquals(Part.INLINE, mainHtml.getDisposition());
			}
			assertEquals(2, mainPart.getCount());
			{
				final BodyPart attachBody = multipart.getBodyPart(1);
				assertEquals("dummy.txt", attachBody.getFileName());
				assertEquals("text/plain; charset=us-ascii; name=dummy.txt", attachBody.getContentType());
				assertEquals("This is an example file\r\nfor testing attachments\r\nin sendmail.\r\n", (String) attachBody.getContent());
				assertEquals(Part.ATTACHMENT, attachBody.getDisposition());
			}
			{
				final BodyPart attachBody = multipart.getBodyPart(2);
				assertEquals("osorno.png", attachBody.getFileName());
				assertEquals("image/png; name=osorno.png", attachBody.getContentType());
				assertArrayEquals(bytes("osorno.png"), bytes((InputStream) attachBody.getContent()));
				assertEquals(Part.ATTACHMENT, attachBody.getDisposition());
			}
			assertEquals(3, multipart.getCount());
		}).execute();
	}

	@Test
	void testPlainTextAndHTMLWithREPLYTOHeaderToUser1() throws InterruptedException, MessagingException, IOException
	{
		new ArgumentsBuilder().to(user1.email).textPlain(TEXT_PLAIN).textHtml(TEXT_HTML).replyTo("dontuse@something.invalid").mailChecker(MailChecker.CHECK_NOTHING).execute();
	}

	@Test
	void testListHeaders() throws InterruptedException, MessagingException, IOException, URISyntaxException
	{
		new ArgumentsBuilder().to(user1.email).cc(user2.email).bcc(user3.email).textPlain(TEXT_PLAIN).textHtml(TEXT_HTML)
				.listHelp(new URI("http://dontuse.something.invalid/help")).listHelp(new URI("ftp://dontuse.something.invalid/help")).listHelp(new URI("mailto:dontuse@something.invalid?subject=Help"))
				.listUnsubscribe(new URI("http://dontuse.something.invalid/unsubscribe")).listUnsubscribe(new URI("ftp://dontuse.something.invalid/unsubscribe")).listUnsubscribe(new URI("mailto:dontuse@something.invalid?subject=Unsubscribe"))
				.listSubscribe(new URI("http://dontuse.something.invalid/subscribe")).listSubscribe(new URI("ftp://dontuse.something.invalid/subscribe")).listSubscribe(new URI("mailto:dontuse@something.invalid?subject=Subscribe"))
				.listPost(new URI("http://dontuse.something.invalid/post")).listPost(new URI("ftp://dontuse.something.invalid/post")).listPost(new URI("mailto:dontuse@something.invalid?subject=Post"))
				.listOwner(new URI("http://dontuse.something.invalid/owner")).listOwner(new URI("ftp://dontuse.something.invalid/owner")).listOwner(new URI("mailto:dontuse@something.invalid?subject=Owner"))
				.listArchive(new URI("http://dontuse.something.invalid/archive")).listArchive(new URI("ftp://dontuse.something.invalid/archive")).listArchive(new URI("mailto:dontuse@something.invalid?subject=Archive"))
				.mailChecker(MailChecker.CHECK_NOTHING).execute();
	}

	@Test
	void testNoPostListHeaders() throws InterruptedException, MessagingException, IOException, URISyntaxException
	{
		new ArgumentsBuilder().to(user1.email).cc(user2.email).bcc(user3.email).textPlain(TEXT_PLAIN).textHtml(TEXT_HTML)
				.listHelp(new URI("http://dontuse.something.invalid/help")).listHelp(new URI("ftp://dontuse.something.invalid/help")).listHelp(new URI("mailto:dontuse@something.invalid?subject=Help"))
				.listUnsubscribe(new URI("http://dontuse.something.invalid/unsubscribe")).listUnsubscribe(new URI("ftp://dontuse.something.invalid/unsubscribe")).listUnsubscribe(new URI("mailto:dontuse@something.invalid?subject=Unsubscribe"))
				.listSubscribe(new URI("http://dontuse.something.invalid/subscribe")).listSubscribe(new URI("ftp://dontuse.something.invalid/subscribe")).listSubscribe(new URI("mailto:dontuse@something.invalid?subject=Subscribe"))
				.listPost(new URI("http://dontuse.something.invalid/post")).listPost(new URI("ftp://dontuse.something.invalid/post")).listPost(new URI("mailto:dontuse@something.invalid?subject=Post"))
				.listOwner(new URI("http://dontuse.something.invalid/owner")).listOwner(new URI("ftp://dontuse.something.invalid/owner")).listOwner(new URI("mailto:dontuse@something.invalid?subject=Owner"))
				.listArchive(new URI("http://dontuse.something.invalid/archive")).listArchive(new URI("ftp://dontuse.something.invalid/archive")).listArchive(new URI("mailto:dontuse@something.invalid?subject=Archive"))
				.mailChecker(MailChecker.CHECK_NOTHING).execute();
	}

	@Test
	void testConnection() throws InterruptedException, MessagingException, IOException
	{
		try(MailSender.Connection c = mailSender.openConnection())
		{
			send(c, "testConnection1", user1);
			send(c, "testConnection2", user2);
			send(c, "testConnection3a", user3);
			send(c, "testConnection3b", user3);
		}
		boolean complete1 = false;
		boolean complete2 = false;
		boolean complete3 = false;
		for(int i = 0; i<30; i++)
		{
			Thread.sleep(1000);
			//noinspection ConstantConditions,NestedAssignment
			if(
					(complete1 || (complete1=countPOP3(user1, 1))) &&
					(complete2 || (complete2=countPOP3(user2, 1))) &&
					(complete3 || (complete3=countPOP3(user3, 2))) )
			{
				break;
			}
		}

		check(user1, (messages)->
		{
			final Message m = messages.get("testConnection1");
			assertEquals("testConnection1", m.getSubject());
			assertEquals("plain text testConnection1" + TEXT_APPENDIX, m.getContent());
			assertEquals(addressList(from), Arrays.asList(m.getFrom()));
			assertEquals(addressList(user1.email), addressList(m.getRecipients(Message.RecipientType.TO)));
			assertEquals(1, messages.size());
		});
		check(user2, (messages)->
		{
			final Message m = messages.get("testConnection2");
			assertEquals("testConnection2", m.getSubject());
			assertEquals("plain text testConnection2" + TEXT_APPENDIX, m.getContent());
			assertEquals(addressList(from), Arrays.asList(m.getFrom()));
			assertEquals(addressList(user2.email), addressList(m.getRecipients(Message.RecipientType.TO)));
			assertEquals(1, messages.size());
		});
		check(user3, (messages)->
		{
			final Message ma = messages.get("testConnection3a");
			final Message mb = messages.get("testConnection3b");
			assertEquals("testConnection3a", ma.getSubject());
			assertEquals("testConnection3b", mb.getSubject());
			assertEquals("plain text testConnection3a" + TEXT_APPENDIX, ma.getContent());
			assertEquals("plain text testConnection3b" + TEXT_APPENDIX, mb.getContent());
			assertEquals(addressList(from), Arrays.asList(ma.getFrom()));
			assertEquals(addressList(from), Arrays.asList(mb.getFrom()));
			assertEquals(addressList(user3.email), addressList(ma.getRecipients(Message.RecipientType.TO)));
			assertEquals(addressList(user3.email), addressList(mb.getRecipients(Message.RecipientType.TO)));
			assertEquals(2, messages.size());
		});
	}

	private void send(
			final MailSender.Connection connection,
			final String subject,
			final Account account) throws MessagingException
	{
		final MailData data = new MailData(from, subject);
		data.addTo(account.email);
		data.setTextPlain("plain text " + subject);
		connection.send(data);
	}

	@FunctionalInterface
	interface Tester
	{
		void accept(TreeMap<String, Message> t) throws MessagingException, IOException;
	}

	private void check(
			final Account account,
			final Tester tester)
			throws MessagingException, IOException
	{
		final Session session = getPOP3Session(account);
		try(final Store store = getPOP3Store(session, account);
			 final InboxFolderWrapper inboxFolderWrapper = new InboxFolderWrapper(store, false))
		{
			final Folder inboxFolder = inboxFolderWrapper.getInboxFolder();
			inboxFolder.open(Folder.READ_ONLY);
			final TreeMap<String, Message> messages = new TreeMap<>();
			for(final Message m : inboxFolder.getMessages())
				if(messages.put(m.getSubject(), m) != null)
					throw new RuntimeException(m.getSubject());
			tester.accept(messages);
		}
	}

	@SuppressWarnings("MethodOnlyUsedFromInnerClass")
	private void sendAndTest(final String subject,
									 final String[] to,
									 final String[] carbonCopy,
									 final String[] blindCarbonCopy,
									 final String[] replyTo,
									 final String messageID,
									 final Date date,
									 final String textPlain,
									 final String textHtml,
									 final DataSource[] attachments,
									 final String charset,
									 final String contentTransferEncoding,
									 final List<URI> listHelp,
									 final List<URI> listUnsubscribe,
									 final List<URI> listSubscribe,
									 final List<URI> listPost,
									 final List<URI> listOwner,
									 final List<URI> listArchive,
									 final boolean listNoPost, final MailChecker mailChecker,
									 final ExceptionChecker exceptionChecker) throws InterruptedException, MessagingException, IOException
	{
		final boolean erroneous = (to == null && carbonCopy == null && blindCarbonCopy == null) ||
										  (to != null && Arrays.stream(to).anyMatch(s -> ! user1.email.equals(s) && ! user2.email.equals(s) && ! user3.email.equals(s))) ||
										  (carbonCopy != null && Arrays.stream(carbonCopy).anyMatch(s -> ! user1.email.equals(s) && ! user2.email.equals(s) && ! user3.email.equals(s))) ||
										  (blindCarbonCopy != null && Arrays.stream(blindCarbonCopy).anyMatch(s -> ! user1.email.equals(s) && ! user2.email.equals(s) && ! user3.email.equals(s)));
		final MailData mailData = getMailData(subject,
				from,
				to,
				carbonCopy,
				blindCarbonCopy,
				replyTo,
				messageID,
				date,
				textPlain,
				textHtml,
				attachments,
				charset,
				contentTransferEncoding);
		listHelp.forEach(mailData.mailingListHeaders()::addHelp);
		listUnsubscribe.forEach(mailData.mailingListHeaders()::addUnsubscribe);
		listSubscribe.forEach(mailData.mailingListHeaders()::addSubscribe);
		if (listNoPost)
		{
			mailData.mailingListHeaders().setNoPost(true);
		}
		else
		{
			listPost.forEach(mailData.mailingListHeaders()::addPost);
		}
		listOwner.forEach(mailData.mailingListHeaders()::addOwner);
		listArchive.forEach(mailData.mailingListHeaders()::addArchive);
		try
		{
			sendMail(mailData);
			if(erroneous)
			{
				fail("Exception should have been thrown.");
			}
		}
		catch(final Exception e)
		{
			if(! erroneous)
			{
				fail("Exception " + e.getClass().getSimpleName() + " was not expected");
			}
			exceptionChecker.checkException(e);
		}

		boolean complete1 = false;
		boolean complete2 = false;
		boolean complete3 = false;
		Stream<String> stream = null;
		if(to != null)
		{
			stream = Arrays.stream(to);
		}
		if(carbonCopy != null)
		{
			if(stream == null)
			{
				stream = Arrays.stream(carbonCopy);
			}
			else
			{
				stream = Stream.concat(stream, Arrays.stream(carbonCopy));
			}
		}
		if(blindCarbonCopy != null)
		{
			if(stream == null)
			{
				stream = Arrays.stream(blindCarbonCopy);
			}
			else
			{
				stream = Stream.concat(stream, Arrays.stream(blindCarbonCopy));
			}
		}
		final Set<Account> accounts = stream != null ? stream.map(
				s -> user1.email.equals(s) ? user1 :
						user2.email.equals(s) ? user2 :
								user3.email.equals(s) ? user3 : null
		).filter(Objects::nonNull).collect(Collectors.toSet()) : Collections.emptySet();
		for(int i = 0; i < 30; i++)
		{
			Thread.sleep(1000);
			if(countDebug)
			{
				System.out.println();
				System.out.print("---------" + i + "--");
			}
			//noinspection ConstantConditions,NestedAssignment
			if(
					(complete1 || (complete1 = countPOP3(user1, accounts.contains(user1) ? 1 : 0))) &&
					(complete2 || (complete2 = countPOP3(user2, accounts.contains(user2) ? 1 : 0))) &&
					(complete3 || (complete3 = countPOP3(user3, accounts.contains(user3) ? 1 : 0))))
			{
				break;
			}
		}
		if(countDebug)
			System.out.println();

		if(accounts.contains(user1))
		{
			assertPOP3(user1, subject,
					from,
					to,
					carbonCopy,
					replyTo,
					messageID,
					date,
					listHelp,
					listUnsubscribe,
					listSubscribe,
					listPost,
					listOwner,
					listArchive,
					listNoPost, mailChecker);
		}
		else
		{
			assertEmptyPOP3(user1);
		}
		if(accounts.contains(user2))
		{
			assertPOP3(user2, subject,
					from,
					to,
					carbonCopy,
					replyTo,
					messageID,
					date,
					listHelp,
					listUnsubscribe,
					listSubscribe,
					listPost,
					listOwner,
					listArchive,
					listNoPost, mailChecker);
		}
		else
		{
			assertEmptyPOP3(user2);
		}
		if(accounts.contains(user3))
		{
			assertPOP3(user3, subject,
					from,
					to,
					carbonCopy,
					replyTo,
					messageID,
					date,
					listHelp,
					listUnsubscribe,
					listSubscribe,
					listPost,
					listOwner,
					listArchive,
					listNoPost, mailChecker);
		}
		else
		{
			assertEmptyPOP3(user3);
		}
	}

	private static MailData getMailData(final String subject,
													final String from,
													final String[] to,
													final String[] carbonCopy,
													final String[] blindCarbonCopy,
													final String[] replyTo,
													final String id,
													final Date date,
													final String textPlain,
													final String textHtml,
													final DataSource[] attachments,
													final String charset,
													final String contentTransferEncoding) throws MessagingException
	{
		final MailData message = new MailData(from, subject);
		if(id != null)
			message.setMessageID(id);
		if(replyTo != null)
			message.setReplyTo(replyTo);
		if(to != null)
			message.setTo(to);
		if(carbonCopy != null)
			message.setCarbonCopy(carbonCopy);
		if(blindCarbonCopy != null)
			message.setBlindCarbonCopy(blindCarbonCopy);
		message.setDate(date);
		if(textPlain != null)
			message.setTextPlain(textPlain);
		if(textHtml != null)
			message.setTextHtml(textHtml);
		if(attachments != null)
			message.setAttachments(attachments);
		if(charset != null)
			message.setCharset(charset);
		if(contentTransferEncoding != null)
			message.setContentTransferEncoding(contentTransferEncoding);
		return message;
	}

	private static final String DEFAULT_CHARSET = "UTF-8";

	private void assertPOP3(final Account account, final MockMail... expectedMails) throws IOException, MessagingException
	{
		final TreeMap<String, MockMail> expectedMessages = new TreeMap<>();
		for(final MockMail m : expectedMails)
		{
			if(expectedMessages.put(m.getSubject(), m)!=null)
				throw new RuntimeException(m.getSubject());
		}

		final Session session = getPOP3Session(account);
		try(final Store store = getPOP3Store(session, account);
			 final InboxFolderWrapper inboxFolderWrapper = new InboxFolderWrapper(store, false))
		{
			final Folder inboxFolder = inboxFolderWrapper.getInboxFolder();
			inboxFolder.open(Folder.READ_ONLY);
			final Message[] inboxMessages = inboxFolder.getMessages();

			final TreeMap<String, Message> actualMessages = new TreeMap<>();
			for(final Message m : inboxMessages)
			{
				if(actualMessages.put(m.getSubject(), m)!=null)
					throw new RuntimeException(m.getSubject());
			}

			// check that the non-ascii characters in the subject are handled properly:
			for(final Message m : actualMessages.values())
			{
				final String subjectPart = m.getSubject().substring(
					m.getSubject().indexOf(" (("), m.getSubject().indexOf("))")+2
				);
				if ( subjectPart.startsWith(" ((utf") )
				{
					assertEquals(NON_ASCII_TEXT, subjectPart, m.getSubject());
				}
				else if ( subjectPart.startsWith(" ((iso") )
				{
					assertEquals(NON_ASCII_TEXT_ISO, subjectPart, m.getSubject());
				}
				else
				{
					fail( m.getSubject() );
				}
			}

			for(final String subject : expectedMessages.keySet())
			{
				final Message m = actualMessages.get(subject);
				final MockMail expected = expectedMessages.get(subject);
				final String message = account.pop3User + " - " + subject;

				assertNotNull(m, "no message " + message + "; found " + actualMessages.keySet());
				assertEquals(expected.getSubject(), m.getSubject(), message);
				assertEquals(Arrays.asList(new InternetAddress(expected.getFrom())), Arrays.asList(m.getFrom()), message);
				assertEquals(((expected.getTo() == null) && (expected.getCarbonCopy() == null)) ? null : addressList(expected.getTo()), addressList(m.getRecipients(Message.RecipientType.TO)), message);
				assertEquals(addressList(expected.getCarbonCopy()), addressList(m.getRecipients(Message.RecipientType.CC)), message);
				assertEquals(null, addressList(m.getRecipients(Message.RecipientType.BCC)), message);
				assertNotNull(m.getHeader("Message-ID"), message);
				assertEquals(1, m.getHeader("Message-ID").length, message);
				if(expected.specialMessageID)
					assertEquals(expected.getMessageID(), m.getHeader("Message-ID")[0], message);
				else
					assertTrue(m.getHeader("Message-ID")[0].indexOf("@") > 0, message);
				assertNotNull(m.getHeader("Date"), message);
				assertEquals(1, m.getHeader("Date").length, message);
				assertEquals((new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z (z)", new Locale ("en"))).format(expected.getDate()), m.getHeader("Date")[0], message);
				expected.checkBody(m);
			}
			assertEquals(expectedMails.length, inboxMessages.length, account.pop3User);
		}
	}

	private void assertPOP3(final Account account,
									final String subject,
									final String from,
									final String[] to,
									final String[] carbonCopy,
									final String[] replyTo,
									final String messageID,
									final Date date,
									final List<URI> listHelp,
									final List<URI> listUnsubscribe,
									final List<URI> listSubscribe,
									final List<URI> listPost,
									final List<URI> listOwner,
									final List<URI> listArchive,
									final boolean listNoPost, final MailChecker mailChecker) throws IOException, MessagingException
	{
		final Session session = getPOP3Session(account);
		try(final Store store = getPOP3Store(session, account);
			 final InboxFolderWrapper inboxFolderWrapper = new InboxFolderWrapper(store, false))
		{
			final Folder inboxFolder = inboxFolderWrapper.getInboxFolder();
			inboxFolder.open(Folder.READ_ONLY);
			final Message[] inboxMessages = inboxFolder.getMessages();
			assertEquals(1, inboxMessages.length, account.pop3User);

			final TreeMap<String, Message> actualMessages = new TreeMap<>();
			for(final Message m : inboxMessages)
			{
				if(actualMessages.put(m.getSubject(), m) != null)
					throw new RuntimeException(m.getSubject());
			}

			// check that the non-ascii characters in the subject are handled properly:
			for(final Message m : actualMessages.values())
			{
				final String subjectPart = m.getSubject().substring(
						m.getSubject().indexOf(" (("), m.getSubject().indexOf("))") + 2
				);
				if(subjectPart.startsWith(" ((utf"))
				{
					assertEquals(NON_ASCII_TEXT, subjectPart, m.getSubject());
				}
				else if(subjectPart.startsWith(" ((iso"))
				{
					assertEquals(NON_ASCII_TEXT_ISO, subjectPart, m.getSubject());
				}
				else
				{
					fail(m.getSubject());
				}
			}

			final String message = account.pop3User + " - " + subject;
			final Message m = actualMessages.get(subject);
			assertNotNull(m, "no message " + message + "; found " + actualMessages.keySet());
			assertEquals(subject, m.getSubject(), message);
			assertEquals(Arrays.asList(new InternetAddress(from)), Arrays.asList(m.getFrom()), message);
			assertEquals(((to == null) && (carbonCopy == null)) ? null : addressList(to), addressList(m.getRecipients(Message.RecipientType.TO)), message);
			assertEquals(addressList(carbonCopy), addressList(m.getRecipients(Message.RecipientType.CC)), message);
			assertEquals(null, addressList(m.getRecipients(Message.RecipientType.BCC)), message);
			assertNotNull(m.getHeader("Message-ID"), message);
			assertEquals(1, m.getHeader("Message-ID").length, message);
			if(messageID != null)
				assertEquals(messageID, m.getHeader("Message-ID")[0], message);
			else
				assertTrue(m.getHeader("Message-ID")[0].indexOf("@") > 0, message);
			assertNotNull(m.getHeader("Date"), message);
			assertEquals(1, m.getHeader("Date").length, message);
			assertEquals((new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z (z)", new Locale("en"))).format(date), m.getHeader("Date")[0], message);
			final String[] replyToHeader = m.getHeader("Reply-To");
			if(replyTo == null)
			{
				assertNull(replyToHeader, message);
			}
			else
			{
				assertNotNull(replyToHeader, message);
				assertEquals(replyTo.length, replyToHeader.length, message);
				assertArrayEquals(replyTo, replyToHeader, message);
			}
			assertListHeader(m, listHelp, "List-Help", message);
			assertListHeader(m, listUnsubscribe, "List-Unsubscribe", message);
			assertListHeader(m, listSubscribe, "List-Subscribe", message);
			if (listNoPost)
			{
				assertListNoPostHeader(m, message);
			}
			else
			{
				assertListHeader(m, listPost, "List-Post", message);
			}
			assertListHeader(m, listOwner, "List-Owner", message);
			assertListHeader(m, listArchive, "List-Archive", message);
			mailChecker.checkBody(m);
		}
	}

	private static void assertListHeader(final Message m, final List<URI> expected, final String headerName, final String message) throws MessagingException
	{
		final String[] header = m.getHeader(headerName);
		if(expected.isEmpty())
		{
			assertNull(header, message);
		}
		else
		{
			assertNotNull(header, message);
			assertEquals(1, header.length, message);
			assertEquals(expected.stream().map(uri -> "<" + uri.toASCIIString() + ">").collect(Collectors.joining(",")), header[0], message);
		}
	}

	private static void assertListNoPostHeader(final Message m, final String message) throws MessagingException
	{
		final String[] header = m.getHeader("List-Post");
		assertNotNull(header, message);
		assertEquals(1, header.length, message);
		assertEquals("NO", header[0], message);
	}

	private void assertEmptyPOP3(final Account account) throws MessagingException
	{
		final Session session = getPOP3Session(account);
		try(final Store store = getPOP3Store(session, account);
			 final InboxFolderWrapper inboxFolderWrapper = new InboxFolderWrapper(store, false))
		{
			final Folder inboxFolder = inboxFolderWrapper.getInboxFolder();
			inboxFolder.open(Folder.READ_ONLY);
			final Message[] inboxMessages = inboxFolder.getMessages();
			assertEquals(0, inboxMessages.length, account.pop3User);
		}
	}

	private boolean countPOP3(final Account account, final int expected) throws MessagingException
	{
		final Session session = getPOP3Session(account);
		try(final Store store = getPOP3Store(session, account);
			 final InboxFolderWrapper inboxFolderWrapper = new InboxFolderWrapper(store, false))
		{
			final Folder inboxFolder = inboxFolderWrapper.getInboxFolder();
			inboxFolder.open(Folder.READ_ONLY);
			final int inboxMessages = inboxFolder.getMessageCount();

			if(countDebug)
				System.out.print(" "+account.pop3User+":"+inboxMessages+"/"+expected);

			return inboxMessages>=expected;
		}
	}

	protected static final byte[] bytes(final String name)
	{
		return bytes(MailSenderTest.class.getResourceAsStream(name));
	}

	protected static final byte[] bytes(final InputStream in)
	{
		final byte[] buf = new byte[1024];
		try(ByteArrayOutputStream baos = new ByteArrayOutputStream())
		{
			for(int len = in.read(buf); len>=0; len = in.read(buf))
				baos.write(buf, 0, len);
			return baos.toByteArray();
		}
		catch(final IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	protected static final ArrayList<InternetAddress> addressList(final String... addresses) throws MessagingException
	{
		if(addresses==null)
			return null;

		final ArrayList<InternetAddress> result = new ArrayList<>(addresses.length);
		for(final String address : addresses)
			result.add(new InternetAddress(address));
		return result;
	}

	protected static final List<Address> addressList(final Address[] addresses)
	{
		if(addresses==null)
			return null;

		return Arrays.asList(addresses);
	}

	static String replaceNewlines(final String s)
	{
		final int pos = s.indexOf(NEWLINES);
		assertTrue(pos>0);
		return s.substring(0, pos) + NEWLINES_RECEIVE + s.substring(pos+NEWLINES.length());
	}

	static void assertEqualsHex(final String expected, final Object actual)
	{
		final String actualString = (String)actual;
		assertEquals(
				expected, actualString, "\n" + Hex.encodeLower(expected.getBytes(UTF_8)) +
												'\n' + Hex.encodeLower(actualString.getBytes(UTF_8))
		);
	}
}
