package ai2017.group1.boa.opponent;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import negotiator.bidding.BidDetails;
import negotiator.boaframework.BOAparameter;
import negotiator.boaframework.NegotiationSession;
import negotiator.boaframework.OMStrategy;
import negotiator.boaframework.OpponentModel;

/**
 * OMStrategy Group 1 2017.
 * This class was partially based on the BestBid class offered as an example in the BOA framework.
 */
public class Bidding extends OMStrategy {

	double updateThreshold = 1.1;

	/**
	 * Initialize the OM Strategy.
	 *
	 * @param negotiationSession negotiation session as set by GENIUS
	 * @param model              opponent model used
	 * @param parameters         parameters including the value for 't'
	 */
	@Override
	public void init(NegotiationSession negotiationSession, OpponentModel model, Map<String, Double> parameters) {
		super.init(negotiationSession, model, parameters);
		if (parameters.get("t") != null) {
			this.updateThreshold = parameters.get("t").doubleValue();
		} else {
			System.out.println("No value for t set. Used t = 1.1");
		}
	}

	/**
	 * Get the bid that we would like to make from a list of bids available.
	 *
	 * @param allBids list of bids to choose form
	 * @return optimal strategy bid
	 */
	@Override
	public BidDetails getBid(List<BidDetails> allBids) {
		// If there is only one option offer this one.
		if (allBids.size() == 1) {
			return allBids.get(0);
		}
		// Else search for best bid. Initialize bestUtil to min value and bestBid to the first one available.
		double bestUtil = -1;
		BidDetails bestBid = allBids.get(0);
		boolean allWereZero = true;

		// Loop over possible bids to see which one has the highest util.
		for (BidDetails bid : allBids) {
			double evaluation = model.getBidEvaluation(bid.getBid());
			if (evaluation > 0.0001) {
				allWereZero = false;
			}
			if (evaluation > bestUtil) {
				bestBid = bid;
				bestUtil = evaluation;
			}
		}
		// If all were zero the opponent model failed, then choose a random bid from the list.
		if (allWereZero) {
			Random r = new Random();
			return allBids.get(r.nextInt(allBids.size()));
		}
		return bestBid;
	}

	/**
	 * Check whether we can update the model based on time and threshold.
	 *
	 * @return boolean whether we can update
	 */
	@Override
	public boolean canUpdateOM() {
		return negotiationSession.getTime() < updateThreshold;
	}

	@Override
	public Set<BOAparameter> getParameterSpec() {
		Set<BOAparameter> set = new HashSet<BOAparameter>();
		set.add(new BOAparameter("t", 1.1, "Time threshold for updating."));
		return set;
	}

	@Override
	public String getName() {
		return "Group 1 2017 bidding.";
	}
}