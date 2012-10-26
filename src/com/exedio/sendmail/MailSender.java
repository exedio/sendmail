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

import static com.exedio.cope.util.InterrupterJobContextAdapter.run;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataSource;
import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import com.exedio.cope.util.Interrupter;
import com.exedio.cope.util.InterrupterJobContextAdapter.Body;
import com.exedio.cope.util.JobContext;

public class MailSender
{
	public static final String DEFAULT_CHARSET = MailData.DEFAULT_CHARSET;
	private static PrintStream log = System.err;

	private final String host;
	private final int connectTimeout;
	private final int readTimeout;
	private final boolean debug;
	private final Session session;

	public MailSender(
			final String host,
			final int connectTimeout,
			final int readTimeout,
			final boolean debug)
	{
		this( host, connectTimeout, readTimeout, debug, null, null );
	}

	/** @param smtpUser null and empty string denote usage without authentification */
	public MailSender(
			final String host,
			final int connectTimeout,
			final int readTimeout,
			final boolean debug,
			final String smtpUser,
			final String smtpPassword )
	{
		if(host==null)
			throw new IllegalArgumentException("host must not be null");
		if(connectTimeout<0)
			throw new IllegalArgumentException("connectTimeout must not be negative");
		if(readTimeout<0)
			throw new IllegalArgumentException("readTimeout must not be negative");

		this.host = host;
		this.connectTimeout = connectTimeout;
		this.readTimeout = readTimeout;
		this.debug = debug;

		final Properties properties = new Properties();
		properties.put("mail.host", host);
		properties.put("mail.transport.protocol", "smtp");
		properties.put("mail.smtp.connectiontimeout", connectTimeout);
		properties.put("mail.smtp.timeout", readTimeout);
		final Session session;
		if ( smtpUser==null || smtpUser.equals("") )
		{
			session = Session.getInstance(properties);
		}
		else
		{
			properties.put("mail.smtp.auth", "true");
			session = Session.getInstance(properties, new SendmailAuthenticator(smtpUser, smtpPassword));
		}
		if(debug)
			session.setDebug(true);
		this.session = session;
	}

	public final String getHost()
	{
		return host;
	}

	public final int getConnectTimeout()
	{
		return connectTimeout;
	}

	public final int getReadTimeout()
	{
		return readTimeout;
	}

	public final boolean isDebug()
	{
		return debug;
	}

	/**
	 * @return the number of successfully sent mails
	 * @deprecated Use {@link #sendMails(MailSource, int, Interrupter)} instead
	 */
	@Deprecated
	public static final int sendMails(
			final MailSource source,
			final String host,
			final int connectTimeout,
			final int readTimeout,
			final boolean debug,
			final int maximumResultSize,
			final Interrupter interrupter)
	{
		return new MailSender(host, connectTimeout, readTimeout, debug).sendMails(source, maximumResultSize, interrupter);
	}

	/**
	 * @deprecated Use {@link #sendMails(MailSource, int, Interrupter)} instead
	 */
	@Deprecated
	public static final void sendMails(
			final MailSource source,
			final String smtpHost,
			final boolean smtpDebug,
			final int maximumResultSize)
	{
		sendMails(source, smtpHost, 60000, 60000, smtpDebug, maximumResultSize, null);
	}

	/**
	 * @deprecated Use {@link #sendMails(MailSource,int,JobContext)} instead.
	 * @return the number of successfully sent mails
	 */
	@Deprecated
	public final int sendMails(
			final MailSource source,
			final int maximumResultSize,
			final Interrupter interrupter)
	{
		return run(
			interrupter,
			new Body(){public void run(final JobContext ctx)
			{
				sendMails(source, maximumResultSize, ctx);
			}}
		);
	}

	public final void sendMails(
			final MailSource source,
			final int maximumResultSize,
			final JobContext ctx)
	{
		for(int sessionCounter = 0; sessionCounter<30; sessionCounter++)
		{
			final Collection<? extends Mail> mails = source.getMailsToSend(maximumResultSize);
			if(mails.isEmpty())
				return;

			if(ctx.requestedToStop())
				return;

			final Transport transport;
			try
			{
				transport = session.getTransport("smtp");
			}
			catch(final NoSuchProviderException e)
			{
				throw new RuntimeException(e);
			}
			int mailsTriedToSendInOneConnection = 0;
			int mailsSentInOneConnection = 0;
			try
			{
				{
					//final long start = System.currentTimeMillis();
					transport.connect();
					//System.out.println("Mailsender connected. ("+(System.currentTimeMillis()-start)+"ms)");
				}

				for(final Mail mail : mails)
				{
					if(ctx.requestedToStop())
						return;

					try
					{
						final MimeMessage message = createMessage(session, mail);
						try
						{
							mailsTriedToSendInOneConnection++;
							sendMessage(transport, message);
							mailsSentInOneConnection++;
							ctx.incrementProgress();
						}
						catch(final IllegalStateException e)
						{
							log.println(MailSender.class.getName() + " encounters unexpectedly closed connection on mail #" + mailsTriedToSendInOneConnection + '/' + mailsSentInOneConnection);
							transport.connect();
							mailsTriedToSendInOneConnection = 1;
							mailsSentInOneConnection = 0;
							sendMessage(transport, message);
							mailsSentInOneConnection++;
						}

						mail.notifySent();
					}
					catch(final Exception e)
					{
						//System.err.println("-------------------------------------e"+mail);
						//e.printStackTrace();
						//System.err.println("-------------------------------------e"+mail);
						mail.notifyFailed(e);
					}
				}
			}
			catch(final MessagingException e)
			{
				throw new RuntimeException(e);
			}
			finally
			{
				if(transport.isConnected())
				{
					try
					{
						//final long start = System.currentTimeMillis();
						transport.close();
						//System.out.println("Mailsender closed. ("+(System.currentTimeMillis()-start)+"ms)");
					}
					catch(final MessagingException e)
					{
						throw new RuntimeException(e);
					}
				}
			}
		}
		log.println(MailSender.class.getName() + " terminates because of possibly infinite loop");
	}

