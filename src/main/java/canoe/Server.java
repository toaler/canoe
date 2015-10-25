package canoe;

public class Server<T> {
	/**
	 * Server id
	 */
	private String id;
	
	/**
	 * latest term the server has seen (initialized to 0 on first boot, increases monotonically)
	 */
	private long currentTerm;
	
	/**
	 * candidateId that received vote in current term (or null if none)
	 */
	private String votedFor;
	
	
	/**
	 * index of highest log entry known to be committed (initialized to 0, increases monotonically)
	 */
	private int commitIndex;

	/**
	 * index of highest log entry applied to state machine (initialized to 0, increases monotonically)
	 */
	private int lastApplied;

	/**
	 * log entries; each entry contains commands for state machine, and term
	 * when entry was received by leader 
	 */
	private Log<T> log;
	
	public Server(String id, long currentTerm, String votedFor, int commitIndex, Log<T> log) {
		this.id = id;
		this.currentTerm = currentTerm;
		this.votedFor = votedFor;
		this.commitIndex = commitIndex;
		this.log = log;
	}
	
	/**
	 * Invoked by candidates to gather votes (5.2)
	 * @param candidateTerm
	 * @param candidateId
	 * @param candidateLastLogIndex
	 * @param candidateLastLogTerm
	 * @return
	 */
	public RequestVoteResult requestVote(long candidateTerm, String candidateId, long candidateLastLogIndex, long candidateLastLogTerm) {
		if (candidateTerm < currentTerm) {
			// TODO add logging
			return new RequestVoteResult(currentTerm, false);
		} 
		
		// TODO validate that commitIndex should be used over lastApplied
		if ((votedFor == null || votedFor.equals(candidateId))
				&& (candidateLastLogIndex >= log.getSize())) {
			// TODO add logging
			return new RequestVoteResult(currentTerm, true);
		}
		
		return new RequestVoteResult(currentTerm, false);
	}
	
	public static class RequestVoteResult {
		private final long term;
		private final boolean voteGranted;
		
		public RequestVoteResult(long term, boolean voteGranted) {
			this.term = term;
			this.voteGranted = voteGranted;
		}

		public long getTerm() {
			return term;
		}
		
		public boolean getVoteGranted() {
			return voteGranted;
		}
	}
	


