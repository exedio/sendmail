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

import com.exedio.cope.util.Interrupter;

final class TaskContextInterrupter implements ExperimentalTaskContext
{
	private final Interrupter interrupter;
	private int progress = 0;
	private boolean exhausted = false;

	TaskContextInterrupter(final Interrupter interrupter)
	{
		this.interrupter = interrupter;
	}

	public boolean requestsStop()
	{
		if(exhausted)
			throw new IllegalStateException();

		return interrupter!=null && interrupter.isRequested();
	}

	public void notifyProgress(final int delta)
	{
		if(exhausted)
			throw new IllegalStateException();

		progress += delta;
	}

	int getProgress()
	{
		if(exhausted)
			throw new IllegalStateException();

		exhausted = true;
		return progress;
	}
}
