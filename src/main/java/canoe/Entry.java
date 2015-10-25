package canoe;

public class Entry<T> {
	private final long term;
	private final int index;
	private final T command;
	
	public Entry(long term, int index, T command) {
		this.term = term;
		this.index = index;
		this.command = command;
	}
	
	public long getTerm() {
		return term;
	}
	
	public int getIndex() {
		return index;
	}
	
	public T getCommand() {
		return command;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((command == null) ? 0 : command.hashCode());
		result = prime * result + index;
		result = prime * result + (int) (term ^ (term >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Entry<T> other = (Entry<T>) obj;
		if (command == null) {
			if (other.command != null)
				return false;
		} else if (!command.equals(other.command))
			return false;
		if (index != other.index)
			return false;
		if (term != other.term)
			return false;
		return true;
	}
}