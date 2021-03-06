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

import static java.util.Objects.requireNonNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.annotation.Nullable;
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
	public enum Disposition
	{
		INLINE(Part.INLINE), ATTACHMENT(Part.ATTACHMENT);

		private final String disposition;

		Disposition(final String disposition)
		{
			this.disposition = disposition;
		}
	}

	static final String DEFAULT_CHARSET = "UTF-8";
	private static final long NOT_A_DATE = Long.MIN_VALUE;

	private final InternetAddress from;
	private final String subject;
	private String messageID = null;
	private long date = NOT_A_DATE;
	private final ArrayList<InternetAddress> to = new ArrayList<>();
	private final ArrayList<InternetAddress> carbonCopy = new ArrayList<>();
	private final ArrayList<InternetAddress> blindCarbonCopy = new ArrayList<>();
	private final ArrayList<InternetAddress> replyTo = new ArrayList<>();
	private String textPlain = null;
	private String textHtml = null;
	private String charset = DEFAULT_CHARSET;
	private String contentTransferEncoding = null;
	private final ArrayList<AttachmentData> attachments = new ArrayList<>();
	private final MailingListHeaders mailingListHeaders = new MailingListHeaders();

	public MailData(
			final String from,
			final String subject)
		throws AddressException
	{
		this.from = new InternetAddress(requireNonNull(from, "from"));
		this.subject = subject;

		if(subject==null)
			throw new NullPointerException("subject");
	}

	public void setMessageID(final String messageID)
	{
		this.messageID = messageID;
	}

	private static void set(
			final ArrayList<InternetAddress> list,
			final String[] value)
		throws AddressException
	{
		list.clear();
		list.addAll(toAdresses(value));
	}

	void setTo(final String[] to) throws AddressException
	{
		set(this.to, to);
	}

	public void addTo(final String to) throws AddressException
	{
		this.to.add(new InternetAddress(to));
	}

	void setCarbonCopy(final String[] carbonCopy) throws AddressException
	{
		set(this.carbonCopy, carbonCopy);
	}

	public void addCarbonCopy(final String carbonCopy) throws AddressException
	{
		this.carbonCopy.add(new InternetAddress(carbonCopy));
	}

	void setBlindCarbonCopy(final String[] blindCarbonCopy) throws AddressException
	{
		set(this.blindCarbonCopy, blindCarbonCopy);
	}

	public void addBlindCarbonCopy(final String blindCarbonCopy) throws AddressException
	{
		this.blindCarbonCopy.add(new InternetAddress(blindCarbonCopy));
	}

	void setReplyTo(final String[] replyTo) throws AddressException
	{
		set(this.replyTo, replyTo);
	}

	public void addReplyTo(final String replyTo) throws AddressException
	{
		this.replyTo.add(new InternetAddress(replyTo));
	}

	public void setDate(final Date date)
	{
		this.date = date!=null ? date.getTime() : NOT_A_DATE;
	}

	public Date getDate()
	{
		return (date!=NOT_A_DATE) ? new Date(date) : null;
	}

	public void setTextPlain(final String textPlain)
	{
		this.textPlain = textPlain;
	}

	public void setTextHtml(final String textHtml)
	{
		this.textHtml = textHtml;
	}

	void setCharset(final String charset)
	{
		this.charset = charset;
	}

	void setContentTransferEncoding(final String contentTransferEncoding)
	{
		this.contentTransferEncoding = contentTransferEncoding;
	}

	void setAttachments(final DataSource[] attachments)
	{
		this.attachments.clear();
		for(final DataSource attachment : attachments)
		{
			addAttachment(attachment);
		}
	}

	/** add an attachment with content-disposition 'attachment' */
	public void addAttachment(final DataSource attachment)
	{
		addAttachment(attachment, Disposition.ATTACHMENT);
	}

	public void addAttachment(final DataSource attachment, @Nullable final Disposition disposition)
	{
		//noinspection UnnecessaryThis
		this.attachments.add(new AttachmentData(attachment, disposition));
	}

	public MailingListHeaders mailingListHeaders()
	{
		return mailingListHeaders;
	}

	MimeMessage createMessage(final Session session)
		throws MessagingException
	{
		if(textPlain==null && textHtml==null)
			throw new NullPointerException("either textPlain or textHtml must be set");

		final String htmlContentType = "text/html; charset=" + charset;
		final String plainContentType = "text/plain; charset=" + charset;
		final String dateString = (new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z (z)", new Locale ("en"))).format( date==NOT_A_DATE ? new java.util.Date() : new java.util.Date(date) );

		final MimeMessage message =
				messageID!=null
				? new MimeMessageWithID(session, messageID, contentTransferEncoding)
				: new MimeMessage(session);
		message.setFrom(from);
		message.setReplyTo(toArray(replyTo));
		message.setRecipients(Message.RecipientType.TO, toArray(to));
		message.setRecipients(Message.RecipientType.CC, toArray(carbonCopy));
		message.setRecipients(Message.RecipientType.BCC, toArray(blindCarbonCopy));
		if(subject!=null)
			message.setSubject(subject, charset);
		message.setHeader("Date", dateString);

		if(attachments.isEmpty())
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
			for(final AttachmentData attachment : attachments)
			{
				final MimeBodyPart attachPart = new MimeBodyPart();
				attachPart.setDataHandler(new DataHandler(attachment.source));
				if (attachment.disposition!=null)
					attachPart.setDisposition(attachment.disposition.disposition);
				final String attachmentName = attachment.source.getName();
				if(attachmentName != null)
					attachPart.setFileName(attachmentName);
				mixed.addBodyPart(attachPart);
			}
			message.setContent(mixed);
		}
		mailingListHeaders.addToMessage(message);
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
		protected synchronized void updateHeaders() throws MessagingException
		{
			super.updateHeaders();
			setHeader("Message-ID", id);
			if(contentTransferEncoding!=null)
				setHeader("Content-Transfer-Encoding", contentTransferEncoding);
		}
	}

	private static List<InternetAddress> toAdresses(final String[] s) throws AddressException
	{
		final InternetAddress[] result = new InternetAddress[s.length];
		int i = 0;
		for(final String address : s)
			result[i++] = new InternetAddress(address);
		return Arrays.asList(result);
	}

	private static InternetAddress[] toArray(final ArrayList<InternetAddress> l)
	{
		return l.toArray(EMPTY_INTERNET_ADDRESSES);
	}

	private static final InternetAddress[] EMPTY_INTERNET_ADDRESSES = new InternetAddress[0];

	private static MimeMultipart alternative(final String plain, final String html, final String plainContentType, final String htmlContentType, final String contentTransferEncoding, final String charset ) throws MessagingException
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

	private static final class AttachmentData
	{
		private final DataSource source;
		@Nullable private final Disposition disposition;

		private AttachmentData(final DataSource source, @Nullable final Disposition disposition)
		{
			this.source = requireNonNull(source);
			this.disposition = disposition;
		}
	}
}
