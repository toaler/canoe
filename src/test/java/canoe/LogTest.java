package canoe;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LogTest {

	@Test
	public void testLogCreation() {
		Log<String> l = new Log<>();
		Entry<String> e = new Entry<>(1, 1, "foo");

		assertEquals(0, l.getSize());

		l.add(e);

		assertEquals(1, l.getSize());
		assertEquals(e, l.getEntry(1));
	}
}
