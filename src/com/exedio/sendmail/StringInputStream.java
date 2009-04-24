/**
 * 
 */
package com.exedio.sendmail;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * Feeds a constant string as an input stream. This converts the string into an
 * array of bytes. The charset to encode the string has to be explicitly given.
 * 
 * @author buecke
 */
public class StringInputStream extends InputStream
{
	private byte[] buffer;
	private int pointer;

	/**
	 * Input string and a charset that is used to encode the string to a byte array.
	 * 
	 * @param input <b>not null</b>
	 * @param charset <b>not null</b>
	 * @see java.lang.String#getBytes(String)
	 */
	public StringInputStream( String input, String charset )
	{
		try
		{
			buffer = input.getBytes( charset );
		} catch (UnsupportedEncodingException e)
		{
			System.err.println( "Unknown charset <" + charset + "> Use default one." );
			buffer = input.getBytes();
		}
		pointer = 0;
	}
	
	@Override
	public int read()
	{
		if ( pointer >= buffer.length ) return -1;
		return buffer[pointer++];
	}

}