package canoe;

public interface Transitions {
	public State timesOut();
	public State startsElection();
	public State elected();
	public State discoversNewLeader();
}