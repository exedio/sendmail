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
		final Collection<? extends Mail> mails = source.getMailsToSend(maximumResultSize);

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
			
				for(final Mail mail : mails)
				{
					try
					{
						//System.err.println("-------------------------------------+"+mail);
						final InternetAddress from = new InternetAddress(mail.getFrom());
						if(from==null)
							throw new NullPointerException("Mail#getFrom() must not return null (" + mail.toString() + ')');
						
						final InternetAddress[] to = toAdresses(mail.getTo());
						final InternetAddress[] carbonCopy = toAdresses(mail.getCarbonCopy());
						final InternetAddress[] blindCarbonCopy = toAdresses(mail.getBlindCarbonCopy());
						final String subject = mail.getSubject();

						final String text = mail.getText();
						final String textAsHtml = mail.getTextAsHtml();
						if(text==null && textAsHtml==null)
							throw new NullPointerException("either Mail#getText() or Mail#getTextAsHtml() must not return null (" + mail.toString() + ')');
						
						final DataSource[] attachments = mail.getAttachments();
						
						final MimeMessage message = new MimeMessage(session);
						message.setFrom(from);
						if(to!=null)
							message.setRecipients(Message.RecipientType.TO, to);
						if(carbonCopy!=null)
							message.setRecipients(Message.RecipientType.CC, carbonCopy);
						if(blindCarbonCopy!=null)
							message.setRecipients(Message.RecipientType.BCC, blindCarbonCopy);
						if(subject!=null)
							message.setSubject(subject, CHARSET);

						if((attachments==null || attachments.length==0) && (text==null || textAsHtml==null))
						{
							//System.err.println("if (attachments==null || attachments.length==0) && (text==null || textAsHtml==null)");
							if(textAsHtml!=null)
							{
								assert text==null;
								assert textAsHtml!=null;
								//System.err.println("textAsHtml!=null");
								message.setContent(textAsHtml, HTML_CONTENT_TYPE);
							}
							else
							{
								assert text!=null;
								assert textAsHtml==null;
								//System.err.println("text!=null");
								message.setText(text, CHARSET);
							}
						}
						else
						{
							//System.err.println("else (attachments==null || attachments.length==0) && (text==null || textAsHtml==null)");
							final MimeMultipart multipart = new MimeMultipart("alternative");
							if(text!=null)
							{
								//System.err.println("text!=null");
								final MimeBodyPart textPart = new MimeBodyPart();
								assert text!=null;
								textPart.setText(text, CHARSET);
								textPart.setDisposition(BodyPart.INLINE);
								multipart.addBodyPart(textPart);
							}
							if(textAsHtml!=null)
							{
								//System.err.println("textAsHtml!=null");
								final MimeBodyPart htmlPart = new MimeBodyPart();
								assert textAsHtml!=null;
								htmlPart.setContent(textAsHtml, HTML_CONTENT_TYPE);
								htmlPart.setDisposition(BodyPart.INLINE);
								multipart.addBodyPart(htmlPart);
							}
							if(attachments!=null)
							{
								for(int j = 0; j<attachments.length; j++)
								{
									final MimeBodyPart attachPart = new MimeBodyPart();
									attachPart.setDataHandler(new DataHandler(attachments[j]));
									attachPart.setDisposition(BodyPart.ATTACHMENT);
									attachPart.setFileName(attachments[j].getName());
									multipart.addBodyPart(attachPart);
								}
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

}
