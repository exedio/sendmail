
package com.exedio.cope.sendmail;

import java.util.Collection;

public interface EmailProvider
{
	public Collection getEmailsToBeSent(final int maximumResultSize);
	
}
