package canoe;

public class StateMachine implements Transitions {
	
	private State current;
	
	public StateMachine() {
		this.current = State.FOLLOWER;
	}
	
	StateMachine(State candidate) {
		this.current = candidate;
	}

	public State getCurrentState() {
		return current;
	}

	public State timesOut() {
		return current = current.timesOut();
	}

	public State startsElection() {
		return current = current.startsElection();
	}

	public State elected() {
		return current = current.elected();
	}

	public State startElection() {
		return current = current.startsElection();
	}

	public State discoversNewLeader() {
		return current = current.discoversNewLeader();
	}
}