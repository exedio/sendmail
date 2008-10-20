/*
 * Copyright (C) 2004-2008  exedio GmbH (www.exedio.com)
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

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public final class MailSender
{
	public static final String DEFAULT_CHARSET = "UTF-8";
	private static PrintStream log = System.err;
	
	private final String smtpHost;
	private final int connectTimeout;
	private final int readTimeout;
	private final boolean smtpDebug;
	private final Session session;
	
	MailSender(final String smtpHost, final int connectTimeout, final int readTimeout, final boolean smtpDebug)
	{
		if(connectTimeout<0)
			throw new IllegalArgumentException("connectTimeout must not be negative");
		if(readTimeout<0)
			throw new IllegalArgumentException("readTimeout must not be negative");
		
		this.smtpHost = smtpHost;
		this.connectTimeout = connectTimeout;
		this.readTimeout = readTimeout;
		this.smtpDebug = smtpDebug;
		
		final Properties properties = new Properties();
		properties.put("mail.host", smtpHost);
		properties.put("mail.transport.protocol", "smtp");
		properties.put("mail.smtp.connectiontimeout", connectTimeout);
		properties.put("mail.smtp.timeout", readTimeout);
		final Session session = Session.getInstance(properties);
		if(smtpDebug)
			session.setDebug(true);
		this.session = session;
	}

	public String getHost()
	{
		return smtpHost;
	}

	public int getConnectTimeout()
	{
		return connectTimeout;
	}

	public int getReadTimeout()
	{
		return readTimeout;
	}

	public boolean isDebug()
	{
		return smtpDebug;
	}
	
	/**
	 * @return the number of successfully sent mails
	 * @deprecated Use {@link #sendMails(MailSource, int)} instead
	 */
	@Deprecated
	public static final int sendMails(final MailSource source, final String smtpHost, final int connectTimeout, final int readTimeout, final boolean smtpDebug, final int maximumResultSize)
	{
		return new MailSender(smtpHost, connectTimeout, readTimeout, smtpDebug).sendMails(source, maximumResultSize);
	}
	
	/**
	 * @return the number of successfully sent mails
	 */
	public int sendMails(final MailSource source, final int maximumResultSize)
	{
		int result = 0;
		for(int sessionCounter = 0; sessionCounter<30; sessionCounter++)
		{
			final Collection<? extends Mail> mails = source.getMailsToSend(maximumResultSize);
			if(mails.isEmpty())
				return result;

			final Transport transport;
			try
			{
				transport = session.getTransport("smtp");
			}
			catch(NoSuchProviderException e)
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
					try
					{
						final MimeMessage message = createMessage(session, mail);
						try
						{
							mailsTriedToSendInOneConnection++;
							//final long start = System.currentTimeMillis();
							transport.sendMessage(message, message.getAllRecipients());
							//System.out.println("Mailsender sent. ("+(System.currentTimeMillis()-start)+"ms)");
							mailsSentInOneConnection++;
							result++;
						}
						catch(IllegalStateException e)
						{
							log.println(MailSender.class.getName() + " encounters unexpectedly closed connection on mail #" + mailsTriedToSendInOneConnection + '/' + mailsSentInOneConnection);
							transport.connect();
							mailsTriedToSendInOneConnection = 1;
							mailsSentInOneConnection = 0;
							transport.sendMessage(message, message.getAllRecipients());
							mailsSentInOneConnection++;
						}
						
						mail.notifySent();
					}
					catch(Exception e)
					{
						//System.err.println("-------------------------------------e"+mail);
						//e.printStackTrace();
						//System.err.println("-------------------------------------e"+mail);
						mail.notifyFailed(e);
					}
				}
			}
			catch(MessagingException e)
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
					catch(MessagingException e)
					{
						throw new RuntimeException(e);
					}
				}
			}
		}
		log.println(MailSender.class.getName() + " terminates because of possibly infinite loop");
		return result;
	}
	
	/**
	 * BEWARE:
	 * this method does not call {@link Mail#notifySent()}
	 * or {@link Mail#notifyFailed(Exception)}.
	 * @deprecated Use {@link #sendMail(Mail)} instead
	 */
	@Deprecated
	public static final void sendMail(final Mail mail, final String smtpHost, final int connectTimeout, final int readTimeout, final boolean smtpDebug)
		throws MessagingException
	{
		new MailSender(smtpHost, connectTimeout, readTimeout, smtpDebug).sendMail(mail);
	}
	
	/**
	 * BEWARE:
	 * this method does not call {@link Mail#notifySent()}
	 * or {@link Mail#notifyFailed(Exception)}.
	 */
	public void sendMail(final Mail mail)
		throws MessagingException
	{
		final MimeMessage message = createMessage(session, mail);
		//final long start = System.currentTimeMillis();
		Transport.send(message);
		//System.out.println("Mailsender sent. ("+(System.currentTimeMillis()-start)+"ms)");
	}
	
	private static final MimeMessage createMessage(final Session session, final Mail mail) throws MessagingException
	{
		//System.err.println("-------------------------------------+"+mail);
		final String id = mail.getMessageID();
		final InternetAddress from;
		{
			final String fromString = mail.getFrom();
			if(fromString==null)
				throw new NullPointerException("Mail#getFrom() must not return null (" + mail.toString() + ')');
			from = new InternetAddress(fromString);
		}
		
		final InternetAddress[] to = toAdresses(mail.getTo());
		final InternetAddress[] carbonCopy = toAdresses(mail.getCarbonCopy());
		final InternetAddress[] blindCarbonCopy = toAdresses(mail.getBlindCarbonCopy());
		final String subject = mail.getSubject();
		final Date mailDate = mail.getDate();

		final String textPlain = mail.getTextPlain();
		final String textHtml = mail.getTextHtml();
		if(textPlain==null && textHtml==null)
			throw new NullPointerException("either Mail#getTextPlain() or Mail#getTextHtml() must not return null (" + mail.toString() + ')');
		
		final DataSource[] attachments = emptyToNull(mail.getAttachments());
		
		final String mailCharset = mail.getCharset();
		final String charset = mailCharset==null ? DEFAULT_CHARSET : mailCharset;
		final String htmlContentType = "text/html; charset=" + charset;
		final String plainContentType = "text/plain; charset=" + charset;
		final String date = (new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z (z)", new Locale ("en"))).format( mailDate==null ? new java.util.Date() : mailDate );
		final String contentTransferEncoding = mail.getContentTransferEncoding();
		
		final MimeMessage message =
				id!=null
				? new MimeMessageWithID(session, id, contentTransferEncoding)
				: new MimeMessage(session);
		message.setFrom(from);
		if(to!=null)
			message.setRecipients(Message.RecipientType.TO, to);
		if(carbonCopy!=null)
			message.setRecipients(Message.RecipientType.CC, carbonCopy);
		if(blindCarbonCopy!=null)
			message.setRecipients(Message.RecipientType.BCC, blindCarbonCopy);
		if(subject!=null)
			message.setSubject(subject, charset);
		message.setHeader("Date", date);

		if(attachments==null)
		{
			if(textPlain==null || textHtml==null)
			{
				if(textPlain!=null)
					message.setContent(textPlain, plainContentType);
				else if(textHtml!=null)
					message.setContent(textHtml, htmlContentType);
				else
					assert false;
			}
			else
			{
				message.setContent(alternative(textPlain, textHtml, plainContentType, htmlContentType, contentTransferEncoding));
			}
		}
		else
		{
			final MimeMultipart mixed = new MimeMultipart("mixed");
			if(textPlain==null || textHtml==null)
			{
				final MimeBodyPart part = new MimeBodyPart();
				if(textPlain!=null)
				{
					part.setContent(textPlain, plainContentType);
					if(contentTransferEncoding!=null)			
						part.setHeader("Content-Transfer-Encoding", contentTransferEncoding);
				}
				else if(textHtml!=null)
				{
					part.setContent(textHtml, htmlContentType);
					if(contentTransferEncoding!=null)			
						part.setHeader("Content-Transfer-Encoding", contentTransferEncoding);					
				}
				else
					assert false;
				part.setDisposition(Part.INLINE);
				mixed.addBodyPart(part);
			}
			else
			{
				final MimeBodyPart alternativePart = new MimeBodyPart();
				alternativePart.setContent(alternative(textPlain, textHtml, plainContentType, htmlContentType, contentTransferEncoding));
				if(contentTransferEncoding!=null)			
					alternativePart.setHeader( "Content-Transfer-Encoding", contentTransferEncoding );
				mixed.addBodyPart(alternativePart);
			}
			for(final DataSource attachment : attachments)
			{
				final MimeBodyPart attachPart = new MimeBodyPart();
				attachPart.setDataHandler(new DataHandler(attachment));
				attachPart.setDisposition(Part.ATTACHMENT);
				final String attachmentName = attachment.getName();
				if(attachmentName!=null)
					attachPart.setFileName(attachmentName);
				mixed.addBodyPart(attachPart);
			}
			message.setContent(mixed);
		}
		return message;
	}
	
	/**
	 * See
	 * http://java.sun.com/products/javamail/FAQ.html#msgid
	 * but updateMessageID did not work, so I used updateHeaders instead.
	 */
	private static final class MimeMessageWithID extends MimeMessage
	{
		final String id;
		final String contentTransferEncoding;
		
		MimeMessageWithID(final Session session, final String id, final String contentTransferEncoding)
		{
			super(session);
			assert id!=null;
			this.id = id;
			this.contentTransferEncoding = contentTransferEncoding;
		}
		
		@Override
		protected void updateHeaders() throws MessagingException
		{
			super.updateHeaders();
			setHeader("Message-ID", id);
			if(contentTransferEncoding!=null)			
				setHeader("Content-Transfer-Encoding", contentTransferEncoding);
		}
	}
	
	private static final InternetAddress[] toAdresses(final String[] s) throws AddressException
	{
		if(s!=null)
		{
			final InternetAddress[] result = new InternetAddress[s.length];
			for(int i = 0; i<s.length; i++)
				result[i] = new InternetAddress(s[i]);
			return result;
		}
		else
			return null;
	}
	
	private static final DataSource[] emptyToNull(final DataSource[] ds)
	{
		return ds==null ? null : ds.length==0 ? null : ds;
	}
	
	private static final MimeMultipart alternative(final String plain, final String html, final String plainContentType, final String htmlContentType, final String contentTransferEncoding) throws MessagingException
	{
		assert plain!=null;
		assert html!=null;
		
		final MimeMultipart result = new MimeMultipart("alternative");
		{
			final MimeBodyPart textPart = new MimeBodyPart();
			textPart.setContent(plain, plainContentType);
			if(contentTransferEncoding!=null)			
				textPart.setHeader( "Content-Transfer-Encoding", contentTransferEncoding );			
			textPart.setDisposition(Part.INLINE);
			result.addBodyPart(textPart);
		}
		{
			final MimeBodyPart htmlPart = new MimeBodyPart();
			htmlPart.setContent(html, htmlContentType);
			if(contentTransferEncoding!=null)			
				htmlPart.setHeader("Content-Transfer-Encoding", contentTransferEncoding);			
			htmlPart.setDisposition(Part.INLINE);
			result.addBodyPart(htmlPart);
		}
		return result;
	}
}
