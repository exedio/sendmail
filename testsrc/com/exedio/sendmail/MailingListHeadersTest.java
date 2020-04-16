package com.exedio.sendmail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
		String s = "https://dontuse.something.invalid";
		final URI uri1 = new URI(s);
		m.addHelp(uri1);
		assertTrue(m.help.size() == 1);
		assertEquals(uri1, m.help.get(0));
		s = "http://dontuse.something.invalid";
		final URI uri2 = new URI(s);
		m.addHelp(uri2);
		assertTrue(m.help.size() == 2);
		assertEquals(uri1, m.help.get(0));
		assertEquals(uri2, m.help.get(1));
		s = "ftp://dontuse.something.invalid";
		final URI uri3 = new URI(s);
		m.addHelp(uri3);
		assertTrue(m.help.size() == 3);
		assertEquals(uri1, m.help.get(0));
		assertEquals(uri2, m.help.get(1));
		assertEquals(uri3, m.help.get(2));
		s = "mailto:dontuse@something.invalid";
		final URI uri4 = new URI(s);
		m.addHelp(uri4);
		assertTrue(m.help.size() == 4);
		assertEquals(uri1, m.help.get(0));
		assertEquals(uri2, m.help.get(1));
		assertEquals(uri3, m.help.get(2));
		assertEquals(uri4, m.help.get(3));
		assertThrows(NullPointerException.class, () -> m.addHelp(null), "help");
		assertThrows(IllegalArgumentException.class, () -> {
			final String aString = "dontuse.something.invalid";
			m.addHelp(new URI(aString));
		}, "help: URI must not have empty scheme: dontuse.something.invalid");
		assertThrows(IllegalArgumentException.class, () -> {
			final String aString = "urn:dontuse1.something.invalid";
			m.addHelp(new URI(aString));
		}, "help: URI must define scheme mailto, http, https or ftp: urn:dontuse1.something.invalid");
		assertThrows(IllegalArgumentException.class, () -> {
			final String aString = "doi:10.1337/0815";
			m.addHelp(new URI(aString));
		}, "help: URI must define scheme mailto, http, https or ftp: doi:10.1337/0815");
		assertThrows(IllegalArgumentException.class, () -> {
			final String aString = "file://something.invalid/dontuse";
			m.addHelp(new URI(aString));
		}, "help: URI must define scheme mailto, http, https or ftp: file://something.invalid/dontuse");
	}

	@Test
	void testUnsubscribe() throws URISyntaxException
	{
		final MailingListHeaders m = new MailingListHeaders();
		assertTrue(m.unsubscribe.isEmpty());
		String s = "https://dontuse.something.invalid";
		final URI uri1 = new URI(s);
		m.addUnsubscribe(uri1);
		assertTrue(m.unsubscribe.size() == 1);
		assertEquals(uri1, m.unsubscribe.get(0));
		s = "http://dontuse.something.invalid";
		final URI uri2 = new URI(s);
		m.addUnsubscribe(uri2);
		assertTrue(m.unsubscribe.size() == 2);
		assertEquals(uri1, m.unsubscribe.get(0));
		assertEquals(uri2, m.unsubscribe.get(1));
		s = "ftp://dontuse.something.invalid";
		final URI uri3 = new URI(s);
		m.addUnsubscribe(uri3);
		assertTrue(m.unsubscribe.size() == 3);
		assertEquals(uri1, m.unsubscribe.get(0));
		assertEquals(uri2, m.unsubscribe.get(1));
		assertEquals(uri3, m.unsubscribe.get(2));
		s = "mailto:dontuse@something.invalid";
		final URI uri4 = new URI(s);
		m.addUnsubscribe(uri4);
		assertTrue(m.unsubscribe.size() == 4);
		assertEquals(uri1, m.unsubscribe.get(0));
		assertEquals(uri2, m.unsubscribe.get(1));
		assertEquals(uri3, m.unsubscribe.get(2));
		assertEquals(uri4, m.unsubscribe.get(3));
		assertThrows(NullPointerException.class, () -> m.addUnsubscribe(null), "unsubscribe");
		assertThrows(IllegalArgumentException.class, () -> {
			final String aString = "dontuse.something.invalid";
			m.addUnsubscribe(new URI(aString));
		}, "unsubscribe: URI must not have empty scheme: dontuse.something.invalid");
		assertThrows(IllegalArgumentException.class, () -> {
			final String aString = "urn:dontuse1.something.invalid";
			m.addUnsubscribe(new URI(aString));
		}, "unsubscribe: URI must define scheme mailto, http, https or ftp: urn:dontuse1.something.invalid");
		assertThrows(IllegalArgumentException.class, () -> {
			final String aString = "doi:10.1337/0815";
			m.addUnsubscribe(new URI(aString));
		}, "unsubscribe: URI must define scheme mailto, http, https or ftp: doi:10.1337/0815");
		assertThrows(IllegalArgumentException.class, () -> {
			final String aString = "file://something.invalid/dontuse";
			m.addUnsubscribe(new URI(aString));
		}, "unsubscribe: URI must define scheme mailto, http, https or ftp: file://something.invalid/dontuse");
	}

	@Test
	void testSubscribe() throws URISyntaxException
	{
		final MailingListHeaders m = new MailingListHeaders();
		assertTrue(m.subscribe.isEmpty());
		String s = "https://dontuse.something.invalid";
		final URI uri1 = new URI(s);
		m.addSubscribe(uri1);
		assertTrue(m.subscribe.size() == 1);
		assertEquals(uri1, m.subscribe.get(0));
		s = "http://dontuse.something.invalid";
		final URI uri2 = new URI(s);
		m.addSubscribe(uri2);
		assertTrue(m.subscribe.size() == 2);
		assertEquals(uri1, m.subscribe.get(0));
		assertEquals(uri2, m.subscribe.get(1));
		s = "ftp://dontuse.something.invalid";
		final URI uri3 = new URI(s);
		m.addSubscribe(uri3);
		assertTrue(m.subscribe.size() == 3);
		assertEquals(uri1, m.subscribe.get(0));
		assertEquals(uri2, m.subscribe.get(1));
		assertEquals(uri3, m.subscribe.get(2));
		s = "mailto:dontuse@something.invalid";
		final URI uri4 = new URI(s);
		m.addSubscribe(uri4);
		assertTrue(m.subscribe.size() == 4);
		assertEquals(uri1, m.subscribe.get(0));
		assertEquals(uri2, m.subscribe.get(1));
		assertEquals(uri3, m.subscribe.get(2));
		assertEquals(uri4, m.subscribe.get(3));
		assertThrows(NullPointerException.class, () -> m.addSubscribe(null), "subscribe");
		assertThrows(IllegalArgumentException.class, () -> {
			final String aString = "dontuse.something.invalid";
			m.addSubscribe(new URI(aString));
		}, "subscribe: URI must not have empty scheme: dontuse.something.invalid");
		assertThrows(IllegalArgumentException.class, () -> {
			final String aString = "urn:dontuse1.something.invalid";
			m.addSubscribe(new URI(aString));
		}, "subscribe: URI must define scheme mailto, http, https or ftp: urn:dontuse1.something.invalid");
		assertThrows(IllegalArgumentException.class, () -> {
			final String aString = "doi:10.1337/0815";
			m.addSubscribe(new URI(aString));
		}, "subscribe: URI must define scheme mailto, http, https or ftp: doi:10.1337/0815");
		assertThrows(IllegalArgumentException.class, () -> {
			final String aString = "file://something.invalid/dontuse";
			m.addSubscribe(new URI(aString));
		}, "subscribe: URI must define scheme mailto, http, https or ftp: file://something.invalid/dontuse");
	}

	@Test
	void testPost() throws URISyntaxException
	{
		final MailingListHeaders m = new MailingListHeaders();
		assertTrue(m.post.isEmpty());
		assertFalse(m.noPost);
		String s = "https://dontuse.something.invalid";
		final URI uri1 = new URI(s);
		m.addPost(uri1);
		assertTrue(m.post.size() == 1);
		assertEquals(uri1, m.post.get(0));
		assertFalse(m.noPost);
		s = "http://dontuse.something.invalid";
		final URI uri2 = new URI(s);
		m.addPost(uri2);
		assertTrue(m.post.size() == 2);
		assertEquals(uri1, m.post.get(0));
		assertEquals(uri2, m.post.get(1));
		assertFalse(m.noPost);
		s = "ftp://dontuse.something.invalid";
		final URI uri3 = new URI(s);
		m.addPost(uri3);
		assertTrue(m.post.size() == 3);
		assertEquals(uri1, m.post.get(0));
		assertEquals(uri2, m.post.get(1));
		assertEquals(uri3, m.post.get(2));
		assertFalse(m.noPost);
		s = "mailto:dontuse@something.invalid";
		final URI uri4 = new URI(s);
		m.addPost(uri4);
		assertTrue(m.post.size() == 4);
		assertEquals(uri1, m.post.get(0));
		assertEquals(uri2, m.post.get(1));
		assertEquals(uri3, m.post.get(2));
		assertEquals(uri4, m.post.get(3));
		assertFalse(m.noPost);
		assertThrows(NullPointerException.class, () -> m.addPost(null), "post");
		assertThrows(IllegalArgumentException.class, () -> {
			final String aString = "dontuse.something.invalid";
			m.addPost(new URI(aString));
		}, "post: URI must not have empty scheme: dontuse.something.invalid");
		assertThrows(IllegalArgumentException.class, () -> {
			final String aString = "urn:dontuse1.something.invalid";
			m.addPost(new URI(aString));
		}, "post: URI must define scheme mailto, http, https or ftp: urn:dontuse1.something.invalid");
		assertThrows(IllegalArgumentException.class, () -> {
			final String aString = "doi:10.1337/0815";
			m.addPost(new URI(aString));
		}, "post: URI must define scheme mailto, http, https or ftp: doi:10.1337/0815");
		assertThrows(IllegalArgumentException.class, () -> {
			final String aString = "file://something.invalid/dontuse";
			m.addPost(new URI(aString));
		}, "post: URI must define scheme mailto, http, https or ftp: file://something.invalid/dontuse");
	}

	@Test
	void testNoPost() throws URISyntaxException
	{
		final MailingListHeaders m = new MailingListHeaders();
		assertTrue(m.post.isEmpty());
		assertFalse(m.noPost);
		m.setNoPost(true);
		assertTrue(m.post.isEmpty());
		assertTrue(m.noPost);
		final URI uri = new URI("https://dontuse.something.invalid");
		assertThrows(IllegalArgumentException.class, () -> m.addPost(uri), "post: Must not add URI to post when noPost is selected: https://dontuse.something.invalid");
		assertTrue(m.post.isEmpty());
		assertTrue(m.noPost);
		m.setNoPost(false);
		assertTrue(m.post.isEmpty());
		assertFalse(m.noPost);
		m.addPost(uri);
		assertTrue(m.post.size() == 1);
		assertEquals(uri, m.post.get(0));
		assertFalse(m.noPost);
		assertThrows(IllegalArgumentException.class, () -> m.setNoPost(true), "post: Can not set post to NO when there are URIs already defined as post");
		assertTrue(m.post.size() == 1);
		assertEquals(uri, m.post.get(0));
		assertFalse(m.noPost);
	}

	@Test
	void testOwner() throws URISyntaxException
	{
		final MailingListHeaders m = new MailingListHeaders();
		assertTrue(m.owner.isEmpty());
		String s = "https://dontuse.something.invalid";
		final URI uri1 = new URI(s);
		m.addOwner(uri1);
		assertTrue(m.owner.size() == 1);
		assertEquals(uri1, m.owner.get(0));
		s = "http://dontuse.something.invalid";
		final URI uri2 = new URI(s);
		m.addOwner(uri2);
		assertTrue(m.owner.size() == 2);
		assertEquals(uri1, m.owner.get(0));
		assertEquals(uri2, m.owner.get(1));
		s = "ftp://dontuse.something.invalid";
		final URI uri3 = new URI(s);
		m.addOwner(uri3);
		assertTrue(m.owner.size() == 3);
		assertEquals(uri1, m.owner.get(0));
		assertEquals(uri2, m.owner.get(1));
		assertEquals(uri3, m.owner.get(2));
		s = "mailto:dontuse@something.invalid";
		final URI uri4 = new URI(s);
		m.addOwner(uri4);
		assertTrue(m.owner.size() == 4);
		assertEquals(uri1, m.owner.get(0));
		assertEquals(uri2, m.owner.get(1));
		assertEquals(uri3, m.owner.get(2));
		assertEquals(uri4, m.owner.get(3));
		assertThrows(NullPointerException.class, () -> m.addOwner(null), "owner");
		assertThrows(IllegalArgumentException.class, () -> {
			final String aString = "dontuse.something.invalid";
			m.addOwner(new URI(aString));
		}, "owner: URI must not have empty scheme: dontuse.something.invalid");
		assertThrows(IllegalArgumentException.class, () -> {
			final String aString = "urn:dontuse1.something.invalid";
			m.addOwner(new URI(aString));
		}, "owner: URI must define scheme mailto, http, https or ftp: urn:dontuse1.something.invalid");
		assertThrows(IllegalArgumentException.class, () -> {
			final String aString = "doi:10.1337/0815";
			m.addOwner(new URI(aString));
		}, "owner: URI must define scheme mailto, http, https or ftp: doi:10.1337/0815");
		assertThrows(IllegalArgumentException.class, () -> {
			final String aString = "file://something.invalid/dontuse";
			m.addOwner(new URI(aString));
		}, "owner: URI must define scheme mailto, http, https or ftp: file://something.invalid/dontuse");
	}

	@Test
	void testArchive() throws URISyntaxException
	{
		final MailingListHeaders m = new MailingListHeaders();
		assertTrue(m.archive.isEmpty());
		String s = "https://dontuse.something.invalid";
		final URI uri1 = new URI(s);
		m.addArchive(uri1);
		assertTrue(m.archive.size() == 1);
		assertEquals(uri1, m.archive.get(0));
		s = "http://dontuse.something.invalid";
		final URI uri2 = new URI(s);
		m.addArchive(uri2);
		assertTrue(m.archive.size() == 2);
		assertEquals(uri1, m.archive.get(0));
		assertEquals(uri2, m.archive.get(1));
		s = "ftp://dontuse.something.invalid";
		final URI uri3 = new URI(s);
		m.addArchive(uri3);
		assertTrue(m.archive.size() == 3);
		assertEquals(uri1, m.archive.get(0));
		assertEquals(uri2, m.archive.get(1));
		assertEquals(uri3, m.archive.get(2));
		s = "mailto:dontuse@something.invalid";
		final URI uri4 = new URI(s);
		m.addArchive(uri4);
		assertTrue(m.archive.size() == 4);
		assertEquals(uri1, m.archive.get(0));
		assertEquals(uri2, m.archive.get(1));
		assertEquals(uri3, m.archive.get(2));
		assertEquals(uri4, m.archive.get(3));
		assertThrows(NullPointerException.class, () -> m.addArchive(null), "archive");
		assertThrows(IllegalArgumentException.class, () -> {
			final String aString = "dontuse.something.invalid";
			m.addArchive(new URI(aString));
		}, "archive: URI must not have empty scheme: dontuse.something.invalid");
		assertThrows(IllegalArgumentException.class, () -> {
			final String aString = "urn:dontuse1.something.invalid";
			m.addArchive(new URI(aString));
		}, "archive: URI must define scheme mailto, http, https or ftp: urn:dontuse1.something.invalid");
		assertThrows(IllegalArgumentException.class, () -> {
			final String aString = "doi:10.1337/0815";
			m.addArchive(new URI(aString));
		}, "archive: URI must define scheme mailto, http, https or ftp: doi:10.1337/0815");
		assertThrows(IllegalArgumentException.class, () -> {
			final String aString = "file://something.invalid/dontuse";
			m.addArchive(new URI(aString));
		}, "archive: URI must define scheme mailto, http, https or ftp: file://something.invalid/dontuse");
	}
}
