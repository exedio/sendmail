
package com.exedio.sendmail;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

public class CascadingEmailProviderTest extends TestCase
{
	public void testCascade() throws Exception
	{
		
		final MailSource pe = new MailSource()
		{
			public Collection getEmailsToBeSent(final int maximumResultSize)
			{
				return Collections.EMPTY_LIST;
			}
		};

		final EmailToBeSent a1 = new Mail("a1");
		final EmailToBeSent a2 = new Mail("a2");
		final MailSource pa = new MailSource()
		{
			public Collection getEmailsToBeSent(final int maximumResultSize)
			{
				switch(maximumResultSize)
				{
					case 0:
						return list();
					case 1:
						return list(a1);
					default:
						return list(a1, a2);
				}
			}
		};
		
		final EmailToBeSent b1 = new Mail("b1");
		final EmailToBeSent b2 = new Mail("b2");
		final MailSource pb = new MailSource()
		{
			public Collection getEmailsToBeSent(final int maximumResultSize)
			{
				switch(maximumResultSize)
				{
					case 0:
						return list();
					case 1:
						return list(b1);
					default:
						return list(b1, b2);
				}
			}
		};
		
		{
			final CompositeMailSource p = new CompositeMailSource(new MailSource[]{pe, pa});
			assertEquals(list(a1, a2), p.getEmailsToBeSent(2));
			assertEquals(list(a1), p.getEmailsToBeSent(1));
			assertEquals(list(), p.getEmailsToBeSent(0));
		}
		{
			final CompositeMailSource p = new CompositeMailSource(new MailSource[]{pa, pe});
			assertEquals(list(a1, a2), p.getEmailsToBeSent(2));
			assertEquals(list(a1), p.getEmailsToBeSent(1));
			assertEquals(list(), p.getEmailsToBeSent(0));
		}
		{
			final CompositeMailSource p = new CompositeMailSource(new MailSource[]{pa, pb});
			assertEquals(list(a1, a2), p.getEmailsToBeSent(2));
			assertEquals(list(a1), p.getEmailsToBeSent(1));
			assertEquals(list(), p.getEmailsToBeSent(0));
		}
		{
			final CompositeMailSource p = new CompositeMailSource(new MailSource[]{pb, pa});
			assertEquals(list(b1, b2), p.getEmailsToBeSent(2));
			assertEquals(list(b1), p.getEmailsToBeSent(1));
			assertEquals(list(), p.getEmailsToBeSent(0));
		}
		{
			final CompositeMailSource p = new CompositeMailSource(new MailSource[]{pe, pe});
			assertEquals(list(), p.getEmailsToBeSent(2));
			assertEquals(list(), p.getEmailsToBeSent(1));
			assertEquals(list(), p.getEmailsToBeSent(0));
		}
	}

	private static class Mail implements EmailToBeSent
	{
		final String code;
		
		Mail(final String code)
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
		
		public String getTo()
		{
			throw new RuntimeException(code);
		}
		
		public String getCarbonCopy()
		{
			throw new RuntimeException(code);
		}
		
		public String getBlindCarbonCopy()
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
	
}
