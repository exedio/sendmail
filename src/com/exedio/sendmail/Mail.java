package com.exedio.sendmail;

import javax.activation.DataSource;


public interface Mail
{
	public String getFrom();
	
	public String[] getTo();
	
	public String[] getCarbonCopy();
	
	public String[] getBlindCarbonCopy();
	
	public boolean isHTML();
	
	public String getText();

	public String getSubject();
	
	public DataSource[] getAttachements();
	
	public void notifySent();

	public void notifyFailed(final Exception exception);
	
}
