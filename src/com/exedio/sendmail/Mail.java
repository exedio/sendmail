package com.exedio.sendmail;


public interface Mail
{
	public String getFrom();
	
	public String getTo();
	
	public String getCarbonCopy();
	
	public String getBlindCarbonCopy();
	
	public String getText();

	public String getSubject();
	
	public void notifySent();

	public void notifyFailed(final Exception exception);
	
}
