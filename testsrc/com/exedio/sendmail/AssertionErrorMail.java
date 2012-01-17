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

class AssertionErrorMail implements Mail
{
	public String[] getTo()
	{
		throw new AssertionError();
	}

	public String getFrom()
	{
		throw new AssertionError();
	}

	public String getSubject()
	{
		throw new AssertionError();
	}

	public String getTextPlain()
	{
		throw new AssertionError();
	}

	public String[] getBlindCarbonCopy()
	{
		throw new AssertionError();
	}

	public String[] getCarbonCopy()
	{
		throw new AssertionError();
	}

	public String getCharset()
	{
		throw new AssertionError();
	}

	public String getContentTransferEncoding()
	{
		throw new AssertionError();
	}

	public Date getDate()
	{
		throw new AssertionError();
	}

	public String getMessageID()
	{
		throw new AssertionError();
	}

	public String getTextHtml()
	{
		throw new AssertionError();
	}

	public DataSource[] getAttachments()
	{
		throw new AssertionError();
	}

	public void notifyFailed(final Exception exception)
	{
		throw new AssertionError(exception);
	}

	public void notifySent()
	{
		throw new AssertionError();
	}

	public String[] getReplyTo()
	{
		throw new AssertionError();
	}
}
