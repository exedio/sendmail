
package com.exedio.sendmail;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * BEWARE: thid puts a lot of mails on your smtp server.
 */
public class SmtpTerror extends AbstractMailTest
{
	private String userEmail;
	private String userPop3User;
	private String userPop3Password;

	private String ts;
	private int sent;
	private static final boolean terrorDebug = true;

	public void setUp() throws Exception
	{
		super.setUp();

		userEmail=       (String)properties.get("user3.email");
		userPop3User=    (String)properties.get("user3.pop3.user");
		userPop3Password=(String)properties.get("user3.pop3.password");

		final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S ");
		ts = df.format(new Date());
		sent = 0;
		
		cleanPOP3Account(userPop3User, userPop3Password);
	}
	
	public void tearDown() throws Exception
	{
		//cleanPOP3Account(userPop3User, userPop3Password);

		super.tearDown();
	}
	
	private static final int SIZE = 300;
	
	private class TerrorMailSource implements MailSource
	{
		private final int threadNumber;
		int number = 0;
		
		TerrorMailSource(final int threadNumber)
		{
			this.threadNumber = threadNumber;
			
		}
		
		public Collection getMailsToSend(int maximumResultSize)
		{
			final String[] to = new String[]{userEmail};
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
		final Thread[] t = new Thread[threadCount];

		for(int i = 0; i<threadCount; i++)
		{
			final int threadNumber = i;
			t[i] = new Thread(new Runnable()
				{
					public void run()
					{
						final MailSource p = new TerrorMailSource(threadNumber);
						MailSender.sendMails(p, smtpHost, smtpDebug, SIZE);
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
			System.out.println("-----------------"+threadCount+": r="+((end-start)/threadCount)+"ms"+" a="+(end-start)+"ms");
	}
	
}
