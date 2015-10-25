package canoe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import canoe.Server.AppendEntryResult;
import canoe.Server.RequestVoteResult;

public class ServerTest {
	private static Log<Void> EMPTY_LOG = new Log<Void>();
	
	@Test
	public void testRequestVoteGranted() {
		String serverId = "server";
		String serverVotedFor = null;
		int serverCurrentTerm = 10;
		int serverCommitIndex = 10;

		Server<Void> s = new Server<>(serverId, serverCurrentTerm, serverVotedFor, serverCommitIndex, EMPTY_LOG);
		
		String candidateId = "foo";
		int candidateTerm = 11;
		int candidateLastLogIndex = 100;
		int candidateLastLogTerm = 12;
		
		RequestVoteResult r = s.requestVote(candidateTerm, candidateId, candidateLastLogIndex, candidateLastLogTerm);

		assertTrue(r.getVoteGranted());
		assertEquals(r.getTerm(), serverCurrentTerm);
	}
	
	@Test
	public void testRequestVoteGrantedToSameCandidate() {
		String serverId = "server";
		String serverVotedForCandidate = "foo";
		int serverCurrentTerm = 10;
		int serverCommitIndex = 10;

		Server<Void> s = new Server<>(serverId, serverCurrentTerm, serverVotedForCandidate, serverCommitIndex, EMPTY_LOG);

		String candidateId = "foo";
		int candidateTerm = 11;
		int candidateLastLogIndex = 100;
		int candidateLastLogTerm = 12;
		
		RequestVoteResult r = s.requestVote(candidateTerm, candidateId, candidateLastLogIndex, candidateLastLogTerm);

		assertTrue(r.getVoteGranted());
		assertEquals(r.getTerm(), serverCurrentTerm);
	}
	
	@Test
	public void testRequestVoteRejectedDueToIncompatibleTerms() {
		String serverId = "server";
		int serverCurrentTerm = 10;
		String votedForCandidate = null;
		int commitIndex = 10;

		Server<Void> s = new Server<>(serverId, serverCurrentTerm, votedForCandidate, commitIndex, EMPTY_LOG);

		String candidateId = "foo";
		int candidateTerm = 9;
		int lastLogIndex = 100;
		int lastLogTerm = 12;
		
		RequestVoteResult r = s.requestVote(candidateTerm, candidateId, lastLogIndex, lastLogTerm);

		assertFalse(r.getVoteGranted());
		assertEquals(r.getTerm(), serverCurrentTerm);
	}
	
	@Test
	public void testRequestVoteRejectedServerAlreadyVoted() {
		String serverId = "server";
		String serverVotedForCandidate = "foo";
		int serverCurrentTerm = 10;
		int serverCommitIndex = 10;

		Server<Void> s = new Server<>(serverId, serverCurrentTerm, serverVotedForCandidate, serverCommitIndex, EMPTY_LOG);

		String candidateId = "bar";
		int candidateTerm = 11;
		int candidateLastLogIndex = 100;
		int candidateLastLogTerm = 12;
		
		RequestVoteResult r = s.requestVote(candidateTerm, candidateId, candidateLastLogIndex, candidateLastLogTerm);
		
		assertFalse(r.getVoteGranted());
		assertEquals(r.getTerm(), serverCurrentTerm);
	}
	
	@Test
	public void testRequestVoteRejectedServerLastLogEntryAheadOfCandidate() {
		String serverId = "server";
		String serverVotedForCandidate = "foo";
		int serverCurrentTerm = 10;
		int serverCommitIndex = 14;

		Server<Void> s = new Server<>(serverId, serverCurrentTerm, serverVotedForCandidate, serverCommitIndex, EMPTY_LOG);
		
		String candidateId = "bar";
		int candidateTerm = 11;
		int candiatelastLogIndex = 100;
		int candiatelastLogTerm = 12;
		
		RequestVoteResult r = s.requestVote(candidateTerm, candidateId, candiatelastLogIndex, candiatelastLogTerm);
		
		assertFalse(r.getVoteGranted());
		assertEquals(r.getTerm(), serverCurrentTerm);
	}
	
