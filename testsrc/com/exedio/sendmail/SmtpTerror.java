/*
 * Copyright (C) 2004-2005  exedio GmbH (www.exedio.com)
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.activation.DataSource;

/**
 * BEWARE: this puts a lot of mails on your smtp server.
 * @author ralf.wiebicke@exedio.com
 */
public class SmtpTerror extends SendmailTest
{
	private Account user;

	private String ts;
	private int sent;
	private static final boolean terrorDebug = true;

	public void setUp() throws Exception
	{
		super.setUp();

		user = new Account("user3");

		final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S ");
		ts = df.format(new Date());
		sent = 0;
		
		cleanPOP3Account(user);
	}
	
	public void tearDown() throws Exception
	{
		//cleanPOP3Account(user);

		super.tearDown();
	}
	
	private static final int SIZE = 300;
	
	private class MockMailSource implements MailSource
	{
		private final int threadNumber;
		int number = 0;
		long readyTimestamp = -1;
		
		MockMailSource(final int threadNumber)
		{
			this.threadNumber = threadNumber;
		}
		
		public Collection getMailsToSend(int maximumResultSize)
		{
			final String[] to = new String[]{user.email};
			final String subject = ts + "terror subject "+threadNumber+" - ";

			final ArrayList result = new ArrayList();
			for( ; maximumResultSize>0 && number<SIZE; maximumResultSize--, number++)
			{
				final int mailNumber = number;
				
				result.add(new Mail()
				{
					public String getFrom()
					{
						return from;
					}
					
					public String[] getTo()
					{
						return to;
					}
					
					public String[] getCarbonCopy()
					{
						return null;
					}
					
					public String[] getBlindCarbonCopy()
					{
						return null;
					}
					
					public String getSubject()
					{
						return subject + mailNumber;
					}
					
					public boolean isHTML()
					{
						return false;
					}
					
					public String getText()
					{
						return "terror mail";
					}
					
					public DataSource[] getAttachements()
					{
						return null;
					}
					
					public void notifySent()
					{
						sent++;
					}

					public void notifyFailed(final Exception exception)
					{
						throw new RuntimeException(exception);
					}
				});
			}
			return result;
		}
	};

	public void testTerror() throws InterruptedException
	{
		if(terrorDebug)
			System.out.println();

		for(int i = 1; i<5; i++)
			doTest(i);
	}
	
	private void doTest(final int threadCount) throws InterruptedException
	{
		final MockMailSource[] tms = new MockMailSource[threadCount];
		for(int i = 0; i<threadCount; i++)
			tms[i] = new MockMailSource(i);

		final Thread[] t = new Thread[threadCount];
		for(int i = 0; i<threadCount; i++)
		{
			final int threadNumber = i;
			t[i] = new Thread(new Runnable()
				{
					public void run()
					{
						MailSender.sendMails(tms[threadNumber], smtpHost, smtpDebug, SIZE);
						tms[threadNumber].readyTimestamp = System.currentTimeMillis();
					}
				}
			);
		}

		final long start = System.currentTimeMillis();
		for(int i = 0; i<threadCount; i++)
			t[i].start();
		for(int i = 0; i<threadCount; i++)
			t[i].join();
		final long end = System.currentTimeMillis();

		if(terrorDebug)
		{
			System.out.println("-----------------"+threadCount+": r="+((end-start)/threadCount)+"ms"+" a="+(end-start)+"ms");
			for(int i = 0; i<threadCount; i++)
				System.out.println("---------------------"+i+": r="+((tms[i].readyTimestamp-start))+"ms");
		}
	}
	
}