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
import java.util.Iterator;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
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
		final Collection mails = source.getMailsToSend(maximumResultSize);

		if(!mails.isEmpty())
		{
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
			
				for(Iterator i = mails.iterator(); i.hasNext(); )
				{
					final Mail mail = (Mail)i.next();
					try
					{
						final MimeMessage message = new MimeMessage(session);
						message.setFrom(new InternetAddress(mail.getFrom()));
						{
							final String[] to = mail.getTo();
							if(to!=null)
								for(int j = 0; j<to.length; j++)
									message.addRecipient(Message.RecipientType.TO, new InternetAddress(to[j]));
						}
						{
							final String[] carbonCopy = mail.getCarbonCopy();
							if(carbonCopy!=null)
								for(int j = 0; j<carbonCopy.length; j++)
									message.addRecipient(Message.RecipientType.CC, new InternetAddress(carbonCopy[j]));
						}
						{
							final String[] blindCarbonCopy = mail.getBlindCarbonCopy();
							if(blindCarbonCopy!=null)
								for(int j = 0; j<blindCarbonCopy.length; j++)
									message.addRecipient(Message.RecipientType.BCC, new InternetAddress(blindCarbonCopy[j]));
						}
						{
							final String subject = mail.getSubject();
							if(subject!=null)
								message.setSubject(subject, CHARSET);
						}
	
						final String text = mail.getText();
						final DataSource[] attachments = mail.getAttachments();
						if(attachments==null || attachments.length==0)
						{
							if(mail.isHTML())
								message.setContent(text, HTML_CONTENT_TYPE);
							else
								message.setText(text, CHARSET);
						}
						else
						{
							final MimeMultipart multipart = new MimeMultipart("alternative");
							{
								final MimeBodyPart mainPart = new MimeBodyPart();
								if(mail.isHTML())
									mainPart.setContent(text, HTML_CONTENT_TYPE);
								else
									mainPart.setText(text, CHARSET);
								mainPart.setDisposition(BodyPart.INLINE);
								multipart.addBodyPart(mainPart);
							}
							for(int j = 0; j<attachments.length; j++)
							{
								final MimeBodyPart attachPart = new MimeBodyPart();
								attachPart.setDataHandler(new DataHandler(attachments[j]));
								attachPart.setDisposition(BodyPart.ATTACHMENT);
								attachPart.setFileName(attachments[j].getName());
								multipart.addBodyPart(attachPart);
							}
							message.setContent(multipart);
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
	}

}
