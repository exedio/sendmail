package com.exedio.sendmail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.activation.DataHandler;
import javax.activation.DataSource;

public class CharsetEncodingDataHandler extends DataHandler
{
	public CharsetEncodingDataHandler( final String plainText, final String charset, final String plainContentType )
	{
		super(new DataSource(){
			public String getContentType()
			{
				return plainContentType;
			}
		
			public InputStream getInputStream()
			{
				try
				{
					return new ByteArrayInputStream( plainText.getBytes( charset ) );
				} catch (UnsupportedEncodingException e)
				{
					// don't send emails in wrong encoding; don't send at all
					throw new RuntimeException( e );
				}
			}
		
			public String getName()
			{
				return "";
			}
		
			public OutputStream getOutputStream()
			{
				return null;
			}
		});
	}
}
