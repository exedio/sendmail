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

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.exedio.cope.util.EmptyJobContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import javax.mail.SendFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MailSenderConnectionCloseTest extends SendmailTest
{

	private String failclose;

	@BeforeEach
	void setUp()
	{
		failclose=System.getProperty("failclose");
	}

	private final class MockMail extends EmptyMail
	{
		private final String id;
		private final String to;
		private final String textPlain;
		private final long timestamp;

		Exception failedException = null;

		MockMail(
				final String id,
				final String to,
				final String textPlain)
		{
			if(id==null)
				throw new RuntimeException("id must not be null");
			if(to!=null && textPlain==null)
				throw new NullPointerException("both textPlain and textAsHtml is null");

			this.id = id;
			this.to = to;
			this.textPlain = textPlain;
			this.timestamp = System.currentTimeMillis();
		}

		@Override
		public String getFrom()
		{
			return from;
		}

		@Override
		public String[] getTo()
		{
			return new String[]{to};
		}

		@Override
		public String getSubject()
		{
			return "subject (\u00e4\u00f6\u00fc\u00df\u0102\u05d8\u20ac)" + '[' + id + ']' ;
		}

		@Override
		public String getTextPlain()
		{
			return textPlain;
		}

		@Override
		public Date getDate()
		{
			return new Date(timestamp);
		}

		@Override
		public void notifySent()
		{
			fail();
		}

		@Override
		public void notifyFailed(final Exception exception)
		{
			failedException = exception;
		}

		@Override
		public String toString()
		{
			return "MockMail(" + id + ')';
		}
	}

	private static final int MAXIMUM_RESULT_SIZE = 345;

	private static final String TEXT = "text for test mail";

	@Test
	void testSendMail()
	{
		final ArrayList<MockMail> mails = new ArrayList<>();
		for(int i = 0; i<50; i++)
			mails.add(new MockMail("mp"+i, failclose, TEXT));

		@SuppressWarnings("deprecation") // OK: testing MailSource API
		final MailSource p = new MailSource()
		{
			boolean done = false;

			@Override
			public Collection<? extends Mail> getMailsToSend(final int maximumResultSize)
			{
				assertEquals(MAXIMUM_RESULT_SIZE, maximumResultSize);

				if(done)
					return Collections.emptyList();

				done = true;
				return mails;
			}
		};
		sendMails(p, MAXIMUM_RESULT_SIZE, new EmptyJobContext());

		for(final MockMail m : mails)
			assertSame(SendFailedException.class, m.failedException.getClass());
	}

}
