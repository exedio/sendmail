package com.exedio.sendmail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.URI;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;

public class MailingListHeadersTest
{
	@Test
	void testHelp() throws URISyntaxException
	{
		final MailingListHeaders m = new MailingListHeaders();
		assertTrue(m.help.isEmpty());
		String s = "https://dontuse.exedio.com";
		final URI uri1 = new URI(s);
		m.addHelp(uri1);
		assertTrue(m.help.size() == 1);
		assertEquals(uri1, m.help.get(0));
		s = "http://dontuse.exedio.com";
		final URI uri2 = new URI(s);
		m.addHelp(uri2);
		assertTrue(m.help.size() == 2);
		assertEquals(uri1, m.help.get(0));
		assertEquals(uri2, m.help.get(1));
		s = "ftp://dontuse.exedio.com";
		final URI uri3 = new URI(s);
		m.addHelp(uri3);
		assertTrue(m.help.size() == 3);
		assertEquals(uri1, m.help.get(0));
		assertEquals(uri2, m.help.get(1));
		assertEquals(uri3, m.help.get(2));
		s = "mailto:dontuse@exedio.com";
		final URI uri4 = new URI(s);
		m.addHelp(uri4);
		assertTrue(m.help.size() == 4);
		assertEquals(uri1, m.help.get(0));
		assertEquals(uri2, m.help.get(1));
		assertEquals(uri3, m.help.get(2));
		assertEquals(uri4, m.help.get(3));
		try
		{
			m.addHelp(null);
			fail();
		}
		catch(final NullPointerException e)
		{
			assertEquals("help", e.getMessage());
		}
		try
		{
			s = "dontuse.exedio.com";
			m.addHelp(new URI(s));
			fail();
		}
		catch(final IllegalArgumentException e)
		{
			assertEquals("help: URI must not have empty scheme: " + s, e.getMessage());
		}
		try
		{
			s = "urn:dontuse1.exedio.com";
			m.addHelp(new URI(s));
			fail();
		}
		catch(final IllegalArgumentException e)
		{
			assertEquals("help: URI must define scheme mailto, http, https or ftp: " + s, e.getMessage());
		}
		try
		{
			s = "doi:10.1337/0815";
			m.addHelp(new URI(s));
			fail();
		}
		catch(final IllegalArgumentException e)
		{
			assertEquals("help: URI must define scheme mailto, http, https or ftp: " + s, e.getMessage());
		}
		try
		{
			s = "file://exedio.com/dontuse";
			m.addHelp(new URI(s));
			fail();
		}
		catch(final IllegalArgumentException e)
		{
			assertEquals("help: URI must define scheme mailto, http, https or ftp: " + s, e.getMessage());
		}
	}

