package com.exedio.sendmail;

import java.util.Collection;

public interface EmailProvider
{
	public Collection getEmailsToBeSent(final int maximumResultSize);
	
	public String getSMTPHost();
	
}
