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
	@Override
	public String[] getTo()
	{
		throw new AssertionError();
	}

	@Override
	public String getFrom()
	{
		throw new AssertionError();
	}

	@Override
	public String getSubject()
	{
		throw new AssertionError();
	}

	@Override
	public String getTextPlain()
	{
		throw new AssertionError();
	}

	@Override
	public String[] getBlindCarbonCopy()
	{
		throw new AssertionError();
	}

	@Override
	public String[] getCarbonCopy()
	{
		throw new AssertionError();
	}

	@Override
	public String getCharset()
	{
		throw new AssertionError();
	}

	@Override
	public String getContentTransferEncoding()
	{
		throw new AssertionError();
	}

	@Override
	public Date getDate()
	{
		throw new AssertionError();
	}

	@Override
	public String getMessageID()
	{
		throw new AssertionError();
	}

	@Override
	public String getTextHtml()
	{
		throw new AssertionError();
	}

	@Override
	public DataSource[] getAttachments()
	{
		throw new AssertionError();
	}

	@Override
	public void notifyFailed(final Exception exception)
	{
		throw new AssertionError(exception);
	}

	@Override
	public void notifySent()
	{
		throw new AssertionError();
	}

	@Override
	public String[] getReplyTo()
	{
		throw new AssertionError();
	}
}
