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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public final class MailData
{
	static final String DEFAULT_CHARSET = "UTF-8";
	private static long NOT_A_DATE = Long.MIN_VALUE;

	private final String mailFrom;
	private final String mailSubject;
	private String messageID = null;
	private long mailInstanceDate = NOT_A_DATE;
	private final ArrayList<String> mailTo = new ArrayList<String>();
	private final ArrayList<String> mailCarbonCopy = new ArrayList<String>();
	private final ArrayList<String> mailBlindCarbonCopy = new ArrayList<String>();
	private final ArrayList<String> mailReplyTo = new ArrayList<String>();
	private String mailTextPlain = null;
	private String mailTextHtml = null;
	private String mailCharset = DEFAULT_CHARSET;
	private String contentTransferEncoding = null;
	private final ArrayList<DataSource> mailAttachments = new ArrayList<DataSource>();

	public MailData(
			final String from,
			final String subject)
	{
		this.mailFrom = from;
		this.mailSubject = subject;

		if(from==null)
			throw new NullPointerException("from");
	}

	void setMessageID(final String messageID)
	{
		this.messageID = messageID;
	}

	void setTo(final String[] to)
	{
		mailTo.clear();
		mailTo.addAll(Arrays.asList(to));
	}

	void setCarbonCopy(final String[] carbonCopy)
	{
		mailCarbonCopy.clear();
		mailCarbonCopy.addAll(Arrays.asList(carbonCopy));
	}

	void setBlindCarbonCopy(final String[] blindCarbonCopy)
	{
		mailBlindCarbonCopy.clear();
		mailBlindCarbonCopy.addAll(Arrays.asList(blindCarbonCopy));
	}

	void setReplyTo(final String[] replyTo)
	{
		mailReplyTo.clear();
		mailReplyTo.addAll(Arrays.asList(replyTo));
	}

	void setDate(final Date date)
	{
		mailInstanceDate = date!=null ? date.getTime() : NOT_A_DATE;
	}

	void setTextPlain(final String textPlain)
	{
		this.mailTextPlain = textPlain;
	}

	void setTextHtml(final String textHtml)
	{
		this.mailTextHtml = textHtml;
	}

	void setCharset(final String charset)
	{
		this.mailCharset = charset;
	}

	void setContentTransferEncoding(final String contentTransferEncoding)
	{
		this.contentTransferEncoding = contentTransferEncoding;
	}

	void setAttachements(final DataSource[] attachments)
	{
		mailAttachments.clear();
		mailAttachments.addAll(Arrays.asList(attachments));
	}

	MimeMessage createMessage(final Session session)
		throws MessagingException
	{
		//System.err.println("-------------------------------------+"+mail);
		final String id = messageID;
		final InternetAddress from;
		{
			from = new InternetAddress(mailFrom);
		}

		final InternetAddress[] replyTo = toAdresses(mailReplyTo);

		final InternetAddress[] to = toAdresses(mailTo);
		final InternetAddress[] carbonCopy = toAdresses(mailCarbonCopy);
		final InternetAddress[] blindCarbonCopy = toAdresses(mailBlindCarbonCopy);
		final String subject = mailSubject;
		final long mailDate = mailInstanceDate;

		final String textPlain = mailTextPlain;
		final String textHtml = mailTextHtml;
		if(textPlain==null && textHtml==null)
			throw new NullPointerException("either textPlain or textHtml() must be set");

		final ArrayList<DataSource> attachments = emptyToNull(mailAttachments);


		final String charset = mailCharset;
		final String htmlContentType = "text/html; charset=" + charset;
		final String plainContentType = "text/plain; charset=" + charset;
		final String date = (new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z (z)", new Locale ("en"))).format( mailDate==NOT_A_DATE ? new java.util.Date() : new java.util.Date(mailDate) );

		final MimeMessage message =
				id!=null
				? new MimeMessageWithID(session, id, contentTransferEncoding)
				: new MimeMessage(session);
		message.setFrom(from);
		if( replyTo != null )
		{
			message.setReplyTo( replyTo );
		}
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
				{
					message.setDataHandler( new CharsetEncodingDataHandler( textPlain, charset, plainContentType) );
				}
				else if(textHtml!=null)
					message.setContent(textHtml, htmlContentType);
				else
					assert false;
			}
			else
			{
				message.setContent(alternative(textPlain, textHtml, plainContentType, htmlContentType, contentTransferEncoding, charset));
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
					part.setDataHandler( new CharsetEncodingDataHandler( textPlain, charset, plainContentType) );
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
				alternativePart.setContent(alternative(textPlain, textHtml, plainContentType, htmlContentType, contentTransferEncoding, charset));
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

	private static final InternetAddress[] toAdresses(final ArrayList<String> s) throws AddressException
	{
		if(s!=null)
		{
			final InternetAddress[] result = new InternetAddress[s.size()];
			int i = 0;
			for(final String address : s)
				result[i++] = new InternetAddress(address);
			return result;
		}
		else
			return null;
	}

	private static final ArrayList<DataSource> emptyToNull(final ArrayList<DataSource> ds)
	{
		return ds==null ? null : ds.size()==0 ? null : ds;
	}

	private static final MimeMultipart alternative(final String plain, final String html, final String plainContentType, final String htmlContentType, final String contentTransferEncoding, final String charset ) throws MessagingException
	{
		assert plain!=null;
		assert html!=null;

		final MimeMultipart result = new MimeMultipart("alternative");
		{
			final MimeBodyPart textPart = new MimeBodyPart();
			textPart.setDataHandler( new CharsetEncodingDataHandler( plain, charset, plainContentType) );
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
