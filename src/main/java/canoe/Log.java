package canoe;

import java.util.ArrayList;
import java.util.List;

public class Log<T> {
	/**
	 * log entries; each entry contains commands for state machine, and term
	 * when entry was received by leader (first index is 1)
	 */
	private List<Entry<T>> log;
	
	Log() {
		this.log = new ArrayList<>();
		this.log.add(null);
	}
	
	public int getSize() {
		return log.size() - 1;
	}

	public void add(Entry<T> e) {
		log.add(e);
	}

	public void add(Iterable<Entry<T>> entries) {
		for (Entry<T> e : entries) {
			log.add(e);
		}
	}
	public Entry<T> getEntry(int i) {
		return log.get(i);
	}
}