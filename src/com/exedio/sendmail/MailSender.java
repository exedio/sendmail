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

import java.util.Collection;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public final class MailSender
{
	private static final String MAIL_SMTP_HOST = "mail.host";
	
	private static final String CHARSET = "UTF-8";
	private static final String HTML_CONTENT_TYPE = "text/html; charset=" + CHARSET;
	
	public static final void sendMails(final MailSource source, final String smtpHost, final boolean smtpDebug, final int maximumResultSize)
	{
		for(int sessionCounter = 0; sessionCounter<30; sessionCounter++)
		{
			final Collection<? extends Mail> mails = source.getMailsToSend(maximumResultSize);
			if(mails.isEmpty())
				return;

			final Properties properties = new Properties();
			properties.put(MAIL_SMTP_HOST, smtpHost);
			properties.put("mail.transport.protocol", "smtp");
			final Session session = Session.getInstance(properties);
			if(smtpDebug)
				session.setDebug(true);
			
			final Transport transport;
			try
			{
				transport = session.getTransport("smtp");
			}
			catch(NoSuchProviderException e)
			{
				throw new RuntimeException(e);
			}
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

						final String textPlain = mail.getTextPlain();
						final String textHtml = mail.getTextHtml();
						if(textPlain==null && textHtml==null)
							throw new NullPointerException("either Mail#getTextPlain() or Mail#getTextHtml() must not return null (" + mail.toString() + ')');
						
						final DataSource[] attachments = emptyToNull(mail.getAttachments());
						
						final MimeMessage message = id!=null ? new MimeMessageWithID(session, id) : new MimeMessage(session);
						message.setFrom(from);
						if(to!=null)
							message.setRecipients(Message.RecipientType.TO, to);
						if(carbonCopy!=null)
							message.setRecipients(Message.RecipientType.CC, carbonCopy);
						if(blindCarbonCopy!=null)
							message.setRecipients(Message.RecipientType.BCC, blindCarbonCopy);
						if(subject!=null)
							message.setSubject(subject, CHARSET);

						if(attachments==null)
						{
							if(textPlain==null || textHtml==null)
							{
								if(textPlain!=null)
									message.setText(textPlain, CHARSET);
								else if(textHtml!=null)
									message.setContent(textHtml, HTML_CONTENT_TYPE);
								else
									assert false;
							}
							else
							{
								message.setContent(alternative(textPlain, textHtml));
							}
						}
						else
						{
							final MimeMultipart mixed = new MimeMultipart("mixed");
							if(textPlain==null || textHtml==null)
							{
								final MimeBodyPart part = new MimeBodyPart();
								if(textPlain!=null)
									part.setText(textPlain, CHARSET);
								else if(textHtml!=null)
									part.setContent(textHtml, HTML_CONTENT_TYPE);
								else
									assert false;
								part.setDisposition(BodyPart.INLINE);
								mixed.addBodyPart(part);
							}
							else
							{
								final MimeBodyPart alternativePart = new MimeBodyPart();
								alternativePart.setContent(alternative(textPlain, textHtml));
								mixed.addBodyPart(alternativePart);
							}
							for(final DataSource attachment : attachments)
							{
								final MimeBodyPart attachPart = new MimeBodyPart();
								attachPart.setDataHandler(new DataHandler(attachment));
								attachPart.setDisposition(BodyPart.ATTACHMENT);
								final String attachmentName = attachment.getName();
								if(attachmentName!=null)
									attachPart.setFileName(attachmentName);
								mixed.addBodyPart(attachPart);
							}
							message.setContent(mixed);
						}
						
						{
							//final long start = System.currentTimeMillis();
							transport.sendMessage(message, message.getAllRecipients());
							//System.out.println("Mailsender sent. ("+(System.currentTimeMillis()-start)+"ms)");
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
		System.err.println("MailSender#sendMails terminates because of possibly infinite loop");
	}
	
	/**
	 * See
	 * http://java.sun.com/products/javamail/FAQ.html#msgid
	 * but updateMessageID did not work, so I used updateHeaders instead.
	 */
	private static final class MimeMessageWithID extends MimeMessage
	{
		final String id;
		
		private MimeMessageWithID(final Session session, final String id)
		{
			super(session);
			assert id!=null;
			this.id = id;
		}
		
		@Override
		protected void updateHeaders() throws MessagingException
		{
			super.updateHeaders();
			setHeader("Message-ID", id);
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
	
	private static final MimeMultipart alternative(final String plain, final String html) throws MessagingException
	{
		assert plain!=null;
		assert html!=null;
		
		final MimeMultipart result = new MimeMultipart("alternative");
		{
			final MimeBodyPart textPart = new MimeBodyPart();
			textPart.setText(plain, CHARSET);
			textPart.setDisposition(BodyPart.INLINE);
			result.addBodyPart(textPart);
		}
		{
			final MimeBodyPart htmlPart = new MimeBodyPart();
			htmlPart.setContent(html, HTML_CONTENT_TYPE);
			htmlPart.setDisposition(BodyPart.INLINE);
			result.addBodyPart(htmlPart);
		}
		return result;
	}

}
