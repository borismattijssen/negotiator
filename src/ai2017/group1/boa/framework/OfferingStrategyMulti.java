package ai2017.group1.boa.framework;

import java.io.Serializable;
import java.util.Map;
import negotiator.bidding.BidDetails;
import negotiator.boaframework.BOA;
import negotiator.boaframework.BoaType;
import negotiator.boaframework.NegotiationSession;
import negotiator.boaframework.OpponentModel;
import negotiator.boaframework.SharedAgentState;

public abstract class OfferingStrategyMulti extends BOA {
	protected BidDetails nextBid;
	protected OpponentModel[] opponentModels;
	protected OMStrategyMulti omStrategy;
	protected SharedAgentState helper;
	protected boolean endNegotiation;

	public OfferingStrategyMulti() {
	}

	public void init(NegotiationSession negotiationSession, OpponentModel[] opponentModels, OMStrategyMulti omStrategy, Map<String, Double> parameters) throws Exception {
		super.init(negotiationSession, parameters);
		this.opponentModels = opponentModels;
		this.omStrategy = omStrategy;
		this.endNegotiation = false;
	}

	public abstract BidDetails determineOpeningBid();

	public abstract BidDetails determineNextBid();

	public BidDetails getNextBid() {
		return this.nextBid;
	}

	public void setNextBid(BidDetails nextBid) {
		this.nextBid = nextBid;
	}

	public SharedAgentState getHelper() {
		return this.helper;
	}

	public boolean isEndNegotiation() {
		return this.endNegotiation;
	}

	public final void storeData(Serializable object) {
		this.negotiationSession.setData(BoaType.BIDDINGSTRATEGY, object);
	}

	public final Serializable loadData() {
		return this.negotiationSession.getData(BoaType.BIDDINGSTRATEGY);
	}
}
