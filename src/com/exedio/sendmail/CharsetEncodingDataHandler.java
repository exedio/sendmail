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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.internet.MimeBodyPart;

/**
 * A DataHandler that transforms a String into a byte array using the given
 * charset and content type. To be used by
 * {@link MimeBodyPart#setDataHandler(DataHandler)}.
 *
 * @author buecke
 */
final class CharsetEncodingDataHandler extends DataHandler
{
	CharsetEncodingDataHandler( final String plainText, final String charset, final String plainContentType )
	{
		super(new MyDataSource(plainContentType, plainText, charset));
	}

	private static class MyDataSource implements DataSource
	{
		private final String plainContentType;
		private final String plainText;
		private final String charset;

		MyDataSource(final String plainContentType, final String plainText, final String charset)
		{
			this.plainContentType = plainContentType;
			this.plainText = plainText;
			this.charset = charset;
		}

		@Override
		public String getContentType()
		{
			return plainContentType;
		}

		@Override
		public InputStream getInputStream()
		{
			try
			{
				return new ByteArrayInputStream( plainText.getBytes(charset) );
			} catch (final UnsupportedEncodingException e)
			{
				// don't send emails in wrong encoding; don't send at all
				throw new RuntimeException( e );
			}
		}

		@Override
		public String getName()
		{
			return "";
		}

		@Override
		public OutputStream getOutputStream()
		{
			return null;
		}
	}
}
