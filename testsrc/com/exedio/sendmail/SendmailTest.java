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

import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.URLName;

import junit.framework.TestCase;

import com.sun.mail.pop3.POP3Store;

public class SendmailTest extends TestCase
{
	protected boolean skipTest;

	protected MailSender mailSender;

	protected String pop3Host;
	protected boolean pop3Debug;

	protected String from;

	@Override
	public void setUp() throws Exception
	{
		super.setUp();

		skipTest = getPropertyBoolean("skipRemote");
		if(skipTest)
		{
			System.out.println("Skipping test " + getClass().getName());
			return;
		}

		final String smtpHost = getProperty("smtp.host");
		final boolean smtpDebug = getPropertyBoolean("smtp.debug");

		mailSender = new MailSender(smtpHost, 5000, 5000, smtpDebug);
		pop3Host=getProperty("pop3.host");
		pop3Debug=getPropertyBoolean("pop3.debug");

		from=getProperty("from");
	}

	protected class Account
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

	private static final boolean getPropertyBoolean(final String key)
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
		sessionProperties.put("mail.pop3.host", pop3Host);
		sessionProperties.put("mail.pop3.user", account.pop3User);
		final Session session = Session.getInstance(sessionProperties);
		if(pop3Debug)
			session.setDebug(true);
		return session;
	}

	protected final POP3Store getPOP3Store(final Session session, final Account account)
	{
		return new POP3Store(session, new URLName("pop3://"+account.pop3User+":"+account.pop3Password+"@"+pop3Host+"/INBOX"));
	}

	protected final void cleanPOP3Account(final Account account)
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
			inboxFolder.open(Folder.READ_WRITE);
			final Message[] inboxMessages = inboxFolder.getMessages();
			//System.out.println("--------removing "+inboxMessages.length+" messages --------");
			for(final Message message : inboxMessages)
			{
				//System.out.println("-----------------removing message "+i);
				message.setFlag(Flags.Flag.DELETED, true);
			}

			inboxFolder.close(true); // expunge
			inboxFolder = null;
			store.close();
			store = null;
		}
		catch(final MessagingException e)
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
				catch(final MessagingException e)
				{/*IGNORE*/}
			}
			if(store!=null)
			{
				try
				{
					store.close();
				}
				catch(final MessagingException e)
				{/*IGNORE*/}
			}
		}
	}
}
