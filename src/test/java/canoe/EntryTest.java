package canoe;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EntryTest {
	@Test
	public void testEntryCreation() {
		long term = 1;
		int index = 2;
		String command = "foo";

		Entry<String> e = new Entry<>(term, index, command);

		assertEquals(1, e.getTerm());
		assertEquals(2, e.getIndex());
		assertEquals(command, e.getCommand());
	}

	@Test
	public void testEntryEquivalance() {
		long term = 1;
		int index = 2;
		String command = "foo";

		Entry<String> a = new Entry<>(term, index, command);
		Entry<String> b = new Entry<>(term, index, command);

		assertEquals(a, b);
	}
}
