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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.activation.DataSource;

import junit.framework.TestCase;

public class CascadingMailSourceTest extends TestCase
{
	int maximumResultSizeE;
	int maximumResultSizeA;
	int maximumResultSizeB;
	
	public void testComposite() throws Exception
	{
		maximumResultSizeE = -1;
		maximumResultSizeA = -1;
		maximumResultSizeB = -1;
		
		final MailSource pe = new MailSource()
		{
			public Collection<? extends Mail> getMailsToSend(final int maximumResultSize)
			{
				if(maximumResultSize<=0)
					throw new RuntimeException();
				maximumResultSizeE = maximumResultSize;

				return Collections.<Mail>emptyList();
			}
		};

		final MockMail a1 = new MockMail("a1");
		final MockMail a2 = new MockMail("a2");
		final MailSource pa = new MailSource()
		{
			public Collection<? extends Mail> getMailsToSend(final int maximumResultSize)
			{
				if(maximumResultSize<=0)
					throw new RuntimeException();
				maximumResultSizeA = maximumResultSize;

				switch(maximumResultSize)
				{
					case 1:
						return list(a1);
					default:
						return list(a1, a2);
				}
			}
		};
		
		final MockMail b1 = new MockMail("b1");
		final MockMail b2 = new MockMail("b2");
		final MailSource pb = new MailSource()
		{
			public Collection<? extends Mail> getMailsToSend(final int maximumResultSize)
			{
				if(maximumResultSize<=0)
					throw new RuntimeException();
				maximumResultSizeB = maximumResultSize;

				switch(maximumResultSize)
				{
					case 1:
						return list(b1);
					default:
						return list(b1, b2);
				}
			}
		};
		
		{
			final CascadingMailSource p = new CascadingMailSource(new MailSource[]{pe, pa});
			assertMails(list(a1, a2), p.getMailsToSend(3), 3, 3,-1);
			assertMails(list(a1, a2), p.getMailsToSend(2), 2, 2,-1);
			assertMails(list(a1), p.getMailsToSend(1),     1, 1,-1);
			assertMails(list(), p.getMailsToSend(0),      -1,-1,-1);
		}
		{
			final CascadingMailSource p = new CascadingMailSource(new MailSource[]{pa, pe});
			assertMails(list(a1, a2), p.getMailsToSend(3), 1, 3,-1);
			assertMails(list(a1, a2), p.getMailsToSend(2),-1, 2,-1);
			assertMails(list(a1), p.getMailsToSend(1),    -1, 1,-1);
			assertMails(list(), p.getMailsToSend(0),      -1,-1,-1);
		}
		{
			final CascadingMailSource p = new CascadingMailSource(new MailSource[]{pa, pb});
			assertMails(list(a1, a2, b1, b2), p.getMailsToSend(5),-1, 5, 3);
			assertMails(list(a1, a2, b1, b2), p.getMailsToSend(4),-1, 4, 2);
			assertMails(list(a1, a2, b1), p.getMailsToSend(3)    ,-1, 3, 1);
			assertMails(list(a1, a2), p.getMailsToSend(2)        ,-1, 2,-1);
			assertMails(list(a1), p.getMailsToSend(1)            ,-1, 1,-1);
			assertMails(list(), p.getMailsToSend(0)              ,-1,-1,-1);
		}
		{
			final CascadingMailSource p = new CascadingMailSource(new MailSource[]{pb, pa});
			assertMails(list(b1, b2, a1, a2), p.getMailsToSend(5),-1, 3, 5);
			assertMails(list(b1, b2, a1, a2), p.getMailsToSend(4),-1, 2, 4);
			assertMails(list(b1, b2, a1), p.getMailsToSend(3)    ,-1, 1, 3);
			assertMails(list(b1, b2), p.getMailsToSend(2)        ,-1,-1, 2);
			assertMails(list(b1), p.getMailsToSend(1)            ,-1,-1, 1);
			assertMails(list(), p.getMailsToSend(0)              ,-1,-1,-1);
		}
		{
			final CascadingMailSource p = new CascadingMailSource(new MailSource[]{pe, pe});
			assertMails(list(), p.getMailsToSend(2), 2,-1,-1);
			assertMails(list(), p.getMailsToSend(1), 1,-1,-1);
			assertMails(list(), p.getMailsToSend(0),-1,-1,-1);
		}
		
		try
		{
			new CascadingMailSource(new MailSource[]{null});
			fail();
		}
		catch(RuntimeException e)
		{
			assertEquals("must have more than one source", e.getMessage());
		}
		try
		{
			new CascadingMailSource(new MailSource[]{null, null});
			fail();
		}
		catch(NullPointerException e)
		{
			assertEquals("source 0 is null", e.getMessage());
		}
	}
	
	protected final void assertMails(
			final List expectedMails, final Collection actualMails,
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

	private static class MockMail implements Mail
	{
		final String code;
		
		MockMail(final String code)
		{
			this.code = code;
		}
		
		@Override
		public String toString()
		{
			return "mail:"+code;
		}
		
		public String getMessageID()
		{
			throw new RuntimeException(code);
		}
		
		public String getFrom()
		{
			throw new RuntimeException(code);
		}
		
		public String[] getTo()
		{
			throw new RuntimeException(code);
		}
		
		public String[] getCarbonCopy()
		{
			throw new RuntimeException(code);
		}
		
		public String[] getBlindCarbonCopy()
		{
			throw new RuntimeException(code);
		}
		
		public String getSubject()
		{
			throw new RuntimeException(code);
		}
		
		public String getText()
		{
			throw new RuntimeException(code);
		}
		
		public String getTextAsHtml()
		{
			throw new RuntimeException(code);
		}
		
		public DataSource[] getAttachments()
		{
			throw new RuntimeException(code);
		}
		
		public void notifySent()
		{
			throw new RuntimeException(code);
		}

		public void notifyFailed(final Exception exception)
		{
			throw new RuntimeException(code);
		}
	}

	protected final static List<Mail> list(final Mail... o)
	{
		return Arrays.asList(o);
	}
	
}
