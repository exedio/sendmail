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

import java.util.Date;

import javax.activation.DataSource;

public interface Mail
{
	/**
	 * May return null, then javax.mail produces an ID.
	 */
	String getMessageID();

	String getFrom();

	String[] getTo();

	String[] getCarbonCopy();

	String[] getBlindCarbonCopy();

	String getTextPlain();

	/**
	 * An implementation may return
	 * both plain text and html text,
	 * then both texts are sent as
	 * alternatives.
	 */
	String getTextHtml();

	String getSubject();

	DataSource[] getAttachments();

	/**
	 * May return null, then the {@link MailSender#DEFAULT_CHARSET default charset} is used.
	 */
	String getCharset();

	/**
	 * Returns the value the Content-Transfer-Encoding header of the mail should be set to.
	 * May return null, then the Content-Transfer-Encoding header won't be set.
	 * @return the desired content-transfer-encoding
	 */
	String getContentTransferEncoding();

	/**
	 * Returns the date the Date header of the mail should be set to.
	 * According to RFC 2822 this Date should provide the creation date of the mail.
	 * <p>
	 * From the RFC:<br>
	 * The origination date specifies the date and time at which the creator
	 * of the message indicated that the message was complete and ready to
	 * enter the mail delivery system.  For instance, this might be the time
	 * that a user pushes the "send" or "submit" button in an application
	 * program.  In any case, it is specifically not intended to convey the
	 * time that the message is actually transported, but rather the time at
	 * which the human or other creator of the message has put the message
	 * into its final form, ready for transport.  (For example, a portable
	 * computer user who is not connected to a network might queue a message
	 * for delivery.  The origination date is intended to contain the date
	 * and time that the user queued the message, not the time when the user
	 * connected to the network to send the message.)
	 *
	 * @return the creation date of the mail
	 */
	Date getDate();

	void notifySent();

	void notifyFailed(Exception exception);
}
