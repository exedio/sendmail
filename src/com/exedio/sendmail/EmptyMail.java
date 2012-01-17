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

/**
 * An empty implementation of {@link Mail}.
 *
 * All methods implementing {@link Mail}
 * do as little as possible as allowed by the
 * specification of {@link Mail}.
 *
 * You may want to subclass this class instead of
 * implementing {@link Mail} directly
 * to make your subclass cope with new methods
 * in {@link Mail}.
 */
public abstract class EmptyMail implements Mail
{
	/**
	 * This default implementation always returns <i>null</i>.
	 */
	public String[] getReplyTo()
	{
		return null;
	}
	
	/**
	 * This default implementation always returns <i>null</i>.
	 */
	public String[] getTo()
	{
		return null;
	}

	/**
	 * This default implementation always returns <i>null</i>.
	 */
	public String getSubject()
	{
		return null;
	}

	/**
	 * This default implementation always returns <i>null</i>.
	 */
	public String getTextPlain()
	{
		return null;
	}

	/**
	 * This default implementation always returns <i>null</i>.
	 */
	public String[] getBlindCarbonCopy()
	{
		return null;
	}

	/**
	 * This default implementation always returns <i>null</i>.
	 */
	public String[] getCarbonCopy()
	{
		return null;
	}

	/**
	 * This default implementation always returns <i>null</i>.
	 */
	public String getCharset()
	{
		return null;
	}

	/**
	 * This default implementation always returns <i>null</i>.
	 */
	public String getContentTransferEncoding()
	{
		return null;
	}

	/**
	 * This default implementation always returns <i>null</i>.
	 */
	public Date getDate()
	{
		return null;
	}

	/**
	 * This default implementation always returns <i>null</i>.
	 */
	public String getMessageID()
	{
		return null;
	}

	/**
	 * This default implementation always returns <i>null</i>.
	 */
	public String getTextHtml()
	{
		return null;
	}

	/**
	 * This default implementation always returns <i>null</i>.
	 */
	public DataSource[] getAttachments()
	{
		return null;
	}

	/**
	 * This default implementation does nothing.
	 */
	public void notifyFailed(final Exception exception)
	{
		// empty
	}

	/**
	 * This default implementation does nothing.
	 */
	public void notifySent()
	{
		// empty
	}
}
