package com.exedio.sendmail;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

public class CompositeMailSourceTest extends TestCase
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
			public Collection getMailsToSend(final int maximumResultSize)
			{
				if(maximumResultSize<=0)
					throw new RuntimeException();
				maximumResultSizeE = maximumResultSize;

				return Collections.EMPTY_LIST;
			}
		};

		final TestMail a1 = new TestMail("a1");
		final TestMail a2 = new TestMail("a2");
		final MailSource pa = new MailSource()
		{
			public Collection getMailsToSend(final int maximumResultSize)
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
		
		final TestMail b1 = new TestMail("b1");
		final TestMail b2 = new TestMail("b2");
		final MailSource pb = new MailSource()
		{
			public Collection getMailsToSend(final int maximumResultSize)
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
			final CompositeMailSource p = new CompositeMailSource(new MailSource[]{pe, pa});
			assertMails(list(a1, a2), p.getMailsToSend(3), 3, 3,-1);
			assertMails(list(a1, a2), p.getMailsToSend(2), 2, 2,-1);
			assertMails(list(a1), p.getMailsToSend(1),     1, 1,-1);
			assertMails(list(), p.getMailsToSend(0),      -1,-1,-1);
		}
		{
			final CompositeMailSource p = new CompositeMailSource(new MailSource[]{pa, pe});
			assertMails(list(a1, a2), p.getMailsToSend(3), 1, 3,-1);
			assertMails(list(a1, a2), p.getMailsToSend(2),-1, 2,-1);
			assertMails(list(a1), p.getMailsToSend(1),    -1, 1,-1);
			assertMails(list(), p.getMailsToSend(0),      -1,-1,-1);
		}
		{
			final CompositeMailSource p = new CompositeMailSource(new MailSource[]{pa, pb});
			assertMails(list(a1, a2, b1, b2), p.getMailsToSend(5),-1, 5, 3);
			assertMails(list(a1, a2, b1, b2), p.getMailsToSend(4),-1, 4, 2);
			assertMails(list(a1, a2, b1), p.getMailsToSend(3)    ,-1, 3, 1);
			assertMails(list(a1, a2), p.getMailsToSend(2)        ,-1, 2,-1);
			assertMails(list(a1), p.getMailsToSend(1)            ,-1, 1,-1);
			assertMails(list(), p.getMailsToSend(0)              ,-1,-1,-1);
		}
		{
			final CompositeMailSource p = new CompositeMailSource(new MailSource[]{pb, pa});
			assertMails(list(b1, b2, a1, a2), p.getMailsToSend(5),-1, 3, 5);
			assertMails(list(b1, b2, a1, a2), p.getMailsToSend(4),-1, 2, 4);
			assertMails(list(b1, b2, a1), p.getMailsToSend(3)    ,-1, 1, 3);
			assertMails(list(b1, b2), p.getMailsToSend(2)        ,-1,-1, 2);
			assertMails(list(b1), p.getMailsToSend(1)            ,-1,-1, 1);
			assertMails(list(), p.getMailsToSend(0)              ,-1,-1,-1);
		}
		{
			final CompositeMailSource p = new CompositeMailSource(new MailSource[]{pe, pe});
			assertMails(list(), p.getMailsToSend(2), 2,-1,-1);
			assertMails(list(), p.getMailsToSend(1), 1,-1,-1);
			assertMails(list(), p.getMailsToSend(0),-1,-1,-1);
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

	private static class TestMail implements Mail
	{
		final String code;
		
		TestMail(final String code)
		{
			this.code = code;
		}
		
		public String toString()
		{
			return "mail:"+code;
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
		
		public boolean isHTML()
		{
			throw new RuntimeException(code);
		}
		
		public String getText()
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

	protected final static List list()
	{
		return Collections.EMPTY_LIST;
	}

	protected final static List list(final Object o)
	{
		return Collections.singletonList(o);
	}
	
	protected final static List list(final Object o1, final Object o2)
	{
		return Arrays.asList(new Object[]{o1, o2});
	}
	
	protected final static List list(final Object o1, final Object o2, final Object o3)
	{
		return Arrays.asList(new Object[]{o1, o2, o3});
	}
	
	protected final static List list(final Object o1, final Object o2, final Object o3, final Object o4)
	{
		return Arrays.asList(new Object[]{o1, o2, o3, o4});
	}
	
}
