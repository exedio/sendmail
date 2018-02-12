/*
 * Copyright (C) 2004-2009  exedio GmbH (www.exedio.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.exedio.sendmail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;


public class CascadingMailSource implements MailSource
{
	private final MailSource[] sources;

	public CascadingMailSource(final MailSource... sources)
	{
		this.sources = sources;

		if(sources.length<=1)
			throw new RuntimeException("must have more than one source");
		for(int i = 0; i<sources.length; i++)
		{
			if(sources[i]==null)
				throw new NullPointerException("sources" + '[' + i + ']');
		}
	}

	@Override
	public Collection<? extends Mail> getMailsToSend(int maximumResultSize)
	{
		Collection<? extends Mail> resultIfOne = null;
		ArrayList<Mail> resultIfMoreThanOne = null;

		for(int i = 0; i<sources.length && maximumResultSize>0; i++)
		{
			final Collection<? extends Mail> mails = sources[i].getMailsToSend(maximumResultSize);
			final int mailsSize = mails.size();

			if(mailsSize>0)
			{
				maximumResultSize -= mailsSize;

				if(resultIfOne==null)
				{
					if(resultIfMoreThanOne==null)
						resultIfOne = mails;
					else
						resultIfMoreThanOne.addAll(mails);
				}
				else
				{
					assert resultIfMoreThanOne==null;
					resultIfMoreThanOne = new ArrayList<>(resultIfOne);
					resultIfOne = null;
					resultIfMoreThanOne.addAll(mails);
				}
			}
		}

		if(resultIfOne==null)
		{
			if(resultIfMoreThanOne==null)
				return Collections.emptyList();
			else
				return resultIfMoreThanOne;
		}
		else
		{
			assert resultIfMoreThanOne==null;
			return resultIfOne;
		}
	}

}
