
package mail;

public interface EmailToBeSent
{
	public String getFrom();
	
	public String getTo();
	
	public String getCarbonCopy();
	
	public String getBlindCarbonCopy();
	
}
