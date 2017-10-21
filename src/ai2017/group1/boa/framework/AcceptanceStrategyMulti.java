package ai2017.group1.boa.framework;

import java.io.Serializable;
import java.util.Map;
import negotiator.boaframework.Actions;
import negotiator.boaframework.BOA;
import negotiator.boaframework.BoaType;
import negotiator.boaframework.NegotiationSession;
import negotiator.boaframework.OpponentModel;
import negotiator.boaframework.SharedAgentState;
import negotiator.protocol.BilateralAtomicNegotiationSession;

public abstract class AcceptanceStrategyMulti extends BOA {
	protected OfferingStrategyMulti offeringStrategy;
	protected SharedAgentState helper;
	protected OpponentModel[] opponentModels;

	public AcceptanceStrategyMulti() {
	}

	public void init(NegotiationSession negotiationSession, OfferingStrategyMulti offeringStrategy, OpponentModel[] opponentModels, Map<String, Double> parameters) throws Exception {
		super.init(negotiationSession, parameters);
		this.offeringStrategy = offeringStrategy;
		this.opponentModels = opponentModels;
	}

	public String printParameters() {
		return "";
	}

	public void setOpponentUtilitySpace(BilateralAtomicNegotiationSession fNegotiation) {
	}

	public abstract Actions determineAcceptability();

	public final void storeData(Serializable object) {
		this.negotiationSession.setData(BoaType.ACCEPTANCESTRATEGY, object);
	}

	public final Serializable loadData() {
		return this.negotiationSession.getData(BoaType.ACCEPTANCESTRATEGY);
	}

	public boolean isMAC() {
		return false;
	}
}
