
package com.exedio.sendmail;

import java.io.FileInputStream;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.URLName;

import com.sun.mail.pop3.POP3Store;

import junit.framework.TestCase;


public class AbstractMailTest extends TestCase
{
	protected Properties properties;

	protected String smtpHost;
	protected boolean smtpDebug;

	protected String pop3Host;
	protected boolean pop3Debug;

	protected String from;

	public void setUp() throws Exception
	{
		super.setUp();

		properties = new Properties();
		properties.load(new FileInputStream("test.properties"));

		smtpHost=(String)properties.get("smtp.host");
		smtpDebug=properties.get("smtp.debug")!=null;
		
		pop3Host=(String)properties.get("pop3.host");
		pop3Debug=properties.get("pop3.debug")!=null;

		from=(String)properties.get("from");
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
	
	protected void cleanPOP3Account(final String pop3User, final String pop3Password)
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
	
}
