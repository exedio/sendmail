package com.exedio.sendmail;

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
	
	public Collection getMailsToSend(final int maximumResultSize)
	{
		for(int i = 0; i<sources.length; i++)
		{
			final Collection mails = sources[i].getMailsToSend(maximumResultSize);
			if(!mails.isEmpty())
				return mails;
		}
		return Collections.EMPTY_LIST;
	}
	
}
