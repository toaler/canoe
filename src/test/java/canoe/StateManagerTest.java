package canoe;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import canoe.timer.NanoSource;


public class StateManagerTest {
	
	@Test
	public void startState() {
		StateMachine sm = new StateMachine();
		assertEquals(State.FOLLOWER, sm.getCurrentState());
	}
	
	@Test
	public void testFollowerToCandidate() {
		StateMachine sm = new StateMachine();
		assertEquals(State.FOLLOWER, sm.getCurrentState());
		
		sm.timesOut();
		assertEquals(State.CANDIDATE, sm.getCurrentState());
	}
	
	@Test
	public void testCandidateToCandidate() {
		StateMachine sm = new StateMachine(State.CANDIDATE);
		assertEquals(State.CANDIDATE, sm.getCurrentState());
		
		sm.timesOut();
		assertEquals(State.CANDIDATE, sm.getCurrentState());
	}
	
	@Test
	public void testCandidateToFollower() {
		StateMachine sm = new StateMachine(State.CANDIDATE);
		assertEquals(State.CANDIDATE, sm.getCurrentState());
		
		sm.discoversNewLeader();
		assertEquals(State.FOLLOWER, sm.getCurrentState());
	}
	
	@Test
	public void testCandidateToLeader() {
		StateMachine sm = new StateMachine(State.CANDIDATE);
		assertEquals(State.CANDIDATE, sm.getCurrentState());
		
		sm.elected();
		assertEquals(State.LEADER, sm.getCurrentState());
	}
	
	@Test
	public void testLeaderToFollower() {
		StateMachine sm = new StateMachine(State.LEADER);
		assertEquals(State.LEADER, sm.getCurrentState());
		
		sm.discoversNewLeader();
		assertEquals(State.FOLLOWER, sm.getCurrentState());
	}
	
	@Test
	public void testElection() {
		int N = 5;
		
		List<StateMachine> machines = new ArrayList<>();
		for (int i = 0; i < N; i++) {
			machines.add(new StateMachine());
		}
	}
	
    public static class FakeTimeSource extends NanoSource {
        long nanos;

        @Override
        public long getNanos() {
            return nanos;
        }

        /**
         * Advance this time source by {@code amount} in nanoseconds.
         */
        public void tick(long amount) {
            nanos += amount;
        }
    }
}
