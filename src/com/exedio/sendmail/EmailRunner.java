
package com.exedio.sendmail;

import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public final class EmailRunner
{
	private static final String MAIL_SMTP_HOST = "mail.host";
	
	public static final void sendMails(final EmailProvider provider, final int maximumResultSize)
	{
		final Collection emails = provider.getEmailsToBeSent(maximumResultSize);

		if(!emails.isEmpty())
		{
			final Properties properties = new Properties();
			properties.put(MAIL_SMTP_HOST, provider.getSMTPHost());
			properties.put("mail.transport.protocol", "smtp");
			final Session session = Session.getInstance(properties);
			session.setDebug(true);

			for(Iterator i = emails.iterator(); i.hasNext(); )
			{
				final EmailToBeSent email = (EmailToBeSent)i.next();
				try
				{
					final MimeMessage message = new MimeMessage(session);
					message.setFrom(new InternetAddress(email.getFrom()));
					message.addRecipient(Message.RecipientType.TO, new InternetAddress(email.getTo()));
					{
						final String carbonCopy = email.getCarbonCopy();
						if(carbonCopy!=null)
							message.addRecipient(Message.RecipientType.CC, new InternetAddress(carbonCopy));
					}
					{
						final String blindCarbonCopy = email.getBlindCarbonCopy();
						if(blindCarbonCopy!=null)
							message.addRecipient(Message.RecipientType.BCC, new InternetAddress(blindCarbonCopy));
					}
					{
						final String subject = email.getSubject();
						if(subject!=null)
							message.setSubject(subject);
					}
					message.setText(email.getText());
					Transport.send(message);
					email.notifySent();
				}
				catch(Exception e)
				{
					email.notifyFailed(e);
				}
			}
		}
	}

}
