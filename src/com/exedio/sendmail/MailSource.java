package com.exedio.sendmail;

import java.util.Collection;

public interface MailSource
{
	public Collection getMailsToSend(final int maximumResultSize);
	
}
