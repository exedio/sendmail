
package mail;

import java.util.Collection;

public interface EmailProvider
{
	public Collection getEmailsToBeSent(final int maximumResultSize);
	
}
