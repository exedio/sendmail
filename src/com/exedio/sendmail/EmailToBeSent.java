package com.exedio.sendmail;

public interface EmailToBeSent
{
	public String getFrom();
	
	public String getTo();
	
	public String getCarbonCopy();
	
	public String getBlindCarbonCopy();
	
	public String getText();
	
	public void notifySent();
	
}
