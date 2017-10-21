package ai2017.group1.boa.framework;

import misc.Range;
import negotiator.bidding.BidDetails;
import negotiator.boaframework.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public abstract class OMStrategyMulti extends BOA {

	protected OpponentModel[] models;
	private final double RANGE_INCREMENT = 0.01D;
	private final int EXPECTED_BIDS_IN_WINDOW = 100;
	private final double INITIAL_WINDOW_RANGE = 0.01D;

	public OMStrategyMulti() {
	}

	public void init(NegotiationSession negotiationSession, OpponentModel[] models, Map<String, Double> parameters) {
		super.init(negotiationSession, parameters);
		this.models = models;
	}

	public abstract BidDetails getBid(List<BidDetails> var1);

	public BidDetails getBid(OutcomeSpace space, Range range) {
		List bids = space.getBidsinRange(range);
		if(bids.size() == 0) {
			if(range.getUpperbound() < 1.01D) {
				range.increaseUpperbound(0.01D);
				return this.getBid(space, range);
			} else {
				this.negotiationSession.setOutcomeSpace(space);
				return this.negotiationSession.getMaxBidinDomain();
			}
		} else {
			return this.getBid(bids);
		}
	}

	public void setOpponentModels(OpponentModel[] models) {
		this.models = models;
	}

	public BidDetails getBid(SortedOutcomeSpace space, double targetUtility) {
		Range range = new Range(targetUtility, targetUtility + 0.01D);
		List bids = space.getBidsinRange(range);
		if(bids.size() < 100) {
			if(range.getUpperbound() < 1.01D) {
				range.increaseUpperbound(0.01D);
				return this.getBid(space, range);
			} else {
				return this.getBid(bids);
			}
		} else {
			return this.getBid(bids);
		}
	}

	public abstract boolean canUpdateOM();

	public final void storeData(Serializable object) {
	}

	public final Serializable loadData() {
		return null;
	}

}