	protected void sendMessage(final Transport transport, final MimeMessage message) throws MessagingException
	{
		//final long start = System.currentTimeMillis();
		transport.sendMessage(message, message.getAllRecipients());
		//System.out.println("Mailsender sent. ("+(System.currentTimeMillis()-start)+"ms)");
	}

	/**
	 * BEWARE:
	 * this method does not call {@link Mail#notifySent()}
	 * or {@link Mail#notifyFailed(Exception)}.
	 * @deprecated Use {@link #sendMail(Mail)} instead
	 */
	@Deprecated
	public static final void sendMail(
			final Mail mail,
			final String host,
			final int connectTimeout,
			final int readTimeout,
			final boolean debug)
		throws MessagingException
	{
		new MailSender(host, connectTimeout, readTimeout, debug).sendMail(mail);
	}

	public final void sendMail(final MailData mail) throws MessagingException
	{
		final MimeMessage message = mail.createMessage(session);
		Transport.send(message);
	}

	/**
	 * @deprecated
	 * this method does not call {@link Mail#notifySent()}
	 * or {@link Mail#notifyFailed(Exception)}.
	 * Use {@link #sendMail(MailData)} instead.
	 */
	@Deprecated
	public final void sendMail(final Mail mail)
		throws MessagingException
	{
		final MimeMessage message = createMessage(session, mail);
		//final long start = System.currentTimeMillis();
		Transport.send(message);
		//System.out.println("Mailsender sent. ("+(System.currentTimeMillis()-start)+"ms)");
	}

	public static final MimeMessage createMessage(
			final Session session,
			final Mail mail)
		throws MessagingException
	{
		//System.err.println("-------------------------------------+"+mail);

		final MailData message = new MailData(mail.getFrom(), mail.getSubject());

		{
			final String id = mail.getMessageID();
			if(id!=null)
				message.setMessageID(id);
		}
		{
			final String[] replyTo = mail.getReplyTo();
			if(replyTo!=null)
				message.setReplyTo(replyTo);
		}
		{
			final String[] to = mail.getTo();
			if(to!=null)
				message.setTo(to);
		}
		{
			final String[] carbonCopy = mail.getCarbonCopy();
			if(carbonCopy!=null)
				message.setCarbonCopy(carbonCopy);
		}
		{
			final String[] blindCarbonCopy = mail.getBlindCarbonCopy();
			if(blindCarbonCopy!=null)
				message.setBlindCarbonCopy(blindCarbonCopy);
		}
		{
			final Date date = mail.getDate();
			if(date!=null)
				message.setDate(date);
		}
		{
			final String textPlain = mail.getTextPlain();
			if(textPlain!=null)
				message.setTextPlain(textPlain);
		}
		{
			final String textHtml = mail.getTextHtml();
			if(textHtml!=null)
				message.setTextHtml(textHtml);
		}
		{
			final DataSource[] attachments = emptyToNull(mail.getAttachments());
			if(attachments!=null)
				message.setAttachments(attachments);
		}
		{
			final String charset = mail.getCharset();
			if(charset!=null)
				message.setCharset(charset);
		}
		{
			final String contentTransferEncoding = mail.getContentTransferEncoding();
			if(contentTransferEncoding!=null)
				message.setContentTransferEncoding(contentTransferEncoding);
		}
		return message.createMessage(session);
	}

	private static final DataSource[] emptyToNull(final DataSource[] ds)
	{
		return ds==null ? null : ds.length==0 ? null : ds;
	}

	private static class SendmailAuthenticator extends Authenticator
	{
		private final String user;
		private final String password;

		public SendmailAuthenticator(final String user, final String password)
		{
			this.user = user;
			this.password = password;
		}

		@Override
		protected PasswordAuthentication getPasswordAuthentication()
		{
			return new PasswordAuthentication(user, password);
		}
	}
}