	@Test
	void testUnsubscribe() throws URISyntaxException
	{
		final MailingListHeaders m = new MailingListHeaders();
		assertTrue(m.unsubscribe.isEmpty());
		String s = "https://dontuse.exedio.com";
		final URI uri1 = new URI(s);
		m.addUnsubscribe(uri1);
		assertTrue(m.unsubscribe.size() == 1);
		assertEquals(uri1, m.unsubscribe.get(0));
		s = "http://dontuse.exedio.com";
		final URI uri2 = new URI(s);
		m.addUnsubscribe(uri2);
		assertTrue(m.unsubscribe.size() == 2);
		assertEquals(uri1, m.unsubscribe.get(0));
		assertEquals(uri2, m.unsubscribe.get(1));
		s = "ftp://dontuse.exedio.com";
		final URI uri3 = new URI(s);
		m.addUnsubscribe(uri3);
		assertTrue(m.unsubscribe.size() == 3);
		assertEquals(uri1, m.unsubscribe.get(0));
		assertEquals(uri2, m.unsubscribe.get(1));
		assertEquals(uri3, m.unsubscribe.get(2));
		s = "mailto:dontuse@exedio.com";
		final URI uri4 = new URI(s);
		m.addUnsubscribe(uri4);
		assertTrue(m.unsubscribe.size() == 4);
		assertEquals(uri1, m.unsubscribe.get(0));
		assertEquals(uri2, m.unsubscribe.get(1));
		assertEquals(uri3, m.unsubscribe.get(2));
		assertEquals(uri4, m.unsubscribe.get(3));
		try
		{
			m.addUnsubscribe(null);
			fail();
		}
		catch(final NullPointerException e)
		{
			assertEquals("unsubscribe", e.getMessage());
		}
		try
		{
			s = "dontuse.exedio.com";
			m.addUnsubscribe(new URI(s));
			fail();
		}
		catch(final IllegalArgumentException e)
		{
			assertEquals("unsubscribe: URI must not have empty scheme: " + s, e.getMessage());
		}
		try
		{
			s = "urn:dontuse1.exedio.com";
			m.addUnsubscribe(new URI(s));
			fail();
		}
		catch(final IllegalArgumentException e)
		{
			assertEquals("unsubscribe: URI must define scheme mailto, http, https or ftp: " + s, e.getMessage());
		}
		try
		{
			s = "doi:10.1337/0815";
			m.addUnsubscribe(new URI(s));
			fail();
		}
		catch(final IllegalArgumentException e)
		{
			assertEquals("unsubscribe: URI must define scheme mailto, http, https or ftp: " + s, e.getMessage());
		}
		try
		{
			s = "file://exedio.com/dontuse";
			m.addUnsubscribe(new URI(s));
			fail();
		}
		catch(final IllegalArgumentException e)
		{
			assertEquals("unsubscribe: URI must define scheme mailto, http, https or ftp: " + s, e.getMessage());
		}
	}

	@Test
	void testSubscribe() throws URISyntaxException
	{
		final MailingListHeaders m = new MailingListHeaders();
		assertTrue(m.subscribe.isEmpty());
		String s = "https://dontuse.exedio.com";
		final URI uri1 = new URI(s);
		m.addSubscribe(uri1);
		assertTrue(m.subscribe.size() == 1);
		assertEquals(uri1, m.subscribe.get(0));
		s = "http://dontuse.exedio.com";
		final URI uri2 = new URI(s);
		m.addSubscribe(uri2);
		assertTrue(m.subscribe.size() == 2);
		assertEquals(uri1, m.subscribe.get(0));
		assertEquals(uri2, m.subscribe.get(1));
		s = "ftp://dontuse.exedio.com";
		final URI uri3 = new URI(s);
		m.addSubscribe(uri3);
		assertTrue(m.subscribe.size() == 3);
		assertEquals(uri1, m.subscribe.get(0));
		assertEquals(uri2, m.subscribe.get(1));
		assertEquals(uri3, m.subscribe.get(2));
		s = "mailto:dontuse@exedio.com";
		final URI uri4 = new URI(s);
		m.addSubscribe(uri4);
		assertTrue(m.subscribe.size() == 4);
		assertEquals(uri1, m.subscribe.get(0));
		assertEquals(uri2, m.subscribe.get(1));
		assertEquals(uri3, m.subscribe.get(2));
		assertEquals(uri4, m.subscribe.get(3));
		try
		{
			m.addSubscribe(null);
			fail();
		}
		catch(final NullPointerException e)
		{
			assertEquals("subscribe", e.getMessage());
		}
		try
		{
			s = "dontuse.exedio.com";
			m.addSubscribe(new URI(s));
			fail();
		}
		catch(final IllegalArgumentException e)
		{
			assertEquals("subscribe: URI must not have empty scheme: " + s, e.getMessage());
		}
		try
		{
			s = "urn:dontuse1.exedio.com";
			m.addSubscribe(new URI(s));
			fail();
		}
		catch(final IllegalArgumentException e)
		{
			assertEquals("subscribe: URI must define scheme mailto, http, https or ftp: " + s, e.getMessage());
		}
		try
		{
			s = "doi:10.1337/0815";
			m.addSubscribe(new URI(s));
			fail();
		}
		catch(final IllegalArgumentException e)
		{
			assertEquals("subscribe: URI must define scheme mailto, http, https or ftp: " + s, e.getMessage());
		}
		try
		{
			s = "file://exedio.com/dontuse";
			m.addSubscribe(new URI(s));
			fail();
		}
		catch(final IllegalArgumentException e)
		{
			assertEquals("subscribe: URI must define scheme mailto, http, https or ftp: " + s, e.getMessage());
		}
	}

