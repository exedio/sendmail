package com.exedio.sendmail;

import javax.mail.MessagingException;

public interface EmailToBeSent
{
	public String getFrom();
	
	public String getTo();
	
	public String getCarbonCopy();
	
	public String getBlindCarbonCopy();
	
	public String getText();

	public String getSubject();
	
	public void notifySent();

	public void notifyFailed(final MessagingException exception);
	
}
