package com.exedio.sendmail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;


public class CompositeMailSource implements MailSource
{
	private final MailSource[] sources;
	
	public CompositeMailSource(final MailSource[] sources)
	{
		this.sources = sources;

		if(sources.length<=1)
			throw new RuntimeException("must have more than one source");
		for(int i = 0; i<sources.length; i++)
		{
			if(sources[i]==null)
				throw new NullPointerException("source "+i+" is null");
		}
	}
	
	public Collection getMailsToSend(int maximumResultSize)
	{
		Collection resultIfOne = null;
		ArrayList resultIfMoreThanOne = null;
		
		for(int i = 0; i<sources.length && maximumResultSize>0; i++)
		{
			final Collection mails = sources[i].getMailsToSend(maximumResultSize);
			final int mailsSize = mails.size();
			
			if(mailsSize>0)
			{
				maximumResultSize -= mailsSize;
				
				if(resultIfOne==null)
					if(resultIfMoreThanOne==null)
						resultIfOne = mails;
					else
						resultIfMoreThanOne.addAll(mails);
				else
					if(resultIfMoreThanOne==null)
					{
						resultIfMoreThanOne = new ArrayList(resultIfOne);
						resultIfOne = null;
						resultIfMoreThanOne.addAll(mails);
					}
					else
						throw new RuntimeException();
			}
		}

		if(resultIfOne==null)
			if(resultIfMoreThanOne==null)
				return Collections.EMPTY_LIST;
			else
				return resultIfMoreThanOne;
		else
			if(resultIfMoreThanOne==null)
				return resultIfOne;
			else
				throw new RuntimeException();
	}
	
}