	@Test
	void testPost() throws URISyntaxException
	{
		final MailingListHeaders m = new MailingListHeaders();
		assertTrue(m.post.isEmpty());
		String s = "https://dontuse.exedio.com";
		final URI uri1 = new URI(s);
		m.addPost(uri1);
		assertTrue(m.post.size() == 1);
		assertEquals(uri1, m.post.get(0));
		s = "http://dontuse.exedio.com";
		final URI uri2 = new URI(s);
		m.addPost(uri2);
		assertTrue(m.post.size() == 2);
		assertEquals(uri1, m.post.get(0));
		assertEquals(uri2, m.post.get(1));
		s = "ftp://dontuse.exedio.com";
		final URI uri3 = new URI(s);
		m.addPost(uri3);
		assertTrue(m.post.size() == 3);
		assertEquals(uri1, m.post.get(0));
		assertEquals(uri2, m.post.get(1));
		assertEquals(uri3, m.post.get(2));
		s = "mailto:dontuse@exedio.com";
		final URI uri4 = new URI(s);
		m.addPost(uri4);
		assertTrue(m.post.size() == 4);
		assertEquals(uri1, m.post.get(0));
		assertEquals(uri2, m.post.get(1));
		assertEquals(uri3, m.post.get(2));
		assertEquals(uri4, m.post.get(3));
		try
		{
			m.addPost(null);
			fail();
		}
		catch(final NullPointerException e)
		{
			assertEquals("post", e.getMessage());
		}
		try
		{
			s = "dontuse.exedio.com";
			m.addPost(new URI(s));
			fail();
		}
		catch(final IllegalArgumentException e)
		{
			assertEquals("post: URI must not have empty scheme: " + s, e.getMessage());
		}
		try
		{
			s = "urn:dontuse1.exedio.com";
			m.addPost(new URI(s));
			fail();
		}
		catch(final IllegalArgumentException e)
		{
			assertEquals("post: URI must define scheme mailto, http, https or ftp: " + s, e.getMessage());
		}
		try
		{
			s = "doi:10.1337/0815";
			m.addPost(new URI(s));
			fail();
		}
		catch(final IllegalArgumentException e)
		{
			assertEquals("post: URI must define scheme mailto, http, https or ftp: " + s, e.getMessage());
		}
		try
		{
			s = "file://exedio.com/dontuse";
			m.addPost(new URI(s));
			fail();
		}
		catch(final IllegalArgumentException e)
		{
			assertEquals("post: URI must define scheme mailto, http, https or ftp: " + s, e.getMessage());
		}
	}