	/**
	 * The leader sends the previous index and term of the entry in it's log
	 * that immediately proceeds the new entries. If the follower does not find
	 * an entry in it's log with the same index and term, then it refuses the
	 * new entries. The consistency check acts as a induction step: the initial
	 * empty state of the logs satisfies the Log Matching Property, and the
	 * consistency check preserves the Log Matching Property whenever logs are
	 * extended. As a result, whenever
	 * {@link Server#appendEntries(int, String, int, long, Entry[], int)}
	 * returns successfully, the leader knows that the follower's log is
	 * identical to it's own log up through the new entries.
	 * <p>
	 * During normal operations, the logs of the leader and followers stay consistent, so the {@link Server#appendEntries}
	 * consistency check never fails.  However, leader crashes can leave the logs inconsistent (the old leader may not
	 * have fully replicated all o the entries in the log).  Theses inconsistenceices can 
	 * compound over a series of leader and follower crashes.  For example the following figure
	 * illustrates the ways in which followers logs may differ from that of a new leader. 
	 * 
	 * TODO : FIGURE OUT HOW TO DO FIXED WITH JAVADOC CORRECTLY
	 * 
	 * <p>
	 * <p>index 1 2 3 4 5 6 7 8 9 10 11 12 Role
	 * <p>      1 1 1 4 4 5 5 6 6 6        Leader for term 8
	 * <p>    a 1 1 1 4 4 5 5 6 6          Follower
	 * <p>    b 1 1 1 4                    Follower
	 * <p>    c 1 1 1 4 4 5 5 6 6 6  6     Follower
	 * <p>    d 1 1 1 4 4 5 5 6 6 6  7  7  Follower
	 * <p>    e 1 1 1 4 4 4 4              Follower
	 * <p>    f 1 1 1 2 2 2 3 3 3 3  3     Follower
	 * <p>
	 * When the leader at the top comes into power, it is possible that any of scenarios (a-f) could occur
	 * in follower logs.  Each box represents one log entry; the number per column is the term.  The following
	 * three scenarios exist
	 * <p>1.  A follower may be missing entries (a-b)
	 * <p>2.  May have extra uncommitted entries (c-d)
	 * <p>3.  Exhibit both missing (1) and extra uncommitted (2) entries (e-f)
	 * <p>
	 * Scenario f can occur if that server was the leader for term 2, added several entries to it's log,
	 * then crashed before committing any of them; it restarted quickly, became leader for term 3, and
	 * added a few more entries to it's log; before any of the entries in either term 2 or term 3 were
	 * committed, the server crashed again and remained down for several terms.
	 * 
	 * @param leaderTerm
	 * @param leaderId
	 * @param leaderPrevLogIndex
	 * @param leaderPrevLogTerm
	 * @param newEntries
	 * @param leaderCommitIndex
	 * @return
	 */
	public AppendEntryResult appendEntries(int leaderTerm, String leaderId,
			int leaderPrevLogIndex, long leaderPrevLogTerm, Iterable<Entry<T>> newEntries,
			int leaderCommitIndex) {
		
	    // If the leader term is stale, just return our term to it.
		System.out.println(String.format("leaderTerm %s < currentTerm %s", leaderTerm, currentTerm));
		if (leaderTerm < currentTerm) {
			System.out.println(String.format(
					"Caller(%s) is stale, current term=%d, leaders term=%d",
					leaderId, currentTerm, leaderTerm));
			return new AppendEntryResult(currentTerm, false);
		}
		
		System.out.println(String.format("commitIndex %s < prevLogIndex %s", commitIndex, leaderPrevLogIndex));
		
		System.out.println(String.format("a %s b %s", log.getEntry(leaderPrevLogIndex).getTerm(), leaderPrevLogTerm));

		// S T A R T   C O N S I S T E N C Y   C H E C K   -   P A R T   1
		// The given entry is accepted if it doesn't leave a gap
		int currentLogIndex = log.getSize();
		if (currentLogIndex < leaderPrevLogIndex) {
			System.out.println(
			    String.format("Rejecting appendEntries, due to gap in log (current log index = %d, request prev log index = %d)", 
			        currentLogIndex, leaderPrevLogIndex));
			return new AppendEntryResult(currentTerm, false);
		}

		// S T A R T   C O N S I S T E N C Y   C H E C K   -   P A R T   2
		// TODO might have to add additional check if start index can change due to log compaction
		long follerPrevLogTerm = log.getEntry(leaderPrevLogIndex).getTerm();
		if (follerPrevLogTerm != leaderPrevLogTerm) {
			System.out.println(
			    String.format("Rejecting appendEntries, terms don't agree (current term = %d, leader term = %d)", 
			    		follerPrevLogTerm, leaderPrevLogTerm));
			return new AppendEntryResult(currentTerm, false);
		}
		
		// A D D   L O G   E N T R I E S
		log.add(newEntries);
		
		// TODO Add complicated out of sync log cleanup cases (see right hand column of page 7 in paper)
		
		if (leaderCommitIndex > commitIndex) {
			int indexOfLastNewEntry = log.getSize();
			commitIndex = Math.min(leaderCommitIndex, indexOfLastNewEntry);
		}

		return new AppendEntryResult(currentTerm, true);
	}

	public static class AppendEntryResult {
		private final long term;
		private final boolean success;
		
		public AppendEntryResult(long term, boolean success) {
			this.term = term;
			this.success = success;
		}

		public long getTerm() {
			return term;
		}
		
		public boolean isSuccess() {
			return success;
		}
	}

	// Supports testing so left package private
	void setTerm(long newTerm) {
		currentTerm = newTerm;
	}
	
	long getCurrentTerm() {
		return currentTerm;
	}

	public int getCommitIndex() {
		return commitIndex;
	}
	
	String getId() {
		return id;
	}
}
