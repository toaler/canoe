package canoe;

public enum State implements Transitions {

	FOLLOWER {
		public State timesOut() {
			return State.CANDIDATE;
		}

		public State startsElection() {
			return State.CANDIDATE;
		}

		public State elected() {
			throw new UnsupportedOperationException();
		}
		
		public State discoversNewLeader() {
			throw new UnsupportedOperationException();
		}
	},
	CANDIDATE {
		public State timesOut() {
			return State.CANDIDATE;
		}

		public State startsElection() {
			return State.CANDIDATE;
		}

		public State elected() {
			return State.LEADER;
		}

		public State discoversNewLeader() {
			return State.FOLLOWER;
		}
	},
	LEADER {
		public State timesOut() {
			throw new UnsupportedOperationException();
		}

		public State startsElection() {
			throw new UnsupportedOperationException();
		}

		public State elected() {
			throw new UnsupportedOperationException();
		}
		
		public State discoversNewLeader() {
			return State.FOLLOWER;
		}
	};

}