	@Test
	public void testAppendEntriesRejectedCurrentTermGreaterThanOrEqualToLeaderTerm() {
		String serverId = "server";
		String serverVotedForCandidate = "foo";
		int serverCurrentTerm = 10;
		int serverCommitIndex = 14;

		Server<Void> s = new Server<>(serverId, serverCurrentTerm, serverVotedForCandidate, serverCommitIndex, EMPTY_LOG);
		
		int leaderTerm = 9;
		
		AppendEntryResult r = s.appendEntries(leaderTerm, null, 0, 0, null, 0);
		
		assertFalse(r.isSuccess());
		assertEquals(r.getTerm(), serverCurrentTerm);
	}
	
	@Test
	public void testAppendEntriesRejectedLeaderPrevLogTermLessThanServerCommitIndex() {
		String serverId = "server";
		String serverVotedForCandidate = "foo";
		int serverCurrentTerm = 10;
		int serverCommitIndex = 14;

		Server<Void> s = new Server<>(serverId, serverCurrentTerm, serverVotedForCandidate, serverCommitIndex, EMPTY_LOG);
		
		int leaderTerm = 9;
		int leaderPrevLogTerm = 13;
		
		AppendEntryResult r = s.appendEntries(leaderTerm, null, leaderPrevLogTerm, 0, null, 0);
		
		assertFalse(r.isSuccess());
		assertEquals(r.getTerm(), serverCurrentTerm);
	}
	
	@Test
	public void testAppendEntriesRejectedLeaderAndServerTermsForPrevLogIndexNotMatching() {
		String serverId = "server";
		String serverVotedForCandidate = "foo";
		int serverCurrentTerm = 10;
		int serverCommitIndex = 1;
		
		Log<String> log = new Log<>();
		log.add(new Entry<String>(serverCurrentTerm, 1, "foo"));
		
		Server<String> s = new Server<>(serverId, serverCurrentTerm, serverVotedForCandidate, serverCommitIndex, log);
		
		int leaderPrevLogIndex = 1;
		
		// TODO do the test where term's are the same
		int leaderTerm = 11;
		int leaderPrevLogTerm = 1;
		
		
		AppendEntryResult r = s.appendEntries(leaderTerm, null, leaderPrevLogIndex, leaderPrevLogTerm, null, 0);
		
		assertFalse(r.isSuccess());
		assertEquals(r.getTerm(), serverCurrentTerm);
	}
	
	@Test
	public void testAppendEntriesSuccessfullyAppendingNewEntries() {
		String leaderId = "leader";
		String serverVotedForCandidate = "leader";
		int leaderCommitIndex = 3;

		Log<String> leaderLog   = new Log<>();
		Log<String> followerLog = new Log<>();
		
		// create fake log with a, b, c
		List<Entry<String>> newLogEntries = new ArrayList<>();
		newLogEntries.add(new Entry<String>(10, 1, "a"));
		newLogEntries.add(new Entry<String>(11, 2, "b"));
		newLogEntries.add(new Entry<String>(12, 3, "c"));
		
		followerLog.add(newLogEntries);
		leaderLog.add(newLogEntries);
		
		int currentTerm = 13;
		
		Server<String> follower = new Server<>(leaderId, currentTerm, serverVotedForCandidate, leaderCommitIndex, followerLog);
		
		assertEquals(leaderCommitIndex, follower.getCommitIndex());
		
		int leaderPrevLogIndex = 3;
		long leaderPrevLogTerm = 12;
		int leaderTerm = currentTerm;

		// create fake log with a, b, c
		newLogEntries = new ArrayList<>();
		newLogEntries.add(new Entry<String>(13, 4, "x"));
		newLogEntries.add(new Entry<String>(13, 5, "y"));
		newLogEntries.add(new Entry<String>(13, 6, "z"));
		
		leaderLog.add(newLogEntries);
		
		System.out.println("log size = " + leaderLog.getSize());
		
		follower.setTerm(13);
		
		AppendEntryResult r = follower.appendEntries(leaderTerm, leaderId, leaderPrevLogIndex, leaderPrevLogTerm, newLogEntries, 6);
		
		assertTrue(r.isSuccess());
		assertEquals(follower.getCurrentTerm(), r.getTerm());
		
		// Compare logs
		assertEquals(leaderLog.getSize(), followerLog.getSize());
		for (int i = 1; i < leaderLog.getSize(); i++) {
			assertEquals(leaderLog.getEntry(i), followerLog.getEntry(i));
		}
		
		assertEquals(6, follower.getCommitIndex());
	}
}
