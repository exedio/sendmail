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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;

public class CascadingMailSourceTest
{
	int maximumResultSizeE;
	int maximumResultSizeA;
	int maximumResultSizeB;

	@SuppressWarnings("deprecation") // OK: testing MailSource API
	@Test
	void testComposite()
	{
		maximumResultSizeE = -1;
		maximumResultSizeA = -1;
		maximumResultSizeB = -1;

		final MailSource pe = maximumResultSize -> 
		{
			if(maximumResultSize<=0)
				throw new RuntimeException();
			maximumResultSizeE = maximumResultSize;

			return Collections.emptyList();
		};

		final MockMail a1 = new MockMail("a1");
		final MockMail a2 = new MockMail("a2");
		final MailSource pa = maximumResultSize -> 
		{
			if(maximumResultSize<=0)
				throw new RuntimeException();
			maximumResultSizeA = maximumResultSize;

			//noinspection SwitchStatementWithTooFewBranches
			switch(maximumResultSize)
			{
				case 1:
					return list(a1);
				default:
					return list(a1, a2);
			}
		};

		final MockMail b1 = new MockMail("b1");
		final MockMail b2 = new MockMail("b2");
		final MailSource pb = maximumResultSize -> 
		{
			if(maximumResultSize<=0)
				throw new RuntimeException();
			maximumResultSizeB = maximumResultSize;

			//noinspection SwitchStatementWithTooFewBranches
			switch(maximumResultSize)
			{
				case 1:
					return list(b1);
				default:
					return list(b1, b2);
			}
		};

		{
			final CascadingMailSource p = new CascadingMailSource(pe, pa);
			assertMails(list(a1, a2), p.getMailsToSend(3), 3, 3,-1);
			assertMails(list(a1, a2), p.getMailsToSend(2), 2, 2,-1);
			assertMails(list(a1), p.getMailsToSend(1),     1, 1,-1);
			assertMails(list(), p.getMailsToSend(0),      -1,-1,-1);
		}
		{
			final CascadingMailSource p = new CascadingMailSource(pa, pe);
			assertMails(list(a1, a2), p.getMailsToSend(3), 1, 3,-1);
			assertMails(list(a1, a2), p.getMailsToSend(2),-1, 2,-1);
			assertMails(list(a1), p.getMailsToSend(1),    -1, 1,-1);
			assertMails(list(), p.getMailsToSend(0),      -1,-1,-1);
		}
		{
			final CascadingMailSource p = new CascadingMailSource(pa, pb);
			assertMails(list(a1, a2, b1, b2), p.getMailsToSend(5),-1, 5, 3);
			assertMails(list(a1, a2, b1, b2), p.getMailsToSend(4),-1, 4, 2);
			assertMails(list(a1, a2, b1), p.getMailsToSend(3)    ,-1, 3, 1);
			assertMails(list(a1, a2), p.getMailsToSend(2)        ,-1, 2,-1);
			assertMails(list(a1), p.getMailsToSend(1)            ,-1, 1,-1);
			assertMails(list(), p.getMailsToSend(0)              ,-1,-1,-1);
		}
		{
			final CascadingMailSource p = new CascadingMailSource(pb, pa);
			assertMails(list(b1, b2, a1, a2), p.getMailsToSend(5),-1, 3, 5);
			assertMails(list(b1, b2, a1, a2), p.getMailsToSend(4),-1, 2, 4);
			assertMails(list(b1, b2, a1), p.getMailsToSend(3)    ,-1, 1, 3);
			assertMails(list(b1, b2), p.getMailsToSend(2)        ,-1,-1, 2);
			assertMails(list(b1), p.getMailsToSend(1)            ,-1,-1, 1);
			assertMails(list(), p.getMailsToSend(0)              ,-1,-1,-1);
		}
		{
			final CascadingMailSource p = new CascadingMailSource(pe, pe);
			assertMails(list(), p.getMailsToSend(2), 2,-1,-1);
			assertMails(list(), p.getMailsToSend(1), 1,-1,-1);
			assertMails(list(), p.getMailsToSend(0),-1,-1,-1);
		}

		try
		{
			new CascadingMailSource((MailSource)null);
			fail();
		}
		catch(final RuntimeException e)
		{
			assertEquals("must have more than one source", e.getMessage());
		}
		try
		{
			new CascadingMailSource(null, null);
			fail();
		}
		catch(final NullPointerException e)
		{
			assertEquals("sources[0]", e.getMessage());
		}
	}

	protected final void assertMails(
			final List<? extends Mail> expectedMails, final Collection<? extends Mail> actualMails,
			final int expectedMaximumResultSizeE, final int expectedMaximumResultSizeA, final int expectedMaximumResultSizeB)
	{
		assertEquals(expectedMails, actualMails);
		assertEquals(expectedMaximumResultSizeE, maximumResultSizeE);
		assertEquals(expectedMaximumResultSizeA, maximumResultSizeA);
		assertEquals(expectedMaximumResultSizeB, maximumResultSizeB);
		maximumResultSizeE = -1;
		maximumResultSizeA = -1;
		maximumResultSizeB = -1;
	}

	private static class MockMail extends AssertionErrorMail
	{
		final String code;
		final long timestamp;

		MockMail(final String code)
		{
			this.code = code;
			this.timestamp = System.currentTimeMillis();
		}

		@Override
		public String toString()
		{
			return "mail:"+code;
		}

		@Override
		public Date getDate()
		{
			return new Date(timestamp);
		}
	}

	protected static final List<Mail> list(final Mail... o)
	{
		return Arrays.asList(o);
	}
}