	@Test
	void testOwner() throws URISyntaxException
	{
		final MailingListHeaders m = new MailingListHeaders();
		assertTrue(m.owner.isEmpty());
		String s = "https://dontuse.exedio.com";
		final URI uri1 = new URI(s);
		m.addOwner(uri1);
		assertTrue(m.owner.size() == 1);
		assertEquals(uri1, m.owner.get(0));
		s = "http://dontuse.exedio.com";
		final URI uri2 = new URI(s);
		m.addOwner(uri2);
		assertTrue(m.owner.size() == 2);
		assertEquals(uri1, m.owner.get(0));
		assertEquals(uri2, m.owner.get(1));
		s = "ftp://dontuse.exedio.com";
		final URI uri3 = new URI(s);
		m.addOwner(uri3);
		assertTrue(m.owner.size() == 3);
		assertEquals(uri1, m.owner.get(0));
		assertEquals(uri2, m.owner.get(1));
		assertEquals(uri3, m.owner.get(2));
		s = "mailto:dontuse@exedio.com";
		final URI uri4 = new URI(s);
		m.addOwner(uri4);
		assertTrue(m.owner.size() == 4);
		assertEquals(uri1, m.owner.get(0));
		assertEquals(uri2, m.owner.get(1));
		assertEquals(uri3, m.owner.get(2));
		assertEquals(uri4, m.owner.get(3));
		try
		{
			m.addOwner(null);
			fail();
		}
		catch(final NullPointerException e)
		{
			assertEquals("owner", e.getMessage());
		}
		try
		{
			s = "dontuse.exedio.com";
			m.addOwner(new URI(s));
			fail();
		}
		catch(final IllegalArgumentException e)
		{
			assertEquals("owner: URI must not have empty scheme: " + s, e.getMessage());
		}
		try
		{
			s = "urn:dontuse1.exedio.com";
			m.addOwner(new URI(s));
			fail();
		}
		catch(final IllegalArgumentException e)
		{
			assertEquals("owner: URI must define scheme mailto, http, https or ftp: " + s, e.getMessage());
		}
		try
		{
			s = "doi:10.1337/0815";
			m.addOwner(new URI(s));
			fail();
		}
		catch(final IllegalArgumentException e)
		{
			assertEquals("owner: URI must define scheme mailto, http, https or ftp: " + s, e.getMessage());
		}
		try
		{
			s = "file://exedio.com/dontuse";
			m.addOwner(new URI(s));
			fail();
		}
		catch(final IllegalArgumentException e)
		{
			assertEquals("owner: URI must define scheme mailto, http, https or ftp: " + s, e.getMessage());
		}
	}

	@Test
	void testArchive() throws URISyntaxException
	{
		final MailingListHeaders m = new MailingListHeaders();
		assertTrue(m.archive.isEmpty());
		String s = "https://dontuse.exedio.com";
		final URI uri1 = new URI(s);
		m.addArchive(uri1);
		assertTrue(m.archive.size() == 1);
		assertEquals(uri1, m.archive.get(0));
		s = "http://dontuse.exedio.com";
		final URI uri2 = new URI(s);
		m.addArchive(uri2);
		assertTrue(m.archive.size() == 2);
		assertEquals(uri1, m.archive.get(0));
		assertEquals(uri2, m.archive.get(1));
		s = "ftp://dontuse.exedio.com";
		final URI uri3 = new URI(s);
		m.addArchive(uri3);
		assertTrue(m.archive.size() == 3);
		assertEquals(uri1, m.archive.get(0));
		assertEquals(uri2, m.archive.get(1));
		assertEquals(uri3, m.archive.get(2));
		s = "mailto:dontuse@exedio.com";
		final URI uri4 = new URI(s);
		m.addArchive(uri4);
		assertTrue(m.archive.size() == 4);
		assertEquals(uri1, m.archive.get(0));
		assertEquals(uri2, m.archive.get(1));
		assertEquals(uri3, m.archive.get(2));
		assertEquals(uri4, m.archive.get(3));
		try
		{
			m.addArchive(null);
			fail();
		}
		catch(final NullPointerException e)
		{
			assertEquals("archive", e.getMessage());
		}
		try
		{
			s = "dontuse.exedio.com";
			m.addArchive(new URI(s));
			fail();
		}
		catch(final IllegalArgumentException e)
		{
			assertEquals("archive: URI must not have empty scheme: " + s, e.getMessage());
		}
		try
		{
			s = "urn:dontuse1.exedio.com";
			m.addArchive(new URI(s));
			fail();
		}
		catch(final IllegalArgumentException e)
		{
			assertEquals("archive: URI must define scheme mailto, http, https or ftp: " + s, e.getMessage());
		}
		try
		{
			s = "doi:10.1337/0815";
			m.addArchive(new URI(s));
			fail();
		}
		catch(final IllegalArgumentException e)
		{
			assertEquals("archive: URI must define scheme mailto, http, https or ftp: " + s, e.getMessage());
		}
		try
		{
			s = "file://exedio.com/dontuse";
			m.addArchive(new URI(s));
			fail();
		}
		catch(final IllegalArgumentException e)
		{
			assertEquals("archive: URI must define scheme mailto, http, https or ftp: " + s, e.getMessage());
		}
	}
}
