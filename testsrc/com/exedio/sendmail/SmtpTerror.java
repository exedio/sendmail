
package com.exedio.sendmail;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.activation.DataSource;

/**
 * BEWARE: thid puts a lot of mails on your smtp server.
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
	
	private class TerrorMailSource implements MailSource
	{
		private final int threadNumber;
		int number = 0;
		long readyTimestamp = -1;
		
		TerrorMailSource(final int threadNumber)
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
		final TerrorMailSource[] tms = new TerrorMailSource[threadCount];
		for(int i = 0; i<threadCount; i++)
			tms[i] = new TerrorMailSource(i);

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
