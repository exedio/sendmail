package com.exedio.sendmail;

import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public final class MailSender
{
	private static final String MAIL_SMTP_HOST = "mail.host";
	
	public static final void sendMails(final MailSource source, final String smtpHost, final int maximumResultSize)
	{
		final Collection mails = source.getMailsToSend(maximumResultSize);

		if(!mails.isEmpty())
		{
			final Properties properties = new Properties();
			properties.put(MAIL_SMTP_HOST, smtpHost);
			properties.put("mail.transport.protocol", "smtp");
			final Session session = Session.getInstance(properties);
			session.setDebug(true);

			for(Iterator i = mails.iterator(); i.hasNext(); )
			{
				final Mail mail = (Mail)i.next();
				try
				{
					final MimeMessage message = new MimeMessage(session);
					message.setFrom(new InternetAddress(mail.getFrom()));
					message.addRecipient(Message.RecipientType.TO, new InternetAddress(mail.getTo()));
					{
						final String carbonCopy = mail.getCarbonCopy();
						if(carbonCopy!=null)
							message.addRecipient(Message.RecipientType.CC, new InternetAddress(carbonCopy));
					}
					{
						final String blindCarbonCopy = mail.getBlindCarbonCopy();
						if(blindCarbonCopy!=null)
							message.addRecipient(Message.RecipientType.BCC, new InternetAddress(blindCarbonCopy));
					}
					{
						final String subject = mail.getSubject();
						if(subject!=null)
							message.setSubject(subject);
					}
					message.setText(mail.getText());
					Transport.send(message);
					mail.notifySent();
				}
				catch(Exception e)
				{
					mail.notifyFailed(e);
				}
			}
		}
	}

}
