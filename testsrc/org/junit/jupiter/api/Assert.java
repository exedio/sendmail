package org.junit.jupiter.api;

public class Assert
{
	public static void assertArrayEquals(final Object[] expected, final Object[] actual)
	{
		Assertions.assertArrayEquals(expected, actual);
	}

	public static void assertArrayEquals(final byte[] expected, final byte[] actual)
	{
		Assertions.assertArrayEquals(expected, actual);
	}

	public static void assertEquals(final Object expected, final Object actual)
	{
		Assertions.assertEquals(expected, actual);
	}

	public static void assertEquals(final String message, final Object expected, final Object actual)
	{
		Assertions.assertEquals(expected, actual, message);
	}

	public static void assertEquals(final int expected, final int actual)
	{
		Assertions.assertEquals(expected, actual);
	}

	public static void assertTrue(final boolean actual)
	{
		Assertions.assertTrue(actual);
	}

	public static void assertTrue(final String message, final boolean actual)
	{
		Assertions.assertTrue(actual, message);
	}

	public static void fail()
	{
		Assertions.fail();
	}

	public static void fail(final String message)
	{
		Assertions.fail(message);
	}

	public static void assertNotSame(final Object unexpected, final Object actual) {
		Assertions.assertNotSame(unexpected, actual);
	}

	public static void assertSame(final Object unexpected, final Object actual) {
		Assertions.assertSame(unexpected, actual);
	}

	public static void assertNotNull(final String message, final Object actual)
	{
		Assertions.assertNotNull(actual, message);
	}
}
