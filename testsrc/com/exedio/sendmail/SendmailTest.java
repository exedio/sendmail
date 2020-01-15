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

import static java.lang.System.getProperty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.exedio.cope.util.JobContext;
import com.exedio.cope.util.PrefixSource;
import com.exedio.cope.util.Sources;
import java.util.Properties;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;

@Tag("RemoteTest")
public class SendmailTest
{
	protected MailSender mailSender;

	protected String pop3Host;
	protected boolean pop3Debug;

	protected String from;

	@BeforeEach
	void setUpSendMailTest()
	{
		final MailSenderProperties mailSenderProperties = MailSenderProperties.factory().create(
				PrefixSource.wrap(
						Sources.SYSTEM_PROPERTIES,
						"smtp."));

		mailSender = mailSenderProperties.get();
		pop3Host=getProperty("pop3.host");
		pop3Debug=getPropertyBoolean("pop3.debug");

		from=getProperty("from");
	}

	protected static class Account
	{
		final String email;
		final String pop3User;
		final String pop3Password;

		Account(final String name)
		{
			email=       getProperty(name+".email");
			pop3User=    getProperty(name+".pop3.user");
			pop3Password=getProperty(name+".pop3.password");

			if(email==null)
				throw new RuntimeException(name);
			if(pop3User==null)
				throw new RuntimeException(name);
			if(pop3Password==null)
				throw new RuntimeException(name);
		}
	}

	private static boolean getPropertyBoolean(final String key)
	{
		final String value = getProperty(key);
		if("true".equals(value))
			return true;
		else if("false".equals(value))
			return false;
		else
			throw new IllegalArgumentException("illegal boolean for key " + key + ": >" + value + '<');
	}

	protected final Session getPOP3Session(final Account account)
	{
		final Properties sessionProperties = new Properties();
		sessionProperties.setProperty("mail.pop3.host", pop3Host);
		sessionProperties.setProperty("mail.pop3.user", account.pop3User);
		final Session session = Session.getInstance(sessionProperties);
		if(pop3Debug)
			session.setDebug(true);
		return session;
	}

	protected final Store getPOP3Store(final Session session, final Account account) throws MessagingException
	{
		try
		{
			final Store store = session.getStore(new URLName("pop3://" + account.pop3User + ":" + account.pop3Password + "@" + pop3Host + "/INBOX"));
			store.connect();
			return store;
		}
		catch(final NoSuchProviderException e)
		{
			throw new RuntimeException(e);
		}
	}

	protected final void cleanPOP3Account(final Account account) throws MessagingException
	{
		final Session session = getPOP3Session(account);
		try (final Store store = getPOP3Store(session, account);
			  final InboxFolderWrapper inboxFolderWrapper = new InboxFolderWrapper(store, true))
		{
			final Folder inboxFolder = inboxFolderWrapper.getInboxFolder();
			inboxFolder.open(Folder.READ_WRITE);
			final Message[] inboxMessages = inboxFolder.getMessages();
			//System.out.println("--------removing "+inboxMessages.length+" messages --------");
			for(final Message message : inboxMessages)
			{
				//System.out.println("-----------------removing message "+i);
				message.setFlag(Flags.Flag.DELETED, true);
			}
		}
	}

	protected final void sendMail(final MailData mail) throws MessagingException
	{
		mailSender.sendMail(mail);
	}

	@SuppressWarnings("deprecation") // OK: testing MailSource API
	protected final void sendMails(
			final MailSource source,
			final int maximumResultSize,
			final JobContext ctx)
	{
		mailSender.sendMails(source, maximumResultSize, ctx);
	}

	protected static final class InboxFolderWrapper implements AutoCloseable
	{
		private final Folder inboxFolder;
		private final boolean expungeOnClose;

		public InboxFolderWrapper(final Store store, final boolean expungeOnClose) throws MessagingException
		{
			assertNotNull(store);
			assertTrue(store.isConnected());
			final Folder defaultFolder = store.getDefaultFolder();
			assertEquals("", defaultFolder.getFullName());
			this.inboxFolder = defaultFolder.getFolder("INBOX");
			assertEquals("INBOX", inboxFolder.getFullName());
			this.expungeOnClose = expungeOnClose;
		}

		public Folder getInboxFolder()
		{
			return inboxFolder;
		}

		@Override
		public void close() throws MessagingException
		{
			inboxFolder.close(expungeOnClose);
		}
	}
}